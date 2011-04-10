package com.nhksos.quantumrex.Casino;

import com.nhksos.quantumrex.Game.Game;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class Casino {
	private static int NextID = 0;
	public static final int NullID = -1;
	
	public Player owner;
	public String name;
	public ID id;
	public DataManager database;
	private BlockVector corner1, corner3;
	
	public Casino(DataManager data, Player person, ID i){
		System.out.println("Casino created for " + person.getName());
		database = data;
		owner = person;
		id = i;
		corner1 = corner3 = null;
	}
	
	public static class CasinoIDAccess{
		private CasinoIDAccess(){}
		
		public void setNextID(int id){
			NextID = id;
		}
		public int getNextID(){
			return NextID++;
		}
	}
	
	public static void init(DataManager db){
		db.receiveKey(new CasinoIDAccess());
	}
	
	public boolean defineGame(Game game, Block enabler){
		return false;
	}

	public Game createGame(GameType type) {
		return null;
	}

	public boolean defineCasino(BlockVector clicked) {
		if(corner1 == null){
			corner1 = clicked;
			owner.sendMessage("Corner 1 defined. One left!");
		}
		else if(clicked != corner1){
			corner3 = BlockVector.getMaximum(clicked, corner1).toBlockVector();
			corner1 = BlockVector.getMinimum(corner1, clicked).toBlockVector();
			corner3.setY(0);
			corner1.setY(0);
			
			owner.sendMessage("Casino boundaries defined!");
			owner.sendMessage("Total area: " + getArea() + "Square Meters.");
		}
		return complete();
	}
	
	public int getArea(){
		int length = corner3.getBlockX() - corner1.getBlockX();
		int width = corner3.getBlockZ() - corner1.getBlockZ();
		return length * width;
	}
	
	public boolean isInside(BlockVector coords){
		coords.setY(0);
		if (coords.isInAABB(corner1, corner3))
			return true;
		return false;
	}

	public boolean setName(String n) {
		name = n;
		owner.sendMessage("You Casino's name was set to: " + name + "!");
		
		return complete();
	}

	private boolean complete() {
		return (corner1 != null && corner3 != null && name != "");
	}
}
