package com.instipod.cellmod;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Carrier {
    private CellMod plugin;
    private ArrayList<Location> tos = new ArrayList<Location>();
    private Double cost = 0.0;
    private String owner;
    private String name;

    public Carrier(CellMod instance, String netname, String netowner, Double messagecost) {
        plugin = instance;
        cost = messagecost;
        name = netname;
        owner = netowner;
    }

    public String getSignal(Player player, Double effect) {
        ArrayList<Location> tosearch = new ArrayList<Location>();
        for (Location l : tos) {
            if (l.getWorld() == player.getWorld()) {
                tosearch.add(l);
            }
        }
        String message = plugin.languageConfig.getString("NoService") + " [    ]";
        Location from = player.getLocation();
        double disSqu = 0;
        Location best = null;
        double curDisSqu;
        if (tosearch.size() > 0) {
            for (Location to : tosearch) {
                curDisSqu = from.distanceSquared(to);
                if (curDisSqu < disSqu || best == null) {
                    best = to;
                    disSqu = curDisSqu;
                }
            }
            Double dist = best.distance(player.getLocation()) - effect;
            if (dist > 170) {
                message = plugin.languageConfig.getString("NoService") + " [    ]";
            } else {
                if (dist < 30) {
                    message = "3G [" + ChatColor.GREEN + "-----" + ChatColor.WHITE + "]";
                } else {
                    if (dist < 60) {
                        message = "3G [" + ChatColor.DARK_GREEN + "---- " + ChatColor.WHITE + "]";
                    } else {
                        if (dist < 110) {
                            message = "E [" + ChatColor.DARK_BLUE + "---  " + ChatColor.WHITE + "]";
                        } else {
                            if (dist < 135) {
                                message = "E [" + ChatColor.RED + "--   " + ChatColor.WHITE + "]";
                            } else {
                                if (dist < 169) {
                                    message = "GPRS [" + ChatColor.DARK_RED + "-    " + ChatColor.WHITE + "]";
                                }
                            }
                        }
                    }
                }
            }
        } else {
            message = plugin.languageConfig.getString("NoService") + " [    ]";
        }
        return message;
    }

    public Double getDistance(Player player, Double effect) {
        ArrayList<Location> tosearch = new ArrayList<Location>();
        for (Location l : tos) {
            if (l.getWorld() == player.getWorld()) {
                tosearch.add(l);
            }
        }
        Location from = player.getLocation();
        double disSqu = 0;
        Location best = null;
        double curDisSqu;
        if (tosearch.size() > 0) {
            for (Location to : tosearch) {
                curDisSqu = from.distanceSquared(to);
                if (curDisSqu < disSqu || best == null) {
                    best = to;
                    disSqu = curDisSqu;
                }
            }
            Double dist = best.distance(player.getLocation()) - effect;
            return dist;
        } else {
            return null;
        }
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Double getCost() {
        return cost;
    }

    public ArrayList getRawTOS() {
        return tos;
    }

    public void addTower(Location loc) {
        tos.add(loc);
    }

    public void removeTower(Location loc) {
        tos.remove(loc);
    }

    public Integer getPlayerRemainingMessages(Player p) {
        ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + p.getName() + "';");
        String count = null;
        try {
            while (rs.next()) {
                count = rs.getString("Plan");
            }
        } catch (Exception ex) {
            TLogger.log(Level.SEVERE, "Failed to read player number!");
        }
        return Integer.parseInt(count);
    }

    public void setPlayerRemainingMessage(Player p, Integer messages) {
        plugin.runUpdateQuery("UPDATE players SET Plan=" + messages + " WHERE Player='" + p.getName() + "';");
    }
}
