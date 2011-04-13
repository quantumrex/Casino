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
		int taskID;
		SerialVector wvector;
		Block wheel;
		boolean spinning;
		
		protected Spinner(SerialVector v){
			wvector = v;
			taskID = -1;
			spinning = false;
			wheel = null;
		}
		
		public Spinner(SlotMachine game, Block spin){
			taskID= -1;
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
			if(ready()){
				taskID = database.getScheduler().scheduleAsyncRepeatingTask(database.getPlugin(), this, 0L, 7L);
				if (taskID == -1){
					try{
						patron.sendMessage("Could not start spinner...");
					}
					catch(NullPointerException e) {}
					return false;
				}
				else{
					spinning = true;
					return true;
				}
			}
			else{
				patron.sendMessage("Spinner is not initialized. Attempting repairs...");
				try{
					wheel = database.getBlock(wvector, owner.world);
					patron.sendMessage("Success!");
					return spin();
				}
				catch(NullPointerException e){
					patron.sendMessage("There has been an error. The database is not initialized for this game...");
					return false;
				}
			}
		}
		
		public boolean stop(){
			if (spinning){
				spinning = false;
				database.getScheduler().cancelTask(taskID);
				return true;
			}
			return true;
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
		slot = null;
	}

	@Override
	public boolean buildInteract(Block block) {
		if(owner.worldCheck(block.getWorld().getName())){
			if (trigger == null){
				switch(block.getType()){
				case STONE_BUTTON:
				case STONE_PLATE:
				case WOOD_PLATE:
				case LEVER:
					trigger = new SerialVector(block.getLocation().toVector());
					
					database.getPlayer(owner.owner).sendMessage("Trigger set!");
					database.registerActivator(trigger, id);
					database.getPlayer(owner.owner).sendMessage("This gametype now needs a single wool block spinner.");
					break;
				default:
					database.getPlayer(owner.owner).sendMessage("Not a valid block for trigger...");
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
		}
		else
			database.getPlayer(owner.owner).sendMessage("You're currently in a different world than your casino. Fix it.");
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
		if (state == MachineState.RUNNING && trigger.equals(block)){
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
		slot = new Spinner((SerialVector) in.readObject());
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
		out.writeObject(slot.wvector);
	}
	
	@Override
	public void reinitialize(DataManager db){
		super.reinitialize(db);
		slot.wheel = slot.wvector.toLocation(db.getWorld(owner.world)).getBlock();
	}
	
	@Override
	public String toString(){
		String string = super.toString() + 
		"Spinner = " + slot.wvector.toString();
		return string;
	}
}

