/**
 * 
 */
package com.nhksos.quantumrex.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.nhksos.quantumrex.Casino.Casino;
import com.nhksos.quantumrex.Casino.DataManager;
import com.nhksos.quantumrex.Casino.ID;
import com.nhksos.quantumrex.Casino.SerialVector;

import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Material;

/**
 * @author rwenner
 *
 */
public class SlotMachine extends Game implements Serializable{
	
	private static final long serialVersionUID = -7156565210635169241L;
	
	transient Spinner slot;
	
	private class Spinner implements Runnable{
		transient int taskId;
		SerialVector wvector;
		transient Block wheel;
		transient boolean spinning;
		
		public Spinner(SlotMachine game, Block spin){
			taskId = -1;
			if (spin.getType() == Material.WOOL){
				wheel = spin;
				wvector = new SerialVector(wheel.getLocation().toVector());
			}
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
		
		@SuppressWarnings("unused")
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

	public SlotMachine(Casino casino, DataManager db, ID i) {
		super(casino, db, i);
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
				tvector = new SerialVector(trigger.getLocation().toVector());
				database.registerActivator(tvector, id);
				owner.owner.sendMessage("This gametype now needs a single wool block spinner.");
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
	public void enable(Player p) {
		patron = p;
		state = MachineState.RUNNING;
		slot.spin();
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
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		slot.wvector = (SerialVector) in.readObject();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
	}
	
	@Override
	public void reinitialize(DataManager db){
		super.reinitialize(db);
		slot.wheel = slot.wvector.toLocation(db.getWorld()).getBlock();
		slot.taskId = -1;
		slot.spinning = false;
	}
}

