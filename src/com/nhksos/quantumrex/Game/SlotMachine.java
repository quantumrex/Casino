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
				return stop();
			}
			else{
				spinning = true;
				taskId = database.getScheduler().scheduleAsyncRepeatingTask(database.getPlugin(), this, 0L, 7L);
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
				database.getScheduler().cancelTask(taskId);
				return true;
			}
			else{
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

	@Override
	public boolean buildInteract(Block block) {
		if (trigger == null){
			switch(block.getType()){
			case STONE_BUTTON:
			case STONE_PLATE:
			case WOOD_PLATE:
			case LEVER:
				owner.owner.sendMessage("Trigger set!");
				trigger = block;
				database.registerActivator(block.getLocation().toVector(), id);
				break;
			default:
				owner.owner.sendMessage("Not a valid block for trigger...");
			}
		}
		else if (slot == null){
			slot = new Spinner(this, block);
			if (slot.ready()){
				state = MachineState.READY;
				if (trigger != null)
					return true;
			}
			else{
				slot = null;
			}
		}
		
		return false;
	}

	@Override
	public boolean enable(Player p) {
		patron = p;
		if (state == MachineState.READY){
			state = MachineState.RUNNING;
			slot.spin();
			return true;
		}
		return false;
	}

	@Override
	public void playInteract(Block block) {
		if (state == MachineState.RUNNING && block.equals(trigger)){
			state = MachineState.READY;
			slot.stop();
			database.finishGame(patron.getName());
		}
		else
			patron.sendMessage("Not my switch, not my problem.");
	}

	@Override
	public void testGame() {
		enable(null);
	}

}

