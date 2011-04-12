package com.nhksos.quantumrex.Casino;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.nhksos.quantumrex.Game.*;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Casino implements Serializable {
	
	private static final long serialVersionUID = 4202979891585014056L;
	
	private static int NextID = 17;
	public static final int NullID = -1;
	
	public transient DataManager database;
	public transient Player owner;
	
	public String name;
	public final ID id;
	private SerialVector corner1, corner3;
	
	public Casino(DataManager data, Player person, ID i){
		System.out.println("Casino created for " + person.getName());
		database = data;
		owner = person;
		id = i;
		name = "";
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

	public Game createGame(GameType type, ID i) {
		Game newgame = null;
		switch (type){
		case SLOT_MACHINE:
			newgame = new SlotMachine(this, database, i);
			break;
		case ROULETTE_WHEEL:
			break;
		case SHELL_GAME:
			break;
		case CARDS_BLACKJACK:
			break;
		case TARGET_PRACTICE:
			break;
		}
		return newgame;
	}

	public boolean defineCasino(SerialVector vector) {
		if(corner1 == null){
			corner1 = vector;
			owner.sendMessage("Corner 1 defined. One left!");
		}
		else if(!vector.equals(corner1) && corner3 == null){
			SerialVector cornertemp = corner1;
			corner3 = new SerialVector(Vector.getMaximum(vector, corner1));
			corner1 = new SerialVector(Vector.getMinimum(corner1, vector));
			corner3.setY(0);
			corner1.setY(0);
			
			int area = getArea();
			if(area > 0){
				owner.sendMessage("Casino boundaries defined!");
				owner.sendMessage("  Total area: " + area + " Square Meters.");
			}
			else{
				owner.sendMessage("Casino area is 0! This means your blocks are in a line. \n" +
								  "  Use opposite corners. Take up space!");
				corner1 = cornertemp;
				corner3 = null;
			}
		}
		return complete();
	}
	
	public int getArea(){
		int length = corner3.getBlockX() - corner1.getBlockX();
		int width = corner3.getBlockZ() - corner1.getBlockZ();
		return length * width;
	}
	
	public boolean isInside(SerialVector coords){
		coords.setY(0);
		if (coords.isInAABB(corner1, corner3))
			return true;
		return false;
	}

	public boolean setName(String n) {
		name = n;
		owner.sendMessage("You Casino's name was set to: \"" + name + "\"");
		
		return complete();
	}

	private boolean complete() {
		if (corner1 != null && corner3 != null && name != ""){
			System.out.println("Casino finished: " + owner.getName() + " has a new casino. \n" +
							   "  Name:     " + name + "\n" +
							   "  Location: " + corner1.toString() + "\n" + 
							   "  Area:     " + getArea() + " Square Blocks");
			return true;
		}
		return false;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
	}
	
	public void reinitialize(DataManager db){
		database = db;
		owner = db.getPlugin().getServer().getPlayer(name);
	}
}
