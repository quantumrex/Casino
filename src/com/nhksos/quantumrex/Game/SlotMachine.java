/**
 * 
 */
package com.nhksos.quantumrex.Game;

import com.nhksos.quantumrex.Casino.Casino;

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
		Plugin main;
		SlotMachine parent;
		private Block wheel;
		private boolean spinning;
		
		public Spinner(SlotMachine game, Block spin){
			taskId = -1;
			parent = game;
			main = parent.owner.plugin;
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
				taskId = main.getServer().getScheduler().scheduleAsyncRepeatingTask(main, this, 0L, 10L);
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
				main.getServer().getScheduler().cancelTask(taskId);
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

	public SlotMachine(Casino casino) {
		super(casino);
	}
	
	public SlotMachine(Casino casino, Block key){
		super(casino, key);
		slot = null;
	}

	@Override
	public boolean enable(Player patron) {
		if (state == MachineState.READY){
			state = MachineState.RUNNING;
			slot.spin();
			return true;
		}
		else if (state == MachineState.RUNNING){
			state = MachineState.READY;
			slot.stop();
			return true;
		}
		return false;
	}

	@Override
	public void buildInteract(Block block) {
		if (slot == null){
			slot = new Spinner(this, block);
			if (slot.ready())
				state = MachineState.READY;
			else
				slot = null;
		}
	}

	@Override
	public void testGame() {
		enable(null);
	}

}

