/**
 * 
 */
package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.nhksos.quantumrex.Game.Game;

/**
 * @author quantumrex
 *
 */

enum JobType {CASINO_CREATE, GAME_CREATE};

public class CMPlayerListener extends PlayerListener {
	CasinoManager plugin;
	HashMap<Block, Game> games;
	
	boolean enabled;
	JobType current;
	
	Casino house;
	Game machine;
	Player man;
	
	
	public CMPlayerListener(CasinoManager parent){
		plugin = parent;
		enabled = false;
		man = null;
		current = null;
	}
	
	public boolean setEnabled(Player trigger){
		if(!enabled){
			enabled = true;
			man = trigger;
			return true;
		}
		return false;
	}
	
	public void setDisabled(){
		if(enabled){
			current = null;
			enabled = false;
			man = null;
		}
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		switch (current){
		case CASINO_CREATE:
			if(enabled){
				if(event.getPlayer() == man){
					house.defineCasino(event.getClickedBlock());
				}
			}
			break;
		case GAME_CREATE:
			if(enabled){
				if(event.getPlayer() == man){
					machine.buildInteract(event.getClickedBlock());
				}
			}
			break;
		default:
			Game game = games.get(event.getClickedBlock());
			if (game != null)
				game.enable(event.getPlayer());
		}
	}

	public boolean setJob(JobType job, Casino casino) {
			current = job;
			return true;
	}

	public boolean setJob(JobType job, Casino casino, Game client) {
		current = job;
		house = casino;
		machine = client;
		
		return true;
	}
	
	public void registerGames(HashMap<Block, Game> newgames){
		games.putAll(newgames);
	}
}
