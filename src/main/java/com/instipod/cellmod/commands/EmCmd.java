package com.instipod.cellmod.commands;

import com.instipod.cellmod.CellMod;
import com.instipod.cellmod.TLogger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public EmCmd(CellMod instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        Player pl = null;
        try {
            pl = (Player) cs;
        } catch (Exception ex) {
            cs.sendMessage("Only players may use this command.");
            return true;
        }
        if (strings.length > 0) {
            String text = "";
            for (String s : strings) {
                text = text + s + " ";
            }
            Player player = (Player) cs;
            List<Player> players = player.getWorld().getPlayers();
            cs.sendMessage(plugin.lang.getProperty("Header"));
            cs.sendMessage(plugin.lang.getProperty("EmSent"));
            for (Player p : players) {
                if (plugin.hasPermission(p, "cellmod.emcontact")) {
                    ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + p.getName() + "';");
        String number = null;
        try {
            while (rs.next()) {
                number = rs.getString("Number");
            }
        } catch (SQLException ex) {
            TLogger.log(Level.SEVERE, "Failed to read player number!");
        }
                    p.sendMessage(ChatColor.RED + "[" + plugin.lang.getProperty("EmTitle") + "] (" + player.getName() + " / " + number + ") " + text);
                }
            }
        } else {
            cs.sendMessage(plugin.lang.getProperty("EmWrongLength"));
        }
        return true;
    }
    
}
