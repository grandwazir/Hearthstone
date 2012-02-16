package name.richardson.james.hearthstone;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import name.richardson.james.bukkit.utilities.command.CommandManager;
import name.richardson.james.bukkit.utilities.internals.Logger;
import name.richardson.james.bukkit.utilities.plugin.SimplePlugin;
import name.richardson.james.hearthstone.general.SetCommand;
import name.richardson.james.hearthstone.general.TeleportCommand;

public class Hearthstone extends SimplePlugin {

  private CommandManager commandManager;
  private DatabaseHandler database;
  private HearthstoneConfiguration configuration;
  private final Map<String, Long> cooldown = new HashMap<String, Long>();

  public Map<String, Long> getCooldownTracker() {
    return this.cooldown;
  }

  @Override
  public List<Class<?>> getDatabaseClasses() {
    return DatabaseHandler.getDatabaseClasses();
  }

  public DatabaseHandler getDatabaseHandler() {
    return this.database;
  }

  @Override
  public void onEnable() {
    try {
      Logger.setDebugging(this, true);
      this.setLoggerPrefix();
      this.loadConfiguration();
      this.setRootPermission();
      this.setResourceBundle();
      this.setupDatabase();
      this.registerCommands();
    } catch (final IOException e) {
      this.logger.severe("Unable to close file stream!");
      this.setEnabled(false);
    } catch (final SQLException e) {
      this.logger.severe(this.getMessage("unable-to-use-database"));
      this.setEnabled(false);
    } finally {
      if (!this.isEnabled()) {
        this.logger.severe(this.getMessage("panic"));
        return;
      }
    }
    this.logger.info(String.format(this.getMessage("plugin-enabled"), this.getDescription().getName()));
  }

  private void registerCommands() {
    this.commandManager = new CommandManager(this);
    this.getCommand("hs").setExecutor(this.commandManager);
    this.commandManager.addCommand(new SetCommand(this));
    TeleportCommand command = new TeleportCommand(this);
    this.commandManager.addCommand(command);
    this.getCommand("home").setExecutor(command);
  }

  private void setupDatabase() throws SQLException {
    try {
      this.getDatabase().find(HomeRecord.class).findRowCount();
    } catch (final PersistenceException ex) {
      this.logger.warning(this.getMessage("no-database"));
      this.installDDL();
    }
    this.database = new DatabaseHandler(this.getDatabase());
    this.logger.info(String.format(this.getMessage("homes-loaded"), this.database.count(HomeRecord.class)));
  }

  public HearthstoneConfiguration getHearthstoneConfiguration() {
    return this.configuration;
  }
  
  private void loadConfiguration() throws IOException {
    this.configuration = new HearthstoneConfiguration(this);
    if (configuration.isDebugging()) Logger.setDebugging(this, true);
  }

}
