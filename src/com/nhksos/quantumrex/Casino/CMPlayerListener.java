/**
 * 
 */
package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.util.BlockVector;

import com.nhksos.quantumrex.Game.Game;

/**
 * @author quantumrex
 *
 */

public class CMPlayerListener extends PlayerListener {
	DataManager database;
	
	public CMPlayerListener(DataManager db){
		database = db;
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		String name = player.getName();
		Block block = event.getClickedBlock();
		BlockVector vector = getBVector(event);
		if(database.hasJob(name)){
			Job temp = database.getJob(name);
			switch (temp.job){
			case CASINO_CREATE:
				database.registerCasino(temp.using, vector);
				break;
			case GAME_CREATE:
				database.registerGame(name, temp.using, block);
				break;
			}
		}
		else if(database.isPlaying(name)){
			database.playInteract(name, block);
		}
		else if (database.isGameActivator(getBVector(event))){
			database.playGame(vector, player);
		}
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		String name = event.getPlayer().getName();
		if(database.hasJob(name)){
			Job temp = database.getJob(name);
			String message = event.getMessage().toLowerCase();
			switch(temp.job){
			case CASINO_CREATE:
				if(message.matches("^name[ ].*")){
					event.setCancelled(true);
					database.nameCasino(temp.using, event.getMessage().substring(5));
				}
				break;
			case GAME_CREATE:
				if(message.matches("^type[ ].*")){
					event.setCancelled(true);
					try {
						GameType type = GameType.valueOf(message.substring(5));
						database.createGame(temp, type);
					} catch (IllegalArgumentException e) {
						event.getPlayer().sendMessage("That was not a valid game type. Try again.");
					}
				}
				break;
			}
		}
    }
	
	private BlockVector getBVector(PlayerInteractEvent event){
		return event.getClickedBlock().getLocation().toVector().toBlockVector();
	}
}
