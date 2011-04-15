package com.nhksos.quantumrex.Casino;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;

import com.nhksos.quantumrex.Casino.Casino.CasinoIDAccess;
import com.nhksos.quantumrex.Game.Game;
import com.nhksos.quantumrex.Game.Game.GameIDAccess;

/**
 * DataManager is the central database for this plugin's data. All necessary data 
 * is stored in a series of HashMaps that are read from and written to files via 
 * the Serializable interface.
 * @author quantumrex
 */
public class DataManager {
	private CasinoManager parent;
	/**
	 * Standard bukkit Description file reference
	 */
	private static PluginDescriptionFile description;
	
	
	/**
	 * Accessor to reset the ID generation System for Games. Implemented for security.
	 */
	private GameIDAccess gamekey;
	/**
	 * Accessor to reset the ID generation System for Casinos. Implemented for security.
	 */
	private CasinoIDAccess casinokey;
	
	/**
	 * A HashMap containing all game activators mapped to their respective GameIDs. An 
	 * activator is the button, switch, or pressure pad that begins a game. This is 
	 * used for looking up a gameID by the PLAYER_INTERACT event.
	 */
	private HashMap<SerialVector, ID> activators;
	/**
	 * A HashMap containing all of the Casino Owners Names, mapped to the ID of the casino
	 * that they own. Used to check if a specific Player owns a Casino.
	 */
	private HashMap<String, ID> owners;
	/**
	 * A HashMap containing all CasinoIDs, mapped to their respective Casinos. This map contains
	 * all Casinos that the plugin is managing. 
	 */
	private HashMap<ID, Casino> casinos;
	/**
	 * A HashMap containing all existing GameIDs mapped to the games that they represent. This 
	 * contains all games being managed by the plugin. Each game is aware of the Casino it belongs
	 * to, as well as all of the properties contained by the Casino.
	 */
	private HashMap<ID, Game> games;
	/**
	 * A HashMap containing all of the currently running tasks that the database is keeping track 
	 * of. These are used to direct player input and events to the proper Casino or Game. Jobs
	 * are looked up by player name. 
	 */
	private HashMap<String, Job> jobs;
	/**
	 * A HashMap containing all currently running games managed by the database. These are used to
	 * direct player input to the game, either thru chat events, or Player interacts.
	 */
	private HashMap<String, ID> running;
	/**
	 * Not yet implemented.
	 */
	//private HashMap<ID, Bet> bets;
	/**
	 * Not yet implemented.
	 */
	private HashMap<String, Stats> stats;
	/**
	 * The writer in charge of saving and loading game data for the Database.
	 */
	private ConfigWriter writer;
	/**
	 * A Configuration object based off of a default config.yml in the plugin's data directory.
	 */
	public Configuration config;
	
	/**
	 * Starts the database, loads all saved data and config options, reinitializes all 
	 * data members, and performs general maintenance to allow the program to run correctly.
	 * @param p The CasinoManager that spawned this DataManger. Used for server access.
	 */
	public DataManager(CasinoManager p){
		try {
			parent = p;
			description = parent.getDescription();
			
			//Get accessors for the ID system
			Casino.init(this);
			Game.init(this);
		
			//Create data writer for save management
			writer = new ConfigWriter(this);
		
			//Load savedata
			load();
			
			//Re-initialize restored save data from the server
			reinitialize();
			
			//Initialize ID generation system
			casinokey.setNextID(config.getInt("global.ID.start.casino", 17));
			gamekey.setNextID(config.getInt("global.ID.start.game", 17));
			
			//Create new runtime storage for player interactions
			jobs = new HashMap<String, Job>();
			running = new HashMap<String, ID>();
			//bets = new HashMap<ID, Bet>();
		} catch (IOException e) {
			//Assumed error in ConfigWriter methods. Fails the database load, and triggers the plugin disable.
			System.out.println("[CasinoManager] Database load error.");
			//In case config was never loaded
			try{
				if(config.getBoolean("global.debug", false))
					e.printStackTrace();
			}
			catch(Exception e2){
				System.out.println("[CasinoManager] There is a problem with the configuration file.");
				e2.printStackTrace();
			}
			finally{
				//Kill the plugin...
				parent.getServer().getPluginManager().disablePlugin(parent);
			}
		}
	}
	
