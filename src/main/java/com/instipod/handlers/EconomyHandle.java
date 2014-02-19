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
    
    public boolean setup()
    {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
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
