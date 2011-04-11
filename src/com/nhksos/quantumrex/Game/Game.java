package com.nhksos.quantumrex.Game;

import com.nhksos.quantumrex.Casino.Casino;
import com.nhksos.quantumrex.Casino.DataManager;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

enum MachineState {READY, RUNNING, WAITING, STOPPED, BROKEN}; 

public abstract class Game {
	private static int NextID = 0;
	public static final int NullID = -1;
	
	DataManager database;
	Casino owner;
	int ID;
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
	
	public Game(Casino casino, DataManager db) {
		owner = casino;
		database = db;
		state = MachineState.BROKEN;
	}
	
	public Game(Casino casino, Block key){
		owner = casino;
		trigger = key;
	}

	public final MachineState getState(){
		return state;
	}
	
	public boolean isComplete(){
		return (state == MachineState.READY);
	}
	
	public abstract boolean enable(Player patron);
	
	public abstract boolean buildInteract(Block block);
	
	public abstract void testGame();

	public void setID(int using) {
		ID = using;
	}
}