	/**
	 * This function performs the final shutdown functions of the database. It saves the current state of the ID
	 * generators, writes all critical data to a file, and kills the writer.
	 * @return Returns false if there was a problem saving data, true otherwise.
	 */
	public boolean stop(){
		try{
			config.setProperty("global.ID.start.casino", casinokey.getNextID());
			config.setProperty("global.ID.start.game", gamekey.getNextID());
			save();
			writer.finalize();
		}catch(Exception e){
			return false;
		}
		return true;
	}

	/**
	 * Method to get the accessor for the ID creation system. This is vital for casino and game storage, so it is well
	 * protected.
	 * @param key The data accessor for the Casino class.
	 */
	public void receiveKey(CasinoIDAccess key) {
		casinokey = key;
	}
	/**
	 * Method to get the accessor for the ID creation system. This is vital for casino and game storage, so it is well
	 * protected.
	 * @param key The data accessor for the Game class.
	 */
	public void receiveKey(GameIDAccess key) {
		gamekey = key;
	}
	 
	
	/**
	 * Instructs the writer to read each of the critical HashMaps from their respective files.
	 */
	private void load(){
		stats = writer.getStats();
		activators = writer.readActivators();
		owners = writer.readOwners();
		casinos = writer.readCasinos();	
		games = writer.readGames();
	}
	
	/**
	 * Passes each of the critical HashMaps to the writer for output to a file. Performs pre-processing to remove unfinished
	 * Games and Casinos from the HashMaps before storace, to prevent errors on loading.
	 */
	private void save(){
		//TODO Pre-process jobs, running, and bets
		writer.writeStats(stats);
		writer.writeActivators(activators);
		writer.writeOwners(owners);
		writer.writeCasinos(casinos);
		writer.writeGames(games);
	}

	/**
	 * This method finishes restoring previously saved data to full operational status. Many components of the items stored in
	 * Games and Casinos are non-Serializable. Among these are Blocks and Players, which are critical to Game operation. It is 
	 * possible, however, to save a string containing a Player's name, or a SerialVector which represents a block. These can be
	 * used to restore the objects and bring the games back online.
	 */
	public void reinitialize() {
		System.out.println("[CasinoManager] Re-initializing Casino Database.");
		
		HashSet<ID> ckeys = new HashSet<ID>(casinos.keySet());
		for (ID i : ckeys){
			casinos.get(i).reinitialize(this);
			if (config.getBoolean("global.debug", false))
				System.out.println(casinos.get(i).toString());
		}

		HashSet<ID> gkeys = new HashSet<ID>(games.keySet());
		for (ID i : gkeys){
			games.get(i).reinitialize(this);
			if (config.getBoolean("global.debug", false))
				System.out.println(games.get(i).toString());
		}
	}
	
	/**
	 * This function is used to register a block with a Casino object. CMPlayerListener calls this function to pass a 
	 * corner block to the Casino. The return value send by the Casino function determines whether the casino building 
	 * job has been completed. If so, the job is cancelled.
	 * @param id ID of the Casino being built
	 * @param block The block being passed to the Casino for processing;
	 */
	public void registerCasino(ID id, Block block){
		if (casinos.get(id).defineCasino(block)){
			String player = casinos.get(id).owner;
			finishJob(player);
		}	
	}
	
