
package name.richardson.james.hearthstone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import name.richardson.james.hearthstone.commands.SetCommand;
import name.richardson.james.hearthstone.commands.TeleportCommand;
import name.richardson.james.hearthstone.exceptions.CooldownNotExpiredException;
import name.richardson.james.hearthstone.exceptions.LocationBlockedException;
import name.richardson.james.hearthstone.persistant.HomeRecord;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.avaje.ebean.EbeanServer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;

public class HearthstoneOld extends JavaPlugin {

  public static ResourceBundle messages;
  private final static File confFile = new File("plugins/Hearthstone/config.yml");

  private static EbeanServer db;
  private static HearthstoneOld instance;

  private final static Locale locale = Locale.getDefault();
  private final static Logger logger = Logger.getLogger("Minecraft");
  public Configuration conf;

  private CommandManager cm;
  private final Map<String, Long> cooldownList = new HashMap<String, Long>();
  private long cooldownTime;
  private PluginDescriptionFile desc;
  private PluginManager pm;
  private GlobalRegionManager worldGuardRegionManager;

  public HearthstoneOld() {
    HearthstoneOld.instance = this;
    if (messages == null) {
      try {
        HearthstoneOld.messages = ResourceBundle.getBundle("name.richardson.james.hearthstone.localisation.Messages", locale);
      } catch (final MissingResourceException e) {
        HearthstoneOld.messages = ResourceBundle.getBundle("name.richardson.james.hearthstone.localisation.Messages");
        log(Level.WARNING, String.format(messages.getString("noLocalisationFound"), locale.getDisplayLanguage()));
      }
    }
  }

  public static EbeanServer getDb() {
    return db;
  }

  public static HearthstoneOld getInstance() {
    return instance;
  }

  public static void log(final Level level, final String msg) {
    logger.log(level, "[Hearthstone] " + msg);
  }

  public void checkPlayerCoolDown(final Player player) throws CooldownNotExpiredException {
    final String playerName = player.getName();
    final String node = getName().toLowerCase() + "." + messages.getString("CooldownPermission");
    if (cooldownList.containsKey(playerName) && player.hasPermission(node)) {
      if (cooldownList.get(playerName) <= System.currentTimeMillis()) {
        cooldownList.remove(playerName);
      } else {
        throw new CooldownNotExpiredException(cooldownList.get(playerName));
      }
    }
  }

  @Override
  public List<Class<?>> getDatabaseClasses() {
    final List<Class<?>> list = new ArrayList<Class<?>>();
    list.add(HomeRecord.class);
    return list;
  }

  public String getMessage(final String key) {
    return messages.getString(key);
  }

  public String getName() {
    return desc.getName();
  }

  public PluginManager getPluginManager() {
    return pm;
  }

  public boolean isLocationBlocked(final Location location) {
    final double locationZ = location.getZ();
    // check to see if the location block, and the block 3 above it, are free of
    // obstructions
    for (double z = locationZ; z > locationZ - 2; z = z - 1) {
      final Location testLocation = location.clone();
      testLocation.setZ(z);
      if (!testLocation.getBlock().getType().equals(Material.AIR))
        return true;
    }
    return false;
  }

  public boolean isLocationBuildable(final Player player, final Location location) {
    if (worldGuardRegionManager != null) {
      return worldGuardRegionManager.canBuild(player, location);
    } else {
      return true;
    }
  }

  public void onDisable() {
    log(Level.INFO, String.format(messages.getString("PluginDisabled"), desc.getName()));
  }

  public void onEnable() {
    pm = getServer().getPluginManager();
    desc = getDescription();
    conf = new Configuration(confFile);
    db = getDatabase();
    cm = new CommandManager(this);

    // load configuration
    loadConfiguration();

    connectWorldGuard();
    setupDatabase();

    // register commands
    getCommand("home").setExecutor(cm);
    cm.registerCommand("set", new SetCommand(this));
    cm.registerCommand("teleport", new TeleportCommand(this));

    log(Level.INFO, String.format(messages.getString("PluginEnabled"), desc.getFullName()));
  }

  public void teleportHome(final HomeRecord home, final Player player) throws LocationBlockedException, CooldownNotExpiredException {
    if (isLocationBlocked(home.getLocation())) {
      home.destroy();
      throw new LocationBlockedException();
    } else {
      checkPlayerCoolDown(player);
      player.teleport(home.getLocation());
      cooldownList.put(player.getName(), System.currentTimeMillis() + cooldownTime);
    }
  }

  private void connectWorldGuard() {
    final Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
    if (plugin != null) {
      worldGuardRegionManager = ((WorldGuardPlugin) plugin).getGlobalRegionManager();
      log(Level.INFO, String.format(messages.getString("ConnectedToWorldGuard"), ((WorldGuardPlugin) plugin).getDescription().getFullName()));
    }

  }

  private void createConfiguration() {
    try {
      log(Level.WARNING, String.format(getMessage("ConfigurationNotFound")));
      log(Level.INFO, String.format(getMessage("CreatingNewConfiguration"), confFile.getPath()));
      confFile.getParentFile().mkdirs();
      confFile.createNewFile();
      conf.getInt("cooldown", 15);
      conf.save();
    } catch (final IOException e) {
      log(Level.SEVERE, String.format("Unable to load configuration: %s", confFile.getPath()));
      pm.disablePlugin(instance);
    }
  }

  private void loadConfiguration() {
    conf.load();
    if (conf.getAll().isEmpty()) {
      createConfiguration();
    }
    cooldownTime = conf.getInt("cooldown", 15) * 60 * 1000;
    log(Level.INFO, String.format("Loaded configuration: %s", confFile.getPath()));
  }

  private void setupDatabase() {
    try {
      getDatabase().find(HomeRecord.class).findRowCount();
    } catch (final PersistenceException ex) {
      log(Level.WARNING, messages.getString("NoDatabase"));
      installDDL();
    }
  }

}
