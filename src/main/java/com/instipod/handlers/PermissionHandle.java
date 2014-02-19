package com.instipod.handlers;

import com.instipod.cellmod.CellMod;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionHandle {
    private Permission permission = null;
    private CellMod plugin;
    
    public PermissionHandle(CellMod instance) {
        this.plugin = instance;
    }
    
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    
    public boolean hasPermission(Player p, String perm) {
        return permission.has(p, perm);
    }
}
