package com.instipod.cellmod;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class playerListener implements Listener {
    private final CellMod plugin;
    public Random pn = new Random();

    public playerListener(CellMod instance) {
        plugin = instance;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
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
                    String num = Integer.toString(pn.nextInt(100));
                    plugin.runInsertQuery("INSERT INTO players (Player, Number, Changed, Carrier, Plan) VALUES ('" + event.getPlayer().getName() + "', '" + num + "', 'false', '', '0');");
                }
            } catch (Exception ex) {
                
            }
        if (plugin.hasPermission(player, "cellmod.use.cell")) {
            String noservice = plugin.lang.getProperty("NoService") + " [    ]";
            if (!noservice.equals(plugin.getSignal(event.getPlayer(), 0.0))) {
                ResultSet getmessages = plugin.getResult("SELECT * FROM backlog WHERE ToUser='" + event.getPlayer().getName() + "';");
            try {
                while(getmessages.next()) {
                     event.getPlayer().sendMessage(plugin.lang.getProperty("Header"));
            event.getPlayer().sendMessage(plugin.config.getProperty("network-name") + " " + plugin.getSignal(event.getPlayer(), 0.0));
            ResultSet fromnumber = plugin.getResult("SELECT * from players WHERE Player='" + getmessages.getString("FromUser") + "';");
            String fromnum = "";
            try {
                while(fromnumber.next()) {
                    fromnum = fromnumber.getString("Number");
                }
            } catch (Exception ex) {
                fromnum = "0000";
            }
            event.getPlayer().sendMessage(plugin.lang.getProperty("From") + ": " + plugin.getServer().getPlayer(getmessages.getString("FromUser")).getName() + " (" + fromnum + ")");
            event.getPlayer().sendMessage(getmessages.getString("Text"));
            plugin.runDeleteQuery("DELETE FROM backlog WHERE Text='" + getmessages.getString("Text") + "';");
                }
            } catch (SQLException ex) {
                System.out.println(ex.toString());
            }
            }
        }
    }
}
