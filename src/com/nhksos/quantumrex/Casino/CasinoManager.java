/**
 * 
 */
package com.nhksos.quantumrex.Casino;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CasinoManager Plugin
 * @author quantumrex
 *
 */
public class CasinoManager extends JavaPlugin implements Plugin {
	
	//Internal Components
	//=======================================================
	public DataManager database;
	private CMPluginListener pluginlistener = null;
	private CMBlockListener blistener = null;
	private CMPlayerListener plistener = null;
	private CMWorldListener wlistener = null;
	
	//External Dependencies
	//=======================================================
    private iConomy iconomy = null;
    private PermissionHandler security = null;
    private boolean penabled = false;

    /**Methods Required by JavaPlugin
     * =======================================================
     *  
     */
    
	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		database.finalize();
			System.out.println("[CasinoManager] Plugin disabled...");
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
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
			sender.sendMessage("Casino plugin is disabled. Waiting for iConomy, probably.");
			System.out.println("[CasinoManager] Plugin is disabled. Waiting for iConomy, probably.");
		}
        return false;
    }

	/**iConomy Methods
	 * =======================================================
	 * @return
	 */
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
    
	/**PermissionHandler Methods
	 * =======================================================
	 * @param handler
	 * @return
	 */
	public boolean setPermissions(PermissionHandler handler) {
		if(security == null){
			security = handler;
			penabled = true;
		}
		else 
			return false;
		return true;
	}	
	public void disablePermissions(){
		if (security != null){
			security = null;
			penabled = false;
		}
	}
	public PermissionHandler getPermissions() {
		return security;
	}
	public boolean permissionsEnabled(){
		return penabled;
	}
}
