package com.instipod.cellmod.commands;

import com.instipod.cellmod.Carrier;
import com.instipod.cellmod.CellMod;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CCCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public CCCmd(CellMod instance) {
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
        Player user = (Player) cs;
        Block target = user.getTargetBlock(null, 100);
        if (target.getTypeId() == Integer.parseInt(plugin.config.getProperty("tower-material", "1"))) {
            if (plugin.hasPermission(user, "cellmod.createcarrier")) {
                Carrier playercarrier = plugin.getPlayerCarrier(user);
                if (plugin.isPlayerCarrierOwner(user, playercarrier)) {
                plugin.runInsertQuery("INSERT INTO towers (BlockWorld, BlockX, BlockY, BlockZ, Carrier) VALUES ('" + target.getWorld().getName() + "', '" + target.getX() + "', '" + target.getY() + "', '" + target.getZ() + "', '" + playercarrier.getName() + "');");
                plugin.globaltos.put(target.getLocation(), playercarrier.getName());
                playercarrier.addTower(target.getLocation());
                user.sendMessage(ChatColor.GREEN + plugin.lang.getProperty("TCreated"));
                } else {
                    user.sendMessage(ChatColor.RED + plugin.lang.getProperty("NoPermission"));
                }
            } else {
                user.sendMessage(ChatColor.RED + plugin.lang.getProperty("NoPermission"));
            }    
        } else {
            user.sendMessage(ChatColor.RED + plugin.lang.getProperty("WrongBlockType"));
        }
        return true;
    }
    
}

