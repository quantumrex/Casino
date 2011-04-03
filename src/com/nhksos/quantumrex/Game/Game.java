package com.nhksos.quantumrex.Game;

import com.nhksos.quantumrex.Casino.Casino;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

enum MachineState {READY, RUNNING, WAITING, STOPPED, BROKEN}; 

public abstract class Game {
	Casino owner;
	Block trigger;
	double payout;
	double payin;
	double JackPot;
	int multiplier;
	MachineState state;
	
	public Game(Casino casino) {
		owner = casino;
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
	
	public abstract void buildInteract(Block block);
	
	public abstract void testGame();
	
}
