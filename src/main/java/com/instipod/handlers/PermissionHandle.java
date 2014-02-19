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
    
    public void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return;
        }
        permission = rsp.getProvider();
    }
    
    public boolean hasPermission(Player p, String perm) {
        return permission.has(p, perm);
    }
}
