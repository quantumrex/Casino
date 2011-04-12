package com.nhksos.quantumrex.Casino;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.nhksos.quantumrex.Casino.Casino.CasinoIDAccess;
import com.nhksos.quantumrex.Game.Game;
import com.nhksos.quantumrex.Game.Game.GameIDAccess;

public class DataManager {
	private CasinoManager parent;
	public static PluginDescriptionFile description;
	
	private GameIDAccess gamekey;
	private CasinoIDAccess casinokey;
	
	private HashMap<SerialVector, ID> activators;
	private HashMap<String, ID> owners;
	private HashMap<ID, Casino> casinos;
	private HashMap<ID, Game> games;
	private HashMap<String, Job> jobs;
	private HashMap<String, ID> running;
	private HashMap<ID, Bet> bets;
	private HashMap<String, Stats> stats;
	
	private ConfigWriter config;
	
	public DataManager(CasinoManager p){
		parent = p;
		description = parent.getDescription();
		
		Casino.init(this);
		Game.init(this);
		
		config = new ConfigWriter(this);
		
		load();
		
		casinokey.setNextID(config.config.getInt("global.ID.start.casino", 17));
		gamekey.setNextID(config.config.getInt("global.ID.start.game", 17));
		
		jobs = new HashMap<String, Job>();
		running = new HashMap<String, ID>();
		bets = new HashMap<ID, Bet>();
	}

	public void receiveKey(CasinoIDAccess key) {
		casinokey = key;
	}

	public void receiveKey(GameIDAccess key) {
		gamekey = key;
	}
	 
	public void load(){
		stats = config.getStats();
		activators = config.readActivators();
		owners = config.readOwners();
		
		casinos = config.readCasinos();
		HashSet<ID> ckeys = new HashSet<ID>(casinos.keySet());
		for (ID i : ckeys){
			casinos.get(i).reinitialize(this);
		}
		
		games = config.readGames();
		HashSet<ID> gkeys = new HashSet<ID>(games.keySet());
		for (ID i : gkeys){
			games.get(i).reinitialize(this);
		}
	}
	
	public void save(){
		//TODO Pre-process jobs, running, and bets
		config.writeStats(stats);
		config.writeActivators(activators);
		config.writeOwners(owners);
		config.writeCasinos(casinos);
		config.writeGames(games);
	}
	
	public void registerCasino(ID id, Block block){
		if (testWorld(block.getWorld().getName())){
			if (casinos.get(id).defineCasino(getVector(block))){
				String player = casinos.get(id).owner.getName();
				cancelJob(player);
			}
		}	
	}
	public void nameCasino(ID id, String name){
		if (casinos.get(id).setName(name)){
			String player = casinos.get(id).owner.getName();
			cancelJob(player);
		}
	}
	public boolean hasCasino(String name) {
		return owners.containsKey(name);
	}
	public Casino getCasino(String name) {
		return casinos.get(owners.get(name));
	}
	public Casino getCasino(ID id) {
		return casinos.get(id);
	}
	
	public void testCasino(String string){
		//TODO Implement
	}
	
	public void destroyCasino(String string){
		//TODO Implement
	}
	
	public void createGame(Job job, GameType type){
		//TODO There's a bug in the ID assignment code
		ID cid = new ID(job.using.casinoID, Game.NullID);
		Casino house = casinos.get(cid);
		if (house == null)
			System.out.println("HOUSE IS NULL!!!");
		Game newgame = null;
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

	public void registerGame(String name, ID id, Block block) {
		if (testWorld(block.getWorld().getName())){
			if(games.containsKey(id)){
				if (games.get(id).buildInteract(block))
					cancelJob(name);
			}
			else{
				parent.getServer().getPlayer(name).sendMessage(
						"You still need to state a type for this game."
						);
			}
		}
	}
	public void registerActivator(SerialVector vector, ID id){
		activators.put(vector, id);
	}
	public boolean isGameActivator(SerialVector vector) {
		return activators.containsKey(vector);
	}
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
	public boolean isPlaying(String player){
		return running.containsKey(player);
	}
	public void playInteract(String player, Block block){
		games.get(running.get(player)).playInteract(block);
	}
	public void finishGame(String name){
		running.remove(name);
		System.out.println(name + " finished a game.");
	}
	public void destroyGame(){
		//TODO Implement
	}
	
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
						String name = "";
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
	
	public boolean hasJob(String player){
		return jobs.containsKey(player);
	}
	
	private void cancelJob(String player) {
		Job temp = jobs.get(player);
		jobs.remove(player);
		System.out.println("User " + player + " finished a job.\n" +
				"  Type: " + temp.job.toString());
	}
	
	public Job getJob(String player){
		return jobs.get(player);
	}
	
	public boolean cancelJob(){
		//TODO Implement if I ever need a public cancelJob
		return false;
	}
	
	public boolean placeBet(){
		//TODO Implement
		return false;
	}

	public BukkitScheduler getScheduler() {
		return parent.getServer().getScheduler();
	}

	public JavaPlugin getPlugin() {
		return parent;
	}
	
	public boolean testWorld(String w){
		String world = config.config.getString("global.world");
		if (world == null){
			config.config.setProperty("global.world", w);
			return true;
		}
		else if (w == world)
			return true;
		return false;
	}

	public World getWorld() {
		return parent.getServer().getWorld(config.config.getString("global.world"));
	}
	
	private SerialVector getVector(Block block){
		return new SerialVector(block.getLocation().toVector());
	}
}

class Bet{
	String player;
	int amount;
	String outcome;
}

class Stats{
	int wins;
	int losses;
	int total;
	double balance;
}

class Job{
	JobType job;
	Player player;
	ID using;
}

enum JobType {CASINO_CREATE, GAME_CREATE, GAME_DESTROY};
enum GameType{
	slot, SLOT_MACHINE, 
	roulette, wheel, ROULETTE_WHEEL, 
	shell, cup, SHELL_GAME, 
	blackjack, bjack, CARDS_BLACKJACK, 
	target, archery, TARGET_PRACTICE
};
