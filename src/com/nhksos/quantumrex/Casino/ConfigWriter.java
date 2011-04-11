package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import org.bukkit.util.Vector;

import com.nhksos.quantumrex.Game.Game;

public class ConfigWriter {
	
	DataManager database;
	
	public ConfigWriter(DataManager parent){
		database = parent;
	}

	public HashMap<Vector, ID> readActivators() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, ID> readOwners() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<ID, Casino> readCasinos() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<ID, Game> readGames() {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeActivators(HashMap<Vector, ID> activators) {
		// TODO Auto-generated method stub
		
	}

	public void writeOwners(HashMap<String, ID> owners) {
		// TODO Auto-generated method stub
		
	}

	public void writeCasinos(HashMap<ID, Casino> casinos) {
		// TODO Auto-generated method stub
		
	}

	public void writeGames(HashMap<ID, Game> games) {
		// TODO Auto-generated method stub
		
	}

}
