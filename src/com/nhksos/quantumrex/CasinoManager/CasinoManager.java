/**
 * 
 */
package com.nhksos.quantumrex.CasinoManager;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CasinoManager Plugin
 * @author quantumrex
 *
 */
public class CasinoManager extends JavaPlugin implements Plugin {
	private CMPluginListener CMPluginListener = null;
    private iConomy iConomy = null;
    private Permissions Permissions = null;
    private boolean permissionsEnabled = false;
    private Server Server = null;
	private CMBlockListener blocky;
	private CMPlayerListener playery;

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
		Server = getServer();

        CMPluginListener = new CMPluginListener(this);

        // Event Registration
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, CMPluginListener, Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, CMPluginListener, Priority.Monitor, this);
	}

    public iConomy getiConomy() {
        return iConomy;
    }
    
    public boolean setiConomy(iConomy plugin) {
        if (iConomy == null) {
            iConomy = plugin;
        } else {
            return false;
        }
        return true;
    }

	public boolean setPermissions(Permissions plugin) {
		if(Permissions == null){
			Permissions = plugin;
			permissionsEnabled = true;
		}
		else 
			return false;
		return true;
	}
	
	public void disablePermissions(){
		if (Permissions != null){
			Permissions = null;
			permissionsEnabled = false;
		}
	}

	public Permissions getPermissions() {
		return Permissions;
	}
	
	public boolean permissionsEnabled(){
		return permissionsEnabled;
	}

}
