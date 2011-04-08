package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import com.nhksos.quantumrex.Game.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Casino {
	public Player owner;
	public String name;
	public CasinoManager plugin;
	public HashMap<Block, Game> games;
	private Block corner1, corner3;
	
	public Casino(CasinoManager parent, Player person){
		System.out.println("Casino created for " + person.getName());
		plugin = parent;
		owner = person;
		games = new HashMap<Block, Game>();
		corner1 = corner3 = null;
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

	public boolean defineCasino(Block clicked) {
		if (corner1 == null){
			corner1 = clicked;
			System.out.println("Corner1 defined!");
			return false;
		}
		else if(corner3 == null){
			int x, X, z, Z;
			if(corner1.getX() > clicked.getX()){
				x = clicked.getX();
				X = corner1.getX();
			}
			else{
				x = corner1.getX();
				X = clicked.getX();
			}
			if(corner1.getZ() > clicked.getZ()){
				z = clicked.getZ();
				Z = corner1.getZ();
			}
			else{
				z = corner1.getZ();
				Z = clicked.getZ();
			}
			
			World w = clicked.getWorld();
			Location l1, l3;
			l1  = l3 = clicked.getLocation().clone();
			l1.setX(x); l1.setZ(z); l1.setY(w.getHighestBlockYAt(x, z));
			l3.setX(X); l3.setZ(Z); l3.setY(w.getHighestBlockYAt(X, Z));
			corner1 = w.getBlockAt(l1);
			corner3 = w.getBlockAt(l3);
			System.out.println("Corner3 defined! Casino bounded!");
			
			return true;
		}
		else{
			System.out.println("Trying to add corners to an already defined casino... Weird huh?");
			return true;
		}
	}
	
	public boolean isInside(Block coords){
		if (corner1.getX() <= coords.getX() && corner3.getX() > coords.getX()){
			if(corner1.getZ() <= coords.getZ() && corner3.getZ() > coords.getZ()){
				return true;
			}
		}
		return false;
	}
}
