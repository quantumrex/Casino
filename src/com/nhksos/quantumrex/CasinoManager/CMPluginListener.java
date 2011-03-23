package com.nhksos.quantumrex.CasinoManager;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 * 
 * Source from:
 * http://wiki.nexua.org/IConomy/API/Setup
 */
public class CMPluginListener extends ServerListener {
	private final CasinoManager CasinoManager;
    public CMPluginListener(Plugin CM) {CasinoManager = (CasinoManager)CM; }

    @Override
    public void onPluginEnabled(PluginEvent event) {
        if(event.getType() == Event.Type.PLUGIN_ENABLE){
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
        	if(event.getPlugin().getDescription().getName() == "Permissions"){
        		System.out.println("[CasinoManager] Permissions enabled. Using advanced permissions...");
        		if(!CasinoManager.permissionsEnabled())
        			CasinoManager.setPermissions((Permissions)event.getPlugin());
        	}
            if(event.getPlugin().getDescription().getName() == "Help"){
            	System.out.println("[CasinoManager] Help entries registered...");
            }
        }
        else if(event.getType() == Event.Type.PLUGIN_DISABLE){
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
}