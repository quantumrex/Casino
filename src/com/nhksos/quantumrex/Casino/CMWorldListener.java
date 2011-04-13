package com.nhksos.quantumrex.Casino;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

public class CMWorldListener extends WorldListener {
	DataManager database;
	
	public CMWorldListener(DataManager db){
		database = db;
	}
	
	public void onWorldLoad(WorldLoadEvent event) {
		database.reinitialize();
	}
}
