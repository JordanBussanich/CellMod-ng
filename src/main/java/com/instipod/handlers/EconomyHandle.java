package com.instipod.handlers;

import com.instipod.cellmod.CellMod;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandle {
    private Economy econ = null;
    private CellMod plugin;
    
    public EconomyHandle(CellMod instance) {
        plugin = instance;
    }
    
    public void setup()
    {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }
    
    public Boolean hasMoney(Player p, Double amount) {
        if (econ != null) {
            return econ.has(p.getName(), amount);
        } else {
            return true;
        }
    }
    public void takeMoney(Player p, Double amount) {
        if (econ != null) {
            econ.withdrawPlayer(p.getName(), amount);
        }
    }
}
