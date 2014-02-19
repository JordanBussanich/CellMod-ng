package com.instipod.cellmod;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class UnifiedListener implements Listener {
    private CellMod plugin;
    
    public UnifiedListener(CellMod instance) {
        instance.getServer().getPluginManager().registerEvents(this, instance);
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.globaltos.containsKey(event.getBlock().getLocation())) {
            if (!plugin.hasPermission(event.getPlayer(), "cellmod.destroy")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
            } else {
                plugin.carriers.get(plugin.globaltos.get(event.getBlock().getLocation())).removeTower(event.getBlock().getLocation());
                plugin.globaltos.remove(event.getBlock().getLocation());
                plugin.runDeleteQuery("DELETE FROM towers WHERE BlockX='" + event.getBlock().getLocation().getX() + "' AND BlockY='" + event.getBlock().getLocation().getY() + "' AND BlockWorld='" + event.getBlock().getLocation().getWorld().getName() + "';");
                event.getPlayer().sendMessage(ChatColor.GREEN + plugin.languageConfig.getString("TDestroyed"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            Boolean neednum = true;
            ResultSet checknum = plugin.getResult("SELECT * FROM players WHERE Player='" + event.getPlayer().getName() + "';");
            try {
                while(checknum.next()) {
                     neednum = false;
                }
                if (neednum == true) {
                    String num = generateNumber(plugin.mainConfig.getInt("phone.number-length"));
                    plugin.runInsertQuery("INSERT INTO players (Player, Number, Changed, Carrier, Plan) VALUES ('" + event.getPlayer().getName() + "', '" + num + "', 'false', '', '0');");
                }
            } catch (Exception ex) {
                
            }
        if (plugin.hasPermission(player, "cellmod.use.cell")) {
            String noservice = plugin.languageConfig.getString("NoService") + " [    ]";
            if (!noservice.equals(plugin.getSignal(event.getPlayer(), 0.0))) {
                ResultSet getmessages = plugin.getResult("SELECT * FROM backlog WHERE ToUser='" + event.getPlayer().getName() + "';");
            try {
                while(getmessages.next()) {
                     event.getPlayer().sendMessage(plugin.languageConfig.getString("Header"));
            event.getPlayer().sendMessage(plugin.getPlayerCarrier(event.getPlayer()).getName() + " " + plugin.getSignal(event.getPlayer(), 0.0));
            ResultSet fromnumber = plugin.getResult("SELECT * from players WHERE Player='" + getmessages.getString("FromUser") + "';");
            String fromnum = "";
            try {
                while(fromnumber.next()) {
                    fromnum = fromnumber.getString("Number");
                }
            } catch (Exception ex) {
                fromnum = "0000";
            }
            event.getPlayer().sendMessage(plugin.languageConfig.getString("From") + ": " + plugin.getServer().getPlayer(getmessages.getString("FromUser")).getName() + " (" + fromnum + ")");
            event.getPlayer().sendMessage(getmessages.getString("Text"));
            plugin.runDeleteQuery("DELETE FROM backlog WHERE Text='" + getmessages.getString("Text") + "';");
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
            }
        }
    }
    public String generateNumber(int charLength) {
        String num = String.valueOf(charLength < 1 ? 0 : new Random().nextInt((9 * (int) Math.pow(10, charLength - 1)) - 1) + (int) Math.pow(10, charLength - 1));
        
        try {
            ResultSet getplayerfromnumber = plugin.getResult("SELECT * FROM players WHERE Number='" + num + "';");
            if (getplayerfromnumber.getFetchSize() > 0) {
                return generateNumber(charLength);
            }
        } catch (Exception ex) { 
            //this should not happen
        }
        
        if (!num.equals(plugin.mainConfig.getString("phone.specialnumbers.carrier")) && !num.equals(plugin.mainConfig.getString("phone.specialnumbers.emergency"))) {
            return num;
        } else {
            return generateNumber(charLength);
        }
    }
}