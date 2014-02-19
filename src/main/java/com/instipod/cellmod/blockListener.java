package com.instipod.cellmod;

// This file written by Michael (Instipod)

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class blockListener implements Listener {
    private final CellMod plugin;

    public blockListener(CellMod instance) {
        plugin = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.globaltos.containsKey(event.getBlock().getLocation())) {
            if (!plugin.hasPermission(event.getPlayer(), "cellmod.destroy")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + plugin.lang.getProperty("NoPermission"));
            } else {
                plugin.carriers.get(plugin.globaltos.get(event.getBlock().getLocation())).removeTower(event.getBlock().getLocation());
                plugin.globaltos.remove(event.getBlock().getLocation());
                plugin.runDeleteQuery("DELETE FROM towers WHERE BlockX='" + event.getBlock().getLocation().getX() + "' AND BlockY='" + event.getBlock().getLocation().getY() + "' AND BlockWorld='" + event.getBlock().getLocation().getWorld().getName() + "';");
                event.getPlayer().sendMessage(ChatColor.GREEN + plugin.lang.getProperty("TDestroyed"));
            }
        }
    }
}