	/**
	 * Passes a String to a Casino specified by ID in order to name it.
	 * @param id - The ID of the Casino being named;
	 * @param name - The new name of the casino
	 */
	public void nameCasino(ID id, String name){
		if (casinos.get(id).setName(name)){
			String player = casinos.get(id).owner;
			finishJob(player);
		}
	}
	/**
	 * Checks owners to see if a Player given by name owns a Casino. Playes must create a Casino before they can
	 * make games for it.
	 * @param name - The player being checked
	 * @return - True if owners contains a mapping for the Player referenced by name, false otherwise
	 */
	public boolean hasCasino(String name) {
		return owners.containsKey(name);
	}
	/**
	 * This method returns the Casino owned by the current user. This method does not check to see if the user has
	 * a Casino, It simply returns the value received from the HashMap. This can be null. This function should always
	 * be preceded by a call to hasCasino(name).
	 * @param name - The name of the owner of the sought Casino
	 * @return The Casino object owned by this player, or null if none is available.
	 */
	public Casino getCasino(String name) {
		return casinos.get(owners.get(name));
	}
	/**
	 * This method returns the Casino referenced by the given ID. This method assumed that the ID is an ID for an existing
	 * Casino object. 
	 * @param id - The ID of the sought Casino
	 * @return The Casino object listed under this ID, or null if none is available.
	 */
	public Casino getCasino(ID id) {
		return casinos.get(id);
	}
	/**
	 * NOT YET IMPLEMENTED!!!
	 */
	public void testCasino(String string){
		//TODO Implement
	}
	/**
	 * NOT YET IMPLEMENTED!!!
	 */
	public void destroyCasino(String string){
		//TODO Implement
	}
	
	/**
	 * This function creates an un-initialized game of the type specified. This method is called 
	 * when a player enters a type via the chat system, whether this ocurrs through the original 
	 * makemachine command, or through a type statement. Once the game is initialized, the function
	 * asks the user to select an activator block for the new game.
	 * @param job - The job that is creating this Game.
	 * @param type - The GameType that is being created.
	 */
	public void createGame(Job job, GameType type){
		ID cid = new ID(job.using.casinoID, Game.NullID);
		Casino house = casinos.get(cid);
		Game newgame = null;
		//Switch statement to call the appropriate Game subclass Constructor
		switch(type){
		case slot:
		case SLOT_MACHINE:
			newgame = house.createGame(GameType.SLOT_MACHINE, job.using);
			break;
		case wheel:
		case roulette:
		case ROULETTE_WHEEL:
			break;
		case cup:
		case shell:
		case SHELL_GAME:
			break;
		case bjack:
		case blackjack:
		case CARDS_BLACKJACK:
			break;
		case archery:
		case target:
		case TARGET_PRACTICE:
			break;
		}
		if (newgame != null){
			games.put(job.using, newgame);
			job.player.sendMessage("You have a new game for your casino. You now need");
			job.player.sendMessage("to construct it.");
			job.player.sendMessage("  Start by defining its activation switch.");
			job.player.sendMessage("  Click or otherwise activate the switch to");
			job.player.sendMessage("  set it.");
		}
	}

