package com.instipod.cellmod;

import com.alta189.sqlLibrary.MySQL.mysqlCore;
import com.alta189.sqlLibrary.SQLite.sqlCore;
import com.instipod.cellmod.commands.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CellMod extends JavaPlugin {
    private CommandManager commandManager = new CommandManager(this);
    private UnifiedListener listener;
    private ConfigAccessor configAccessor;

    private Permission permission = null;
    public Economy economy = null;

    public FileConfiguration mainConfig;
    public FileConfiguration languageConfig;

    public mysqlCore manageMySQL;
    public sqlCore manageSQLite;
    public Boolean MySQL = false;

    public HashMap<Location, String> globaltos = new HashMap<Location, String>();
    public HashMap<String, Carrier> carriers = new HashMap<String, Carrier>();

    @Override
    public void onDisable() {
        TLogger.info("CellMod disabled.");
    }

    @Override
    public void onEnable() {
        TLogger.initialize(this, Logger.getLogger("Minecraft"));

        try {
            loadConfig();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        if ("sqlite".equals(mainConfig.getString("database.type"))) {
            MySQL = false;
        } else {
            MySQL = true;
        }
        if (this.MySQL) {
            // Declare MySQL Handler
            this.manageMySQL = new mysqlCore(Logger.getLogger("Minecraft"), "[CellMod] ", mainConfig.getString("database.mysql-host"), mainConfig.getString("database.mysql-db"), mainConfig.getString("database.mysql-user"), mainConfig.getString("database.mysql-pass"));

            TLogger.info("MySQL Initializing");
            // Initialize MySQL Handler
            this.manageMySQL.initialize();

            try {
                if (this.manageMySQL.checkConnection()) { // Check if the Connection was successful
                    TLogger.info("MySQL connection successful");
                    if (!this.manageMySQL.checkTable("towers")) { // Check if the table exists in the database if not create it
                        TLogger.info("Creating table towers");
                        String query = "CREATE TABLE towers (BlockWorld VARCHAR(255), BlockX VARCHAR(255), BlockY VARCHAR(255), BlockZ VARCHAR(255), Carrier VARCHAR(120));";
                        this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
                    }
                    if (!this.manageMySQL.checkTable("players")) { // Check if the table exists in the database if not create it
                        TLogger.info("Creating table players");
                        String query = "CREATE TABLE players (Player VARCHAR(60), Number VARCHAR(60), Changed VARCHAR(10), Carrier VARCHAR(120), Plan VARCHAR(120));";
                        this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
                    }
                    if (!this.manageMySQL.checkTable("backlog")) { // Check if the table exists in the database if not create it
                        TLogger.info("Creating table backlog");
                        String query = "CREATE TABLE backlog (FromUser VARCHAR(60), ToUser VARCHAR(60), MessageText VARCHAR(255));";
                        this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
                    }
                    if (!this.manageMySQL.checkTable("carriers")) { // Check if the table exists in the database if not create it
                        TLogger.info("Creating table carriers");
                        String query = "CREATE TABLE carriers (Name VARCHAR(60), Owner VARCHAR(60), MessagePrice VARCHAR(12));";
                        this.manageMySQL.createTable(query); // Use mysqlCore.createTable(query) to create tables
                    }
                } else {
                    TLogger.log(Level.SEVERE, "MySQL connection failed");
                    this.MySQL = false;
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            }
        } else {
            TLogger.info("SQLite Initializing");

            // Declare SQLite handler
            this.manageSQLite = new sqlCore(Logger.getLogger("Minecraft"), "[CellMod] ", "CellMod", getDataFolder().getPath());

            // Initialize SQLite handler
            this.manageSQLite.initialize();

            // Check if the table exists, if it doesn't create it
            if (!this.manageSQLite.checkTable("towers")) { // Check if the table exists in the database if not create it
                TLogger.info("Creating table towers");
                String query = "CREATE TABLE towers (BlockWorld VARCHAR(255), BlockX VARCHAR(255), BlockY VARCHAR(255), BlockZ VARCHAR(255), Carrier VARCHAR(120));";
                this.manageSQLite.createTable(query); // Use mysqlCore.createTable(query) to create tables
            }
            if (!this.manageSQLite.checkTable("players")) { // Check if the table exists in the database if not create it
                TLogger.info("Creating table players");
                String query = "CREATE TABLE players (Player VARCHAR(60), Number VARCHAR(60), Changed VARCHAR(10), Carrier VARCHAR(120), Plan VARCHAR(120));";
                this.manageSQLite.createTable(query); // Use mysqlCore.createTable(query) to create tables
            }
            if (!this.manageSQLite.checkTable("backlog")) { // Check if the table exists in the database if not create it
                TLogger.info("Creating table backlog");
                String query = "CREATE TABLE backlog (FromUser VARCHAR(60), ToUser VARCHAR(60), MessageText VARCHAR(255));";
                this.manageSQLite.createTable(query); // Use mysqlCore.createTable(query) to create tables
            }
            if (!this.manageSQLite.checkTable("carriers")) { // Check if the table exists in the database if not create it
                TLogger.info("Creating table carriers");
                String query = "CREATE TABLE carriers (Name VARCHAR(60), Owner VARCHAR(60), MessagePrice VARCHAR(12));";
                this.manageSQLite.createTable(query); // Use mysqlCore.createTable(query) to create tables
            }

        }
        ResultSet car = getResult("SELECT * FROM carriers;");
        try {
            while (car.next()) {
                String cname = car.getString("Name");
                String cowner = car.getString("Owner");
                String ccost = car.getString("MessagePrice");
                Double cost = Double.parseDouble(ccost);
                carriers.put(cname, new Carrier(this, cname, cowner, cost));
            }
        } catch (Exception ex) {
            TLogger.log(Level.SEVERE, "A Carrier Has An Invaild Message Cost! Must be an Integer or Double!");
        }
        ResultSet rs = getResult("SELECT * FROM towers;");
        int towercount = 0;
        try {
            while (rs.next()) {
                Integer blockx = Integer.parseInt(rs.getString("BlockX"));
                Integer blocky = Integer.parseInt(rs.getString("BlockY"));
                Integer blockz = Integer.parseInt(rs.getString("BlockZ"));
                String world = rs.getString("BlockWorld");
                String carrier = rs.getString("Carrier");
                Block tower = new Location(getServer().getWorld(world), blockx, blocky, blockz).getBlock();
                globaltos.put(tower.getLocation(), carrier);
                carriers.get(carrier).addTower(tower.getLocation());
                towercount++;
            }
        } catch (SQLException ex) {
        }
        TLogger.log(Level.INFO, towercount + " towers loaded.");

        if (!setupEconomy()) {
            TLogger.log(Level.SEVERE, "Failed to initialize Economy!");
        }
        if (!setupPermissions()) {
            TLogger.log(Level.SEVERE, "Failed to initialize Permissions!");
        }

        listener = new UnifiedListener(this);

        addCommand("cell", cellcmd);
        addCommand("number", numcmd);
        addCommand("send", sendcmd);
        addCommand("ct", cccmd);
        addCommand("directory", phonebookcmd);
        addCommand("plan", plancmd);
        addCommand("carrier", carriercmd);
        TLogger.info("CellMod version " + getDescription().getVersion() + " is enabled!");
    }

    public CellCmd cellcmd = new CellCmd(this);
    public NumberCmd numcmd = new NumberCmd(this);
    public SendCmd sendcmd = new SendCmd(this);
    public CCCmd cccmd = new CCCmd(this);
    public PhonebookCmd phonebookcmd = new PhonebookCmd(this);
    public PlanCmd plancmd = new PlanCmd(this);
    public CarrierCmd carriercmd = new CarrierCmd(this);

    private void addCommand(String command, CommandExecutor executor) {
        getCommand(command).setExecutor(executor);
        commandManager.addCommand(command, executor);
    }

    public String getSignal(Player p, Double effect) {
        ResultSet hascarrier = getResult("SELECT * from players WHERE Player='" + p.getName() + "';");
        String thecarrier = null;
        try {
            while (hascarrier.next()) {
                thecarrier = hascarrier.getString("Carrier");
            }
        } catch (Exception ex) {
        }
        if (!"".equals(thecarrier)) {
            return carriers.get(thecarrier).getSignal(p, effect);
        } else {
            return languageConfig.getString("NoService") + " [    ]";
        }
    }

    public Double getDistance(Player p, Double effect) {
        ResultSet hascarrier = getResult("SELECT * from players WHERE Player='" + p.getName() + "';");
        String thecarrier = null;
        try {
            while (hascarrier.next()) {
                thecarrier = hascarrier.getString("Carrier");
            }
        } catch (Exception ex) {
        }
        if (!"".equals(thecarrier)) {
            return carriers.get(thecarrier).getDistance(p, effect);
        } else {
            return null;
        }
    }

    public String getPlayerCarrierName(Player p) {
        Carrier carrier = getPlayerCarrier(p);
        if (carrier == null) {
            return "No Service";
        }
        return carrier.getName();
    }

    public Carrier getPlayerCarrier(Player p) {
        ResultSet hascarrier = getResult("SELECT * from players WHERE Player='" + p.getName() + "';");
        String thecarrier = null;
        try {
            while (hascarrier.next()) {
                thecarrier = hascarrier.getString("Carrier");
            }
        } catch (Exception ex) {
        }
        if (!"".equals(thecarrier)) {
            return carriers.get(thecarrier);
        } else {
            return null;
        }
    }

    public Boolean isPlayerCarrierOwner(Player p, Carrier carrier) {
        if (carrier == null) {
            return false;
        }
        if (carrier.getOwner().equals(p.getName())) {
            return true;
        } else {
            return false;
        }
    }

    private void loadConfig() throws IOException {
        //load main configuration
        saveDefaultConfig();
        mainConfig = getConfig();
        mainConfig.options().copyDefaults(true);

        mainConfig.set("donotchange.version", getDescription().getVersion());
        saveConfig();

        //load language file
        configAccessor = new ConfigAccessor(this, "language.yml");
        languageConfig = configAccessor.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandManager.dispatch(sender, cmd, label, args);
    }

    public ResultSet getResult(String query) {
        ResultSet result = null;
        if (MySQL) {
            try {
                result = manageMySQL.sqlQuery(query);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            }
        } else {
            result = manageSQLite.sqlQuery(query);
        }
        return result;
    }

    public void runInsertQuery(String query) {
        if (MySQL) {
            try {
                manageMySQL.insertQuery(query);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            }
        } else {
            manageSQLite.insertQuery(query);
        }
    }

    public void runUpdateQuery(String query) {
        if (MySQL) {
            try {
                manageMySQL.updateQuery(query);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            }
        } else {
            manageSQLite.updateQuery(query);
        }
    }

    public void runDeleteQuery(String query) {
        if (MySQL) {
            try {
                manageMySQL.deleteQuery(query);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.out.println(e.toString());
            }
        } else {
            manageSQLite.deleteQuery(query);
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public boolean hasPermission(Player p, String perm) {
        return permission.has(p, perm);
    }
}
