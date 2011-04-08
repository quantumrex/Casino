/**
 * 
 */
package com.nhksos.quantumrex.Casino;

import java.util.HashMap;

import com.nhksos.quantumrex.Game.Game;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

enum GameType{SLOT_MACHINE, ROULETTE_WHEEL, SHELL_GAME, BLACKJACK, TARGET_GAME};

/**
 * CasinoManager Plugin
 * @author quantumrex
 *
 */
public class CasinoManager extends JavaPlugin implements Plugin {
	//Basic Data Strings
	//=======================================================
	private final String ConfigFile = "config.yml";
	private final String DefaultDirectory = "CasinoManager";
	private final String CurrentRelease = "0.01";
	
	//Internal Components
	//=======================================================
	public static PluginDescriptionFile description;
	private CMPluginListener pluginlistener = null;
	private CMBlockListener blistener = null;
	private CMPlayerListener plistener = null;
	private HashMap<String, Casino> casinos;
	
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
		System.out.println("[CasinoManager] Plugin disabled...");
	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
        pluginlistener = new CMPluginListener(this);
        plistener = new CMPlayerListener(this);
        casinos = new HashMap<String, Casino>();

        //Command Registration
        getCommand("makecasino").setExecutor(this);
        getCommand("makemachine").setExecutor(this);
        getCommand("test").setExecutor(this);
        getCommand("destroycasino").setExecutor(this);

        // Event Registration
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, plistener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, pluginlistener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, pluginlistener, Priority.Monitor, this);
        System.out.println("[CasinoManager] Plugin Enabled Successfully!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(isEnabled()){
			if (command == getCommand("makecasino")){
				System.out.println("[CasinoManager] makecasino received!");
				if (sender instanceof Player){
					Player person = (Player) sender;
					if (plistener.setEnabled(person)){
						Casino casino = new Casino(this, person);
						casinos.put(person.getName(), casino);
						if(!plistener.setJob(JobType.CASINO_CREATE, casino)){
							person.chat("Something bad just happened...");
							return false;
						}
					}
					else
						person.chat("The casino manager is currently in use, try again later.");
				}
				return true;
			}
			else if(command == getCommand("makemachine")){
				System.out.println("[CasinoManager] makemachine received!");
				if (sender instanceof Player){
					Player person = (Player) sender;
					if (plistener.setEnabled(person)){
						Casino casino = casinos.get(person.getName());
						if (casino != null){
							Game tomake = casino.createGame(GameType.SLOT_MACHINE);
							if (!plistener.setJob(JobType.GAME_CREATE, casino, tomake)){
								person.chat("Something bad just happened...");
								return false;
							}
						}
						else
							person.chat("You don't seem to have a casino. Use /makecasino.");		
					}	
					else
						person.chat("The casino manager is currently in use, try again later.");
				}
				return true;
			}
			else if (command == getCommand("test")){
				// TODO destroy game
				return true;
			}
			else if (command == getCommand("destroycasino")){
				// TODO destroy game
				return true;
			}
		}
		else
			// TODO player chat
			System.out.println("[CasinoManager] Plugin is disabled. Waiting for iConomy, probably.");
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