	/**
	 * This method checks to see if there is a game named by the current ID. If there is not, it is assumed that
	 * the user has not specified a type for the game, and therefore createGame() has not run. The return value
	 * of the game function represents its ready state. If true, the game has all the data it needs to run, and
	 * this job can be cancelled.
	 * @param name - Name of the player building the game.
	 * @param id - ID of the Game in progress
	 * @param block - The block being passed to the Game for processing.
	 */
	public void registerGame(String name, ID id, Block block) {
		if(games.containsKey(id)){
			if (games.get(id).buildInteract(block))
				finishJob(name);
		}
		else{
			parent.getServer().getPlayer(name).sendMessage(
					"You still need to state a type for this game."
					);
		}
	}
	/**
	 * This method places a game activator and a Game ID into the activators field. Once this is accomplished, 
	 * players can enable the machine in question by pressing that button.
	 * @param vector - Activator to be placed in the HashMap.
	 * @param id - The ID of the game to be activated.
	 */
	public void registerActivator(SerialVector vector, ID id){
		activators.put(vector, id);
	}
	/**
	 * Used by CMPlayerListener to determine if a block the player has interacted with should enable a game. 
	 * @param vector - Block the listener is concerned with.
	 * @return - True if the block is a registered activator, False otherwise.
	 */
	public boolean isGameActivator(SerialVector vector) {
		return activators.containsKey(vector);
	}
	/**
	 * This function is called by CMPlayerListener when it determines that a game activator has been pressed.
	 * It first checks to see if a player is already playing a game. Players are only allowed to run one game at once.
	 * It then checks the status of the game, and if the game is in the ready state, it adds the player to the
	 * running HashMap and calls the activated game's enable method.
	 * @param vector - SerialVector representing the activator that was pressed.
	 * @param player - Player that activated the game.
	 */
	public void playGame(SerialVector vector, Player player){
		if (!running.containsKey(player.getName())){
			ID temp = activators.get(vector);
			Game testing = games.get(temp);
			switch (testing.getState()){
			case UNINITIALIZED:
				testing.reinitialize(this);
			case READY:
				running.put(player.getName(), temp);
				testing.enable(player);
				break;
			case BROKEN:
				player.sendMessage("This game is currently out of order.");
				break;
			case RUNNING:
			case WAITING:
			case STOPPED:
				player.sendMessage("This game is currently in use. Try again later.");
				break;
			}
		}
		else{
			player.sendMessage("You seem to be playing a game already.");
			player.sendMessage("Finish the game you started first.");
		}
	}
	/**
	 * Function to test if a player is currently playing a game.
	 * @param player - The Player being tested.
	 * @return - True if the player is playing a game, False otherwise.
	 */
	public boolean isPlaying(String player){
		return running.containsKey(player);
	}
	
	/**
	 * Called by various listeners to pass events to a game being played. These will be interpreted by the game. Games are
	 * looked up by searching for the player's name in the running HashMap. It is assumed that the given Player is playing 
	 * a game. This call should always be preceded by a call to isPlaying(...).
	 * @param player - The player who fired the event.
	 * @param event - The event that was fired.
	 */
	public void playInteract(String player, Event event){
		games.get(running.get(player)).playInteract(event);
	}
	/**
	 * This function is called by the game to notify the database that it has finished its execution. This removes the player 
	 * from the running HashMap, and frees them to play other games. A game must always call this when it is finished.
	 * @param name - The name of the player who was playing the game.
	 */
	public void finishGame(String name){
		running.remove(name);
		System.out.println(name + " finished a game.");
	}
	/**
	 * NOT YET IMPLEMENTED!!!
	 */
	public void destroyGame(){
		//TODO Implement
	}
	
