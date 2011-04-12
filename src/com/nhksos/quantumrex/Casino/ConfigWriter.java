package com.nhksos.quantumrex.Casino;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;

import com.nhksos.quantumrex.Game.Game;

public class ConfigWriter {
	private final File folder = new File("plugins", "CasinoManager");
	private final String configname = "config.yml";
	private final File configfile = new File(folder, configname);
	
	private DataManager database;
	private FileInputStream fis;
	private FileOutputStream fos;
	private ObjectInputStream ois;
	ObjectOutputStream oos;
	Configuration config;
	
	public ConfigWriter(DataManager parent){
		database = parent;
		folder.mkdir();
		if (configfile.exists())
			config = new Configuration(configfile);
		else{
			System.out.println("[CasinoManager] Config file is missing from: " + 
							    configfile.getPath());
			System.out.println("[CasinoManager] Creating the default config file.");
			try {
				configfile.createNewFile();
				InputStream res = DataManager.class.getResourceAsStream("/config.yml");
				FileWriter tx = new FileWriter(configfile);
				try {
					for (int i = 0; (i = res.read()) > 0;)
						tx.write(i);
				} finally {
					tx.flush();
					tx.close();
					res.close();
				}
				config = new Configuration(configfile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		readConfig();
	}
	
	private void readConfig(){
		config.load();
	}

	public HashMap<SerialVector, ID> readActivators() {
		return readHash(config.getString("global.objects.path.activators", "activators.sav"));
	}

	public HashMap<String, ID> readOwners() {
		return readHash(config.getString("global.objects.path.owners", "owners.sav"));
	}

	public HashMap<ID, Casino> readCasinos() {
		return readHash(config.getString("global.objects.path.casinos", "casinos.sav"));
	}

	public HashMap<ID, Game> readGames() {
		return readHash(config.getString("global.objects.path.games", "games.sav"));
	}

	public HashMap<String, Stats> getStats() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void writeConfig() {
		config.save();
	}
	
	public void writeActivators(HashMap<SerialVector, ID> activators) {
		writeHash(activators, config.getString("global.objects.path.activators", "activators.sav"));
	}

	public void writeOwners(HashMap<String, ID> owners) {
		writeHash(owners, config.getString("global.objects.path.owners", "owners.sav"));
	}

	public void writeCasinos(HashMap<ID, Casino> casinos) {
		writeHash(casinos, config.getString("global.objects.path.casinos", "casinos.sav"));
	}

	public void writeGames(HashMap<ID, Game> games) {
		writeHash(games, config.getString("global.objects.path.games", "games.sav"));
	}

	public void writeStats(HashMap<String, Stats> stats) {
		
	}
	
	@SuppressWarnings("unchecked")
	private <T,U> HashMap<T, U> readHash(String fname){
		HashMap<T, U> map = new HashMap<T, U>();
		File newfile = new File(folder, fname);
		try {
			fis = new FileInputStream(newfile);
			ois = new ObjectInputStream(fis);
			map = (HashMap<T, U>)ois.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("[CasinoManager] Creating new file: " + fname);
			try {
				newfile.createNewFile();
				fos = new FileOutputStream(newfile);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(map);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return map;
	}

	private <T, U> void writeHash(HashMap<T, U> map, String fname) {
		
		File newfile = new File(folder, fname);
		try {
			fos = new FileOutputStream(fname);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(map);
		} catch (FileNotFoundException e) {
			System.out.println("[CasinoManager] File: " + fname + " should already exist...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
