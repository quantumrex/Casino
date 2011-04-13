package com.nhksos.quantumrex.Game;

import java.io.Serializable;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nhksos.quantumrex.Casino.Casino;
import com.nhksos.quantumrex.Casino.DataManager;
import com.nhksos.quantumrex.Casino.ID;
import com.nhksos.quantumrex.Casino.SerialVector;



public abstract class Game implements Serializable{
	
	private static final long serialVersionUID = 5548425295617698713L;

	public enum MachineState {READY, RUNNING, WAITING, STOPPED, BROKEN, UNINITIALIZED}; 
	
	private static int NextID = 17;
	public static final int NullID = -1;
	
	transient DataManager database;
	transient Casino owner;
	transient Player patron;
	
	final ID id;
	final ID cowner;
	double payout;
	double payin;
	double jackpot;
	int multiplier;
	MachineState state;
	SerialVector trigger;
	
	public static class GameIDAccess{
		private GameIDAccess(){}
		
		public void setNextID(int id){
			NextID = id;
		}
		public int getNextID(){
			return NextID++;
		}
	}
	
	public static final void init(DataManager db){
		db.receiveKey(new GameIDAccess());
	}
	
	public Game(Casino casino, DataManager db, ID i) {
		owner = casino;
		cowner = casino.id;
		database = db;
		patron = null;
		id = i;
		state = MachineState.BROKEN;
	}
	
	public MachineState getState(){
		return state;
	}
	
	public abstract void enable(Player patron);
	
	public abstract boolean buildInteract(Block block);
	
	public abstract void playInteract(Block block);
	
	public abstract void testGame();
	
	public void reinitialize(DataManager db) {
		// TODO Auto-generated method stub
		database = db;
		owner = db.getCasino(cowner);
		state = MachineState.READY;
	}
}