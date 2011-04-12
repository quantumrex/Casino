package com.nhksos.quantumrex.Game;

import java.io.Serializable;

import com.nhksos.quantumrex.Casino.Casino;
import com.nhksos.quantumrex.Casino.DataManager;
import com.nhksos.quantumrex.Casino.ID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

enum MachineState {READY, RUNNING, WAITING, STOPPED, BROKEN}; 

public abstract class Game implements Serializable {
	
	private static final long serialVersionUID = -6944677438957693318L;
	
	private static int NextID = 17;
	public static final int NullID = -1;
	
	DataManager database;
	Casino owner;
	Player patron;
	final ID id;
	Block trigger;
	double payout;
	double payin;
	double JackPot;
	int multiplier;
	MachineState state;
	
	public static class GameIDAccess{
		private GameIDAccess(){}
		
		public void setNextID(int id){
			NextID = id;
		}
		public int getNextID(){
			return NextID++;
		}
	}
	
	public static void init(DataManager db){
		db.receiveKey(new GameIDAccess());
	}
	
	public Game(Casino casino, DataManager db, ID i) {
		owner = casino;
		database = db;
		patron = null;
		id = i;
		state = MachineState.BROKEN;
	}
	
	public boolean isReady(){
		return (state == MachineState.READY);
	}
	
	public abstract boolean enable(Player patron);
	
	public abstract boolean buildInteract(Block block);
	
	public abstract void playInteract(Block block);
	
	public abstract void testGame();
}