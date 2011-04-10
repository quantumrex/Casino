/**
 * 
 */
package com.nhksos.quantumrex.Game;

import com.nhksos.quantumrex.Casino.Casino;
import com.nhksos.quantumrex.Casino.DataManager;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;

/**
 * @author rwenner
 *
 */
public class SlotMachine extends Game {
	Spinner slot;
	
	private class Spinner implements Runnable{
		int taskId;
		SlotMachine parent;
		private Block wheel;
		private boolean spinning;
		
		public Spinner(SlotMachine game, Block spin){
			taskId = -1;
			parent = game;
			if (spin.getType() == Material.WOOL)
				wheel = spin;
			else{
				System.out.println("This is not a valid spinner block!!!");
				wheel = null;
			}
		}
		
		public boolean spin(){
			// TODO Allow user communication
			if (spinning){
				System.out.println("Spinner is already running, stopping...");
				return stop();
			}
			else{
				spinning = true;
				//taskId = main.getServer().getScheduler().scheduleAsyncRepeatingTask(main, this, 0L, 7L);
				if (taskId == -1){
					System.out.println("Could not start spinner...");
					return false;
				}
				return true;
			}
		}
		
		public boolean stop(){
			if (spinning){
				spinning = false;
				//main.getServer().getScheduler().cancelTask(taskId);
				return true;
			}
			else{
				System.out.println("Spinner is stopped. Starting...");
				return spin();
			}
		}
		
		public DyeColor value(){
			return DyeColor.getByData(wheel.getData());
		}

		@Override
		public void run(){
			byte color = (byte)(wheel.getData() + 0x1);
			if (color > 0xF)
				color = 0x0;
			wheel.setData(color);
		}
		
		public boolean ready(){
			return (wheel != null);
		}
	}

	public SlotMachine(Casino casino, DataManager db) {
		super(casino, db);
	}
	
	public SlotMachine(Casino casino, Block key){
		super(casino, key);
		slot = null;
	}

	@Override
	public boolean enable(Player patron) {
		System.out.println("Game starting!");
		if (state == MachineState.READY){
			System.out.println("Game starting!");
			state = MachineState.RUNNING;
			slot.spin();
			return true;
		}
		else if (state == MachineState.RUNNING){
			System.out.println("Game stopping!");
			state = MachineState.READY;
			slot.stop();
			return true;
		}
		return false;
	}

	@Override
	public boolean buildInteract(Block block) {
		if (slot == null){
			slot = new Spinner(this, block);
			if (slot.ready()){
				state = MachineState.READY;
				if (trigger != null)
					return true;
				else
					return false;
			}
			else{
				slot = null;
				return false;
			}
		}
		else if (trigger == null){
			switch(block.getType()){
			case STONE_BUTTON:
			case STONE_PLATE:
			case WOOD_PLATE:
			case LEVER:
				System.out.println("Trigger set!");
				trigger = block;
				return true;
			default:
				System.out.println("Not a valid block for trigger...");
				return false;
			}
		}
		else 
			return false;
	}

	@Override
	public void testGame() {
		enable(null);
	}

}

