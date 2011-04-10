package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.BlockVector;

import com.nhksos.quantumrex.Casino.Casino.CasinoIDAccess;
import com.nhksos.quantumrex.Game.Game;
import com.nhksos.quantumrex.Game.Game.GameIDAccess;

public class DataManager {
	private CasinoManager parent;
	public static PluginDescriptionFile description;
	
	private final String defaultpath = "plugins/CasinoManager";
	private final String configname = "config.yml";
	private final String savename = "casinodata.save";
	
	private GameIDAccess gamekey;
	private CasinoIDAccess casinokey;
	
	private HashMap<BlockVector, ID> activators;
	private HashMap<String, ID> owners;
	private HashMap<ID, Casino> casinos;
	private HashMap<ID, Game> games;
	private HashMap<String, Job> jobs;
	private HashMap<String, ID> running;
	private HashMap<ID, Bet> bets;
	
	private ConfigWriter config;
	
	public DataManager(CasinoManager p){
		parent = p;
		description = parent.getDescription();
		
		Casino.init(this);
		Game.init(this);
		
		config = new ConfigWriter(this);
		//load();
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
	
	/* Have not yet implemented file Saving or Loading, so there will be none of that.
	 * 
	 * public void load(){
		activators = config.readActivators();
		owners = config.readOwners();
		casinos = config.readCasinos();
		games = config.readGames();
	}
	
	public void save(){
		//TODO Pre-process jobs, running, and bets
		config.writeActivators(activators);
		config.writeOwners(owners);
		config.writeCasinos(casinos);
		config.writeGames(games);
	}*/
	
	public void registerCasino(ID id, BlockVector vector){
		if (casinos.get(id).defineCasino(vector)){
			String player = casinos.get(id).owner.getName();
			cancelJob(player);
		}
			
	}

	public void nameCasino(ID id, String name){
		if (casinos.get(id).setName(name)){
			String player = casinos.get(id).owner.getName();
			cancelJob(player);
		}
	}
	
	public void testCasino(String string){
		//TODO Implement
	}
	
	public void destroyCasino(String string){
		//TODO Implement
	}
	
	public void registerGame(){
		//TODO Implement
	}
	
	public boolean isGameActivator(BlockVector vector) {
		return activators.containsKey(vector);
	}
	
	public void playGame(BlockVector vector, Player player){
		if (!running.containsKey(player.getName())){
			ID temp = activators.get(vector);
			running.put(player.getName(), temp);
			games.get(temp).enable(player);
		}
		else{
			player.sendMessage("You seem to be playing a game already.");
			player.sendMessage("Finish the game you started first.");
		}
	}
	
	public void destroyGame(){
		//TODO Implement
	}
	
	public void registerJob(Player player, JobType job){
		if(!jobs.containsKey(player.getName())){
			//Create a new job for the DataManager to feed to CMPlayerListener
			Job newjob = new Job();
			newjob.job = job;
			newjob.player = player;
			//Job needs an ID
			ID current = new ID();
			
			switch(job){
			case CASINO_CREATE:
				if (!owners.containsKey(player.getName())){
					current.casinoID = casinokey.getNextID();
					current.gameID = Game.NullID;
					
					newjob.using = current;
					jobs.put(player.getName(), newjob);
					owners.put(player.getName(), current);
					Casino newcasino = new Casino(this, player, current);
					casinos.put(current, newcasino);

					player.sendMessage("You are now ready to make your casino.");
					player.sendMessage("Start by clicking the blocks that define \n" +
									   "  two opposite corners of its boundary.");
					player.sendMessage("You must also name it. Example:\n " +
									   "  \"name Casino\" would name it Casino.");
				}
				else{
					player.sendMessage("You seem to have a Casino already.");
					player.sendMessage("Delete it if you want another. Use /destroycasino");
				}
				break;
			case GAME_CREATE:
				if(owners.containsKey(player.getName())){
					current.casinoID = owners.get(player.getName()).casinoID;
					current.gameID = gamekey.getNextID();
					
					newjob.using = current;
					jobs.put(player.getName(), newjob);

					player.sendMessage("You are now ready to make a game for your casino.");
					player.sendMessage("  Start by issuing a game type. For Example:");
					player.sendMessage("  \"game slot\" would get you a slot machine");
				}
				else{
					player.sendMessage("You don't seem to have a casino to put games in.");
					player.sendMessage("You need to create a casino first. Use /makecasino");
				}
				break;
			}
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
		jobs.remove(player);
	}
	
	public Job getJob(String player){
		return jobs.get(player);
	}
	
	public boolean finishJob(){
		//TODO Implement
		return false;
	}
	
	public boolean placeBet(){
		//TODO Implement
		return false;
	}

}

class Bet{
	String player;
	int amount;
	String outcome;
}

class ID{
	int casinoID;
	int gameID;
}

class Job{
	JobType job;
	Player player;
	ID using;
}

enum JobType {CASINO_CREATE, GAME_CREATE, GAME_DESTROY};
enum GameType{SLOT_MACHINE, ROULETTE_WHEEL, SHELL_GAME, BLACKJACK, TARGET_GAME};