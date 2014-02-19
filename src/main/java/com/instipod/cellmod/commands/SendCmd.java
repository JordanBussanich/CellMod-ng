package com.instipod.cellmod.commands;

import com.instipod.cellmod.CellMod;
import java.sql.ResultSet;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public SendCmd(CellMod instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        Player p = null;
        try {
            p = (Player) cs;
        } catch (Exception ex) {
            cs.sendMessage("Only players may use this command.");
            return true;
        }
        if (strings.length > 1) {
            if (plugin.hasPermission((Player) cs, "cellmod.use.send")) {
                if (plugin.getPlayerCarrier(p) != null) {
                String text = "";
                Integer temp = 0;
                for (String s : strings) {
                    temp++;
                    if (temp > 1) {
                        text = text + s + " ";
                    }
                }
                Player messagefrom = (Player) cs;
                Player messageto = null;
                ResultSet getplayerfromnumber = plugin.getResult("SELECT * FROM players WHERE Number='" + strings[0] + "';");
                try {
                    while(getplayerfromnumber.next()) {
                        messageto = plugin.getServer().getPlayer(getplayerfromnumber.getString("Player"));
                    }
                    if (messageto == null) {
                         messagefrom.sendMessage(plugin.lang.getProperty("Header"));
            messagefrom.sendMessage(plugin.getPlayerCarrierName(messagefrom) + " " + plugin.getSignal(messagefrom, 0.0));
            messagefrom.sendMessage(plugin.lang.getProperty("InvaildDevice"));
                    } else {
                        if (plugin.getPlayerCarrier(messagefrom).getPlayerRemainingMessages(messagefrom) > 0) {
                            Integer remaining = plugin.getPlayerCarrier(messagefrom).getPlayerRemainingMessages(messagefrom) - 1;
                    sendText(messagefrom, messageto, text);
                        } else {
                            messagefrom.sendMessage(ChatColor.RED + plugin.lang.getProperty("NoMessagesPlan"));
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
                } else {
                    cs.sendMessage(ChatColor.RED + plugin.lang.getProperty("JoinNetwork"));
                }
            } else {
                cs.sendMessage(ChatColor.RED + plugin.lang.getProperty("NoPermission"));
            }
        } else {
            cs.sendMessage(plugin.lang.getProperty("WrongLength"));
        }
        return true;
    }
    public void sendText(Player from, Player to, String text) {
        String noservice = plugin.lang.getProperty("NoService") + " [    ]";
        if (to.isOnline()) {
            if (!noservice.equals(plugin.getSignal(to, 0.0))) {
                if (!noservice.equals(plugin.getSignal(from, 0.0))) {
            from.sendMessage(plugin.lang.getProperty("Header"));
            from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
            from.sendMessage(plugin.lang.getProperty("MessageSent"));
            to.sendMessage(plugin.lang.getProperty("Header"));
            to.sendMessage(plugin.getPlayerCarrierName(to) + " " + plugin.getSignal(to, 0.0));
            ResultSet fromnumber = plugin.getResult("SELECT * from players WHERE Player='" + from.getName() + "';");
            String fromnum = "";
            try {
                while(fromnumber.next()) {
                    fromnum = fromnumber.getString("Number");
                }
            } catch (Exception ex) {
                fromnum = "0000";
            }
            to.sendMessage(plugin.lang.getProperty("From") + ": " + from.getName() + " (" + fromnum + ")");
            to.sendMessage(text);
                } else {
                    from.sendMessage(plugin.lang.getProperty("Header"));
            from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
            from.sendMessage(plugin.lang.getProperty("ServiceRequired"));
                }
            } else {
                from.sendMessage(plugin.lang.getProperty("Header"));
            from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
            from.sendMessage(plugin.lang.getProperty("MessageSent"));
            plugin.runInsertQuery("INSERT INTO backlog (FromUser, ToUser, MessageText) VALUES ('" + from.getName() + "', '" + to.getName() + "', '" + text + "');");
            }
        } else {
            from.sendMessage(plugin.lang.getProperty("Header"));
            from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
            from.sendMessage(plugin.lang.getProperty("MessageSent"));
            plugin.runInsertQuery("INSERT INTO backlog (FromUser, ToUser, MessageText) VALUES ('" + from.getName() + "', '" + to.getName() + "', '" + text + "');");
        }
    }
}
