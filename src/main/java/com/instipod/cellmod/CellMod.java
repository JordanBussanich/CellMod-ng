package com.instipod.cellmod;

import com.alta189.sqlLibrary.MySQL.mysqlCore;
import com.alta189.sqlLibrary.SQLite.sqlCore;
import com.instipod.cellmod.commands.*;
import com.instipod.handlers.EconomyHandle;
import com.instipod.handlers.PermissionHandle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CellMod extends JavaPlugin {
    private final CommandManager commandManager = new CommandManager(this);
    private playerListener playerListener;
    public EconomyHandle economy = new EconomyHandle(this);
    public PermissionHandle permission = new PermissionHandle(this);
    public mysqlCore manageMySQL; // MySQL handler
    public sqlCore manageSQLite;
    public File pFolder = new File("plugins" + File.separator + "CellMod");
    public Boolean MySQL = false;
    private String dbHost = null;
    private String dbUser = null;
    private String dbPass = null;
    private String dbDatabase = null;
    private Integer towercount = 0;
    public HashMap<Location,String> globaltos = new HashMap<Location,String>();
    public HashMap<String,Carrier> carriers = new HashMap<String,Carrier>();
    private blockListener bListener;
    
    @Override
    public void onDisable() {
        TLogger.info("CellMod disabled."); 
    }

    @Override
    public void onEnable() {
        playerListener = new playerListener(this);
        bListener = new blockListener(this);
        TLogger.initialize(this, Logger.getLogger("Minecraft"));
        try {
            loadConfig();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        if ("sqlite".equals(config.getProperty("database-type"))) {
            MySQL = false;
        } else {
            MySQL = true;
            dbHost = config.getProperty("database-mysql-host");
            dbDatabase = config.getProperty("database-mysql-name");
            dbUser = config.getProperty("database-mysql-user");
            dbPass = config.getProperty("database-mysql-pass");
        }
                if (this.MySQL) {
			// Declare MySQL Handler
			this.manageMySQL = new mysqlCore(Logger.getLogger("Minecraft"), "[CellMod] ", this.dbHost, this.dbDatabase, this.dbUser, this.dbPass);
			
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
			this.manageSQLite = new sqlCore(Logger.getLogger("Minecraft"), "[CellMod] ", "CellMod", pFolder.getPath());
			
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
        TLogger.log(Level.INFO, towercount.toString() + " towers loaded.");
        economy.setup();
        addCommand("cell", cellcmd);
        addCommand("number", numcmd);
        addCommand("send", sendcmd);
        addCommand("ct", cccmd);
        addCommand("em", emcmd);
        addCommand("directory", phonebookcmd);
        addCommand("plan", plancmd);
        addCommand("carrier", carriercmd);
        TLogger.info("CellMod version " + getDescription().getVersion() + " is enabled!");
    }
    
    public CellCmd cellcmd = new CellCmd(this);
    public NumberCmd numcmd = new NumberCmd(this);
    public SendCmd sendcmd = new SendCmd(this);
    public CCCmd cccmd = new CCCmd(this);
    public EmCmd emcmd = new EmCmd(this);
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
                while(hascarrier.next()) {
                    thecarrier = hascarrier.getString("Carrier");
                }
            } catch (Exception ex) {
            }
       if (!"".equals(thecarrier)) {
           return carriers.get(thecarrier).getSignal(p, effect);
       } else {
           return lang.getProperty("NoService") + " [    ]";
       }
    }
    public Double getDistance(Player p, Double effect) {
        ResultSet hascarrier = getResult("SELECT * from players WHERE Player='" + p.getName() + "';");
        String thecarrier = null;
            try {
                while(hascarrier.next()) {
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
                while(hascarrier.next()) {
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
    public File configfile = new File("plugins/CellMod/config.properties");
    public File langfile = new File("plugins/CellMod/language.properties");
    public File folder = new File("plugins/CellMod/");
    public Properties prop = new Properties();
    public Properties lang = new Properties();
    public Properties config = new Properties();
    public FileInputStream filein;
    public Integer blockint = 0;
    
    private void loadConfig() throws IOException {
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        if(!configfile.exists()) {
            configfile.createNewFile();
            FileOutputStream out = new FileOutputStream(configfile);
	    prop.put("tower-material-id", "1");
            prop.put("database-type", "sqlite");
            prop.put("database-mysql-name", "cellmod");
            prop.put("database-mysql-user", "root");
            prop.put("database-mysql-pass", "");
            prop.put("database-mysql-host", "localhost");
	    prop.store(out, "CellMod 3.x Configuration File");
	    out.flush();
	    out.close();
	    prop.clear();
        }
        if(!langfile.exists()) {
            langfile.createNewFile();
            if (langfile.exists()) {
            FileOutputStream out = new FileOutputStream(langfile);
	    prop.put("NoService", "No Service");
            prop.put("Header", "---- Phone ----");
            prop.put("NoPermission", "You do not have permission.");
            prop.put("WrongBlockType", "This material cannot be identifed as a tower.");
            prop.put("TypeSend", "Type /send to send a new message.");
            prop.put("InvaildNumber", "The number entered was invalid.");
            prop.put("MessageSent", "Your message was sent.");
            prop.put("TowerDist", "Tower Distance");
            prop.put("AntennaType", "Antenna Type");
            prop.put("Pumpkin", "PUMPKIN Extended Range");
            prop.put("Standard", "Standard");
            prop.put("NumberIs", "Your number is ");
            prop.put("From", "From");
            prop.put("ServiceRequired", "Service is required to perform this action.");
            prop.put("WrongLength", "You must enter a phone number and message.");
            prop.put("EmTitle", "911 Call");
            prop.put("EmWrongLength", "You must enter a message.");
            prop.put("EmSent", "Your 911 call was sent to all online administrators.");
            prop.put("NumChanged", "Your number was changed.");
            prop.put("NumNoMore", "You may not change your number any more!");
            prop.put("NumWrongLength", "You must enter a new number!");
            prop.put("TCreated", "Cell Tower Created.");
            prop.put("TDestroyed", "Cell Tower Destroyed.");
            prop.put("NumAlready", "That phone number is already taken.");
            prop.put("PhonebookTitle", "Currently Connected Users");
            prop.put("InvaildDevice", "There is no such device on the network.");
            prop.put("PlanCount1", "Your current plan has ");
            prop.put("PlanCount2", " messages remaining.");
            prop.put("Carrier", "Carrier");
            prop.put("CarrierIs", "Your carrier is ");
            prop.put("RateSet", "Text message rate set.");
            prop.put("BuyMessages", "To buy messages type: /plan buy [number of messages]");
            prop.put("MessagesCost", "The price per message is: ");
            prop.put("JoinNetwork", "Join a Carrier first!");
            prop.put("MoreMoney", "You need more money to purchase that!");
            prop.put("GoodPurchase", "Your purchase was successful!");
            prop.put("CreateCarrier", "The carrier was created!");
            prop.put("InvaildCarrier", "Invaild Carrier Name!");
            prop.put("JoinedCarrier", "You joined the carrier.");
            prop.put("NoMessagesPlan", "You don't have any messages left on your plan.");
            prop.put("ChangedPrice", "The message cost was changed.");
	    prop.store(out, "CellMod 3.x Language File");
	    out.flush();
	    out.close();
	    prop.clear();
            } else {
                TLogger.log(Level.SEVERE, "Failed to create language file.");
            }
        }
        filein = new FileInputStream(langfile);
        lang.load(filein);
        filein.close();
        filein = new FileInputStream(configfile);
        config.load(filein);
        filein.close();
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
     
     public boolean hasPermission(Player p, String perm) {
         return permission.hasPermission(p, perm);
     }
}