	/**
	 * This function is called by CasinoManager to register jobs with the database. Player commands that need to create
	 * jobs are handled here. It creates a new job, and creates an ID for the job. it then adds the job to the jobs HashMap,
	 * under the player's name. Any arguments that the player entered with the command are also processed, if possible.
	 * This function, after creating the job, also issues instructions to the player, stating how to continue working with 
	 * their job.
	 * @param player - Player who issued the command
	 * @param job - The type of job that was requested
	 * @param args - Any arguments that were issued with the command
	 */
	public void registerJob(Player player, JobType job, String[] args){
		if(!jobs.containsKey(player.getName())){
			boolean jobcreated = false;
			//Create a new job for the DataManager to feed to CMPlayerListener
			Job newjob = new Job();
			newjob.job = job;
			newjob.player = player;
			//Job needs an ID
			int casino, game;
			ID current;
			
			switch(job){
			case CASINO_CREATE:
				if (!owners.containsKey(player.getName())){
					casino = casinokey.getNextID();
					game = Game.NullID;
					
					current = new ID(casino, game);
					
					newjob.using = current;
					jobs.put(player.getName(), newjob);
					owners.put(player.getName(), current);
					Casino newcasino = new Casino(this, player, current);
					casinos.put(current, newcasino);

					player.sendMessage("You are now ready to make your casino.");
					player.sendMessage("Start by clicking the blocks that define \n" +
									   "  two opposite corners of its boundary.");
					if(args.length == 0){
						player.sendMessage("You must also name it. Example:\n " +
							"  \"name Casino\" would name it Casino.");
					}
					else if (args.length == 1){
						nameCasino(current, args[0]);
					}
					else{
						String name = args[0];
						for (int i = 1; i < args.length; i++){
							name = name + ' ' + args[i];
						}
						nameCasino(current, name);
					}
					jobcreated = true;
				}
				else{
					player.sendMessage("You seem to have a Casino already.");
					player.sendMessage("Delete it if you want another. Use /destroycasino");
				}
				break;
			case GAME_CREATE:
				if(owners.containsKey(player.getName())){
					casino = owners.get(player.getName()).casinoID;
					game = gamekey.getNextID();
					
					current = new ID(casino, game);
					
					newjob.using = current;
					jobs.put(player.getName(), newjob);

					player.sendMessage("You are now ready to make a game for your casino.");
					if (args.length == 0){
						player.sendMessage("  Start by issuing a game type. For Example:");
						player.sendMessage("  \"type slot\" would get you a slot machine");
					}
					else if (args.length == 1){
						try {
							GameType type = GameType.valueOf(args[0]);
							createGame(newjob, type);
						}
						catch (IllegalArgumentException e){
							player.sendMessage("That was not a valid game type. Try again;");
							player.sendMessage("  Start by issuing a game type. For Example:");
							player.sendMessage("  \"type slot\" would get you a slot machine");
						}
					}
					else{
						player.sendMessage("That was not a valid game type. Try again;");
						player.sendMessage("  Start by issuing a game type. For Example:");
						player.sendMessage("  \"type slot\" would get you a slot machine");
					}
					jobcreated = true;
				}
				else{
					player.sendMessage("You don't seem to have a casino to put games in.");
					player.sendMessage("You need to create a casino first. Use /makecasino");
				}
				break;
			}
			if (jobcreated)
				System.out.println("Created job for user " + player.getName() + ".\n" +
						"  Type: " + job.toString());
		}
		else{
			player.sendMessage("You're already working on a job. Finish it first.");
			Job temp = jobs.get(player.getName());
			player.sendMessage("Current Job: " + temp.job.toString());
		}
	}
	/**
	 * Called to check if a given player is currently working on a job.
	 * @param player - Player being tested.
	 * @return - True if the player is working, False otherwise.
	 */
	public boolean hasJob(String player){
		return jobs.containsKey(player);
	}
	/**
	 * Called when a player finishes a job. This removes them from the job HashMap, and frees them to perform other jobs.
	 * @param player
	 */
	private void finishJob(String player) {
		Job temp = jobs.get(player);
		jobs.remove(player);
		System.out.println("User " + player + " finished a job.\n" +
				"  Type: " + temp.job.toString());
	}
	/**
	 * Returns the job that the current player is working on. This method assumes that the given player refers to someone
	 * working on a job, so the return can be null. For safety, this method should be preceded by a call to hasJob(...).
	 * @param player - The player whose Job we are searching for.
	 * @return - The job being performed by the player.
	 */
	public Job getJob(String player){
		return jobs.get(player);
	}
	
	/**
	 * NOT YET IMPLEMENTED!!!
	 * @return - null
	 */
	public boolean cancelJob(){
		//TODO Implement if I ever need a public cancelJob
		return false;
	}
	/**
	 * NOT YET IMPLEMENTED!!!
	 * @return - null
	 */
	public boolean placeBet(){
		//TODO Implement
		return false;
	}
	/**
	 * Gets the server scheduler from CasinoManager. For use by games when events need to be registered.
	 * @return - The BukkitScheduler instance returned by the server.
	 */
	public BukkitScheduler getScheduler() {
		return parent.getServer().getScheduler();
	}
	/**
	 * Gets the CasinoManager for this plugin. Allows games that can access the database to interact with 
	 * other plugins. 
	 * @return - The CasinoManagaer representing this plugin.
	 */
	public CasinoManager getPlugin() {
		return parent;
	}
	/**
	 * Returns the world on this server represented by the given string. This is not a very robust call, 
	 * needs to be updated.
	 * @param world - The string representing the world being searched for.
	 * @return - The World if it exists, null otherwise.
	 */
	public World getWorld(String world) {
		//TODO Bugfix, to prevent NullPointerException on invalid world strings
		return parent.getServer().getWorld(world);
	}
	/**
	 * Returns the Plater represented by the given string, or null if the player is not present
	 * @param name - The name of the Player sought
	 * @return - The Player, if he exists, otherwise null.
	 */
	public Player getPlayer(String name) {
		return parent.getServer().getPlayer(name);
	}
	/**
	 * Returns the block in the World represented by world at the coordinates specified in wvector.
	 * @param wvector - Coordinates of the desired block
	 * @param world - World containing the desired block
	 * @return Block at the specified wvector in the given world, or null if it doesn't exist.
	 * @throws NullPointerException - if world does not represent a world on the current server, and possibly if
	 * there is a problem with wvector.
	 */
	public Block getBlock(SerialVector wvector, String world) throws NullPointerException{
		//TODO Make more robust to prevent exceptions
		return wvector.toLocation(getWorld(world)).getBlock();
	}
}

