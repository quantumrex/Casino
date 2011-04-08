/**
 * 
 */
package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.nhksos.quantumrex.Game.Game;

/**
 * @author quantumrex
 *
 */

enum JobType {CASINO_CREATE, GAME_CREATE, READY};

public class CMPlayerListener extends PlayerListener {
	CasinoManager plugin;
	HashMap<Block, Game> games;
	
	boolean enabled;
	JobType current;
	
	Casino house;
	Game machine;
	Player man;
	
	
	public CMPlayerListener(CasinoManager parent){
		games = new HashMap<Block, Game>();
		plugin = parent;
		enabled = false;
		man = null;
		current = JobType.READY;
	}
	
	public boolean setEnabled(Player trigger){
		if(!enabled){
			System.out.println("Listener enabled...");
			enabled = true;
			man = trigger;
			return true;
		}
		return false;
	}
	
	public void setDisabled(){
		if(enabled){
			System.out.println("Listener disabled...");
			current = JobType.READY;
			enabled = false;
			man = null;
		}
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_AIR){
			System.out.println(event.getPlayer().getName() + " touched a block!\n" +
					"Touch method was :" + event.getAction().toString() + "\n" + 
					"Block type : " + event.getClickedBlock().getType().toString());
			switch (current){
			case CASINO_CREATE:
				if(enabled){
					if(event.getPlayer() == man){
						if(house.defineCasino(event.getClickedBlock()))
							setDisabled();
					}
				}
				break;
			case GAME_CREATE:
				if(enabled){
					if(event.getPlayer() == man){
						if(machine.buildInteract(event.getClickedBlock())){
							games.put(event.getClickedBlock(), machine);
							setDisabled();
						}
					}
				}
				break;
			default:
				if(!games.isEmpty()){
					if(games.containsKey(event.getClickedBlock())){
						System.out.println("Activating game!");
						Game game = games.get(event.getClickedBlock());
						if (game != null)
							game.enable(event.getPlayer());
					}
				}
			}
		}
		return;
	}

	public boolean setJob(JobType job, Casino casino) {
		System.out.println("Giving job: " + job.toString());
		current = job;
		house  = casino;
		return true;
	}

	public boolean setJob(JobType job, Casino casino, Game client) {
		System.out.println("Giving job: " + job.toString());
		current = job;
		house = casino;
		machine = client;
		return true;
	}
	
	public void registerGames(HashMap<Block, Game> newgames){
		games.putAll(newgames);
	}
}
