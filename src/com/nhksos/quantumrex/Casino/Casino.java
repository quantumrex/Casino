package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import com.nhksos.quantumrex.Game.*;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Casino {
	public Player owner;
	public String name;
	public CasinoManager plugin;
	public HashMap<Block, Game> games;
	
	public Casino(CasinoManager parent, Player person){
		plugin = parent;
		owner = person;
	}
	
	public boolean defineGame(Game game, Block enabler){
		if(games.containsValue(game)){
			if(games.containsKey(null)){
				games.remove(null);
				games.put(enabler, game);
			}
			else
				System.out.println("The game was already defined... weird huh?");
		}
		else
			System.out.println("This game is apparently not a member of this casino... weird huh?");
		return true;
	}
	
	public void define(Block corner1, Block corner3){
		
	}

	public Game createGame(GameType type) {
		Game game;
		switch (type){
		case SLOT_MACHINE:
			game = new SlotMachine(this);
			break;
		default:
			game = null;
		}
		if (game != null)
			games.put(null, game);
		
		return game;
	}

	public void defineCasino(Block clickedBlock) {
		// TODO Auto-generated method stub
		
	}
}