/**
 * Bet is a data container representing the amount of money a player has placed on a certain outcome of a game.
 * This money is removed from the players account when the bet is made, and is returned to the player with extra
 * upon the successful completion of the game.
 * @author quantumrex
 *
 */
class Bet{
	/**
	 * The name of the player who placed the bet.
	 */
	String player;
	/**
	 * The amount of currency the player has bet.
	 */
	double amount;
	/**
	 * The expected outcome of the game being played. This string should match the outcome of the game, if a player wins.
	 * It could be as simple as "win" or "lose", or something along the lines of "5 Red" or "30 chickens, 1 cow", as long 
	 * as the game can produce a matching outcome.
	 */
	String outcome;
}

/**
 * This is a data class representing per player (and maybe also per machine) wins losses etc. I hope to implement a statistical calculation system, for 
 * use by casino owners and patrons, but this has not happened as of yet.  
 * @author quantumrex
 *
 */
class Stats{
	/**
	 * The name of the player this statistic refers to.
	 */
	String name;
	/**
	 * The number of times a player has won in casino games.
	 */
	int wins;
	/**
	 * The number of times a player has lost in casino games.
	 */
	int losses;
	/**
	 * The total number of times that a player has played casino games.
	 */
	int total;
	/**
	 * The resulting profit or loss that a player has incurred from the casino.
	 */
	double balance;
}

/**
 * Job is a data structure for the databases use. It represents a player currently working on a 
 * job with a specific casino or machine.
 * @author quantumrex
 *
 */
class Job{
	/**
	 * Represents the type of job being handled. This determines what actions are taken when a 
	 * player issues a command or performs an action.  
	 */
	JobType job;
	/**
	 * Player executing the job. This allows for communication with the player during the job's execution,
	 * without having to fetch them from the server.
	 */
	Player player;
	/**
	 * The ID of the casino or game the player is working on. This is used to determine where to send
	 * input from the player. 
	 */
	ID using;
}

/**
 * JobType represents all possible jobs that a player could perform related to casino construction, 
 * maintenance, or demolition. These enum types are handled in registerJob(...), and should be 
 * registered in the switch statement there, as well as in CasinoManager's onCommand(...) function.
 * This ensures that the job will be created and used when a player issues a command.
 * @author quantumrex
 *
 */
enum JobType {CASINO_CREATE, GAME_CREATE, GAME_DESTROY};
/**
 * Gametype represents the official listing of all types of games suported by the casino system. Each 
 * registered gameType should have an OFFICIAL_DESIGNATION, followed by one or more aliases. This allows
 * players to easily select the type of game that they are creating, without having to type LONG_SENTENCES.
 * These enum types are handled in createGame(...), and should be registered in the switch statement there. 
 * 
 * @author quantumrex
 *
 */
enum GameType{
	SLOT_MACHINE, slot,  
	ROULETTE_WHEEL, roulette, wheel, 
	SHELL_GAME, shell, cup, 
	CARDS_BLACKJACK, blackjack, bjack, 
	TARGET_PRACTICE, target, archery 
};
