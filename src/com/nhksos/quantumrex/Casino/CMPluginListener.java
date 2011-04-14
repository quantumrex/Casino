package com.nhksos.quantumrex.Casino;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Checks for plugins whenever one is enabled
 * 
 * Original Source from:
 * http://wiki.nexua.org/IConomy/API/Setup
 */
public class CMPluginListener extends ServerListener{
	//Necessary Components
	private final CasinoManager parent;
	
    public CMPluginListener(CasinoManager CM) {
    	parent = CM;
    }
    
    public boolean hookPlugins(){
		hookHelp();
		hookPermissions();
    	return hookiConomy();
    }
    
    private void hookHelp() {
		
	}

	private void hookPermissions() {
        parent.setPermissions((Permissions)parent.getServer().getPluginManager().getPlugin("Permissions"));
	}

	private boolean hookiConomy() {
		if(parent.getiConomy() == null) {
            iConomy dependency = (iConomy) parent.getServer().getPluginManager().getPlugin("iConomy");
            if (dependency != null) {
                if(dependency.isEnabled()) {
                    parent.setiConomy(dependency);
                    System.out.println("[CasinoManager] Successfully linked with iConomy.");
                    return true;
                }
            }
        }
		return false;
	}

	@Override
    public void onPluginEnable(PluginEnableEvent event) {
    	//Register with iConomy
    	if(event.getPlugin().getDescription().getName() == "iConomy"){
    		if (!parent.isEnabled()){
    			parent.getPluginLoader().enablePlugin(parent);
    		}
    		hookiConomy();
    	}
    	//Register with Permissisons
    	if(event.getPlugin().getDescription().getName().equals("Permissions")) {
    		hookPermissions();
            System.out.println("[CasinoManager] Linked with Permissions.");
        }
    	//Register with Help
        if(event.getPlugin().getDescription().getName() == "Help"){
        	System.out.println("[CasinoManager] Help entries registered.");
        }
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event){
    	//Disable Casino if iConomy dies
    	if(event.getPlugin().getDescription().getName() == "iConomy"){
    		System.out.println("[CasinoManager] iConomy disabled. Shutting down...");
    		parent.getPluginLoader().disablePlugin(parent);
    	}
    	
    	else if(event.getPlugin().getDescription().getName() == "Permissions"){
    		System.out.println("[CasinoManager] Permissions disabled. Defaulting to Op only commands...");
    		parent.disablePermissions();
    	}
    	else if(event.getPlugin().getDescription().getName() == "Help"){
    		System.out.println("[CasinoManager] Help integration disabled...");
    	}
    }
}