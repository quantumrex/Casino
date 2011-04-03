package com.nhksos.quantumrex.Casino;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 * 
 * Original Source from:
 * http://wiki.nexua.org/IConomy/API/Setup
 */
public class CMPluginListener extends ServerListener{
	//Necessary Components
	private final CasinoManager CasinoManager;
	
	/**
	 * 
	 * @param CM
	 */
    public CMPluginListener(CasinoManager CM) {
    	CasinoManager = CM;
    }
    
    /**
     * 
     */
    @Override
    public void onPluginEnable(PluginEvent event) {
    	//Register with iConomy
    	if(event.getPlugin().getDescription().getName() == "iConomy"){
        	iConomy dependency;
    		if (!CasinoManager.isEnabled()){
    			CasinoManager.getPluginLoader().enablePlugin(CasinoManager);
    		}
    		if(CasinoManager.getiConomy() == null) {
                dependency = (iConomy)event.getPlugin();

                if (dependency != null) {
                    if(dependency.isEnabled()) {
                        CasinoManager.setiConomy((iConomy)dependency);
                        System.out.println("[CasinoManager] Successfully linked with iConomy.");
                    }
                }
            }
    	}
    	//Register with Permisisons
    	if(event.getPlugin().getDescription().getName().equals("Permissions")) {
            CasinoManager.setPermissions(((Permissions)event.getPlugin()).Security);
            System.out.println("[CasinoManager] Linked with Permissions.");
        }
    	//Register with Help
        if(event.getPlugin().getDescription().getName() == "Help"){
        	System.out.println("[CasinoManager] Help entries registered.");
        }
    }
    
    @Override
    public void onPluginDisable(PluginEvent event){
    	//Disable Casino if iConomy dies
    	if(event.getPlugin().getDescription().getName() == "iConomy"){
    		System.out.println("[CasinoManager] iConomy disabled. Shutting down...");
    		CasinoManager.getPluginLoader().disablePlugin(CasinoManager);
    	}
    	
    	else if(event.getPlugin().getDescription().getName() == "Permissions"){
    		System.out.println("[CasinoManager] Permissions disabled. Defaulting to Op only commands...");
    		CasinoManager.disablePermissions();
    	}
    	else if(event.getPlugin().getDescription().getName() == "Help"){
    		System.out.println("[CasinoManager] Help integration disabled...");
    	}
    }
}