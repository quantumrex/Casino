package com.nhksos.quantumrex.Casino;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CasinoManager is the basic plugin management system for the casino
 * system. It contains all functions that involve other plugins, and 
 * methods of interacting with, and sending data to, those plugins.
 * <br><br>
 * Currently, we have support for:
 * <ul> 
 * 		<li><b>iConomy</b> - the basic economy plugin. This plugin allows 
 * 				us to change a player's balance when he bets on a machine and wins.
 * 		</li>
 * 		<li><b>Permissions</b> - This plugin will allow us to control who on the 
 * 				server has access to the casino system. I plan to split up 
 * 				permissions into:
 * 			<ul>
 * 			<li>owners - people who can create casinos</li>
 * 			<li>players - people who can play games</li> 
 * 			</ul>
 * 				These permissions are completely independent. An owner is not 
 * 				automatically able to play games. This should hopefully prevent 
 * 				players from making intentionally broken machines that always 
 * 				pay out to increase their bank accounts.
 * 		</li>
 * 		<li><b>Help</b> - This is just a standard helper plugin. I don't really 
 * 				understand it at the moment, but I may implement support later.
 * 		</li>
 * </ul>
 *                  
 * @author quantumrex
 */
public class CasinoManager extends JavaPlugin implements Plugin {
	
	//Internal Components
	//=======================================================
	private DataManager database;
	private CMPluginListener pluginlistener = null;
	@SuppressWarnings("unused")
	private CMBlockListener blistener = null;
	private CMPlayerListener plistener = null;
	private CMWorldListener wlistener = null;
	
	//External Dependencies
	//=======================================================
    private iConomy iconomy = null;
    @SuppressWarnings("unused")
	private Permissions permissions;
    private static PermissionHandler security = null;
    private static boolean penabled = false;

	/** 
	 * This is the standard onDisable() command inherited from Bukkit.
	 * It monitors the closing of the DataManager, and reports on its 
	 * success or failure.
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		System.out.println("[CasinoManager] Disabling plugin...");
		if (database.stop())
			System.out.println("[CasinoManager] Plugin disabled Successfully!");
		else
			System.out.println("[CasinoManager] Encountered an error while exiting...");
	}

	/** 
	 * This method creates a DataManager for the plugin, and initializes the 
	 * listeners that the plugin requires. It then registers the events
	 * this plugin listens to. If this method encounters any errors,
	 * it disables the plugin and reports that there was an error.
	 *  
	 * I assume that if an error is encountered, it is during the 
	 * loading of the database. So, rather than allowing the program 
	 * to overwrite the previous successful database, the program 
	 * halts, and it is up to the server administrator to discover 
	 * the cause.
	 */
	@Override
	public void onEnable() {
		System.out.println("[CasinoManager] Enabling plugin...");
		try{
			database = new DataManager(this);
	        pluginlistener = new CMPluginListener(this);
	        
	        plistener = new CMPlayerListener(database);
	        wlistener = new CMWorldListener(database);

	        // Event Registration

	        getServer().getPluginManager().registerEvent(Event.Type.WORLD_LOAD, wlistener, Priority.Normal, this);
	        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, plistener, Priority.Normal, this);
	        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, plistener, Priority.Normal, this);
	        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, pluginlistener, Priority.Monitor, this);
	        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, pluginlistener, Priority.Monitor, this);
	        System.out.println("[CasinoManager] Plugin Enabled Successfully!");
		}
		catch (Exception e){
	        System.out.println("[CasinoManager] Encountered an error while loading.");
	        if (database.config.getBoolean("global.debug", false))
	        	e.printStackTrace();
	        getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	/** 
	 * Almost all commands supported by this plugin are actually handled 
	 * by the DataManager, because that is where most of the global data 
	 * is actually stored and maintained. Commands are passed to it thru
	 * this method. 
	 * In essence, this method checks whether the plugins required to 
	 * perform the command are available, and if so, the command is 
	 * executed.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isEnabled()){
			if(sender instanceof Player){
				Player player = (Player) sender;
				if (command == getCommand("makecasino")){
					database.registerJob(player, JobType.CASINO_CREATE, args);
				}
				else if(command == getCommand("makemachine")){
					database.registerJob(player, JobType.GAME_CREATE, args);
				}
				else if (command == getCommand("testcasino")){
					// TODO test casino
					database.testCasino(player.getName());
				}
				else if (command == getCommand("destroycasino")){
					database.destroyCasino(player.getName());
				}
			}
			else
				System.out.println("[CasinoManager] You can't do that from here!");
			return true;
		}
		else{
			sender.sendMessage("CasinoManager plugin is not disabled. Waiting for iConomy, probably.");
			System.out.println("[CasinoManager] Plugin is disabled. Waiting for iConomy, probably.");
		}
        return false;
    }

	
    public iConomy getiConomy() {
        return iconomy;
    }
    public boolean setiConomy(iConomy plugin) {
        if (iconomy == null) {
            iconomy = plugin;
        } else {
            return false;
        }
        return true;
    }
    
	
	public void setPermissions(Permissions p) {
		permissions = p;
		security = p.getHandler();
		penabled = true;
	}
	
	public void disablePermissions(){
		permissions = null;
		security = null;
		penabled = false;
	}
	public static PermissionHandler getPermissions() {
		return security;
	}
	public static boolean hasPermission(Player player, String permission){
		if(penabled)
			return security.has(player, permission);
		else
			return player.isOp();
	}
}
