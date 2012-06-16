/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * Hearthstone.java is part of Hearthstone.
 * 
 * Hearthstone is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Hearthstone is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Hearthstone. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package name.richardson.james.hearthstone;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;

import name.richardson.james.bukkit.utilities.command.CommandManager;
import name.richardson.james.bukkit.utilities.plugin.SkeletonPlugin;
import name.richardson.james.hearthstone.general.HomeCommand;
import name.richardson.james.hearthstone.general.SetCommand;
import name.richardson.james.hearthstone.general.TeleportCommand;

public class Hearthstone extends SkeletonPlugin {

  private CommandManager commandManager;
  private DatabaseHandler database;
  private HearthstoneConfiguration configuration;
  private final Map<String, Long> cooldown = new HashMap<String, Long>();
  
  private WorldGuardPlugin worldGuard;

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

  public HearthstoneConfiguration getHearthstoneConfiguration() {
    return this.configuration;
  }
  
  public GlobalRegionManager getGlobalRegionManager() {
    if (this.worldGuard != null) {
      return this.worldGuard.getGlobalRegionManager();
    } else {
      return null;
    }
  }
  

  private void connectToWorldGuard() {
    this.worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
    if (this.worldGuard != null) {
      this.logger.info(this.getSimpleFormattedMessage("worldguard-hooked", this.worldGuard.getDescription().getFullName()));
    }
  }

  protected void loadConfiguration() throws IOException {
    this.configuration = new HearthstoneConfiguration(this);
    this.connectToWorldGuard();
  }

  protected void registerCommands() {
    this.commandManager = new CommandManager(this);
    this.getCommand("hs").setExecutor(this.commandManager);
    SetCommand setCommand = new SetCommand(this);
    this.commandManager.addCommand(setCommand);
    TeleportCommand teleportCommand = new TeleportCommand(this);
    this.commandManager.addCommand(teleportCommand);
    this.getCommand("home").setExecutor(new HomeCommand(this, teleportCommand, setCommand));
  }
  

  protected void setupPersistence() throws SQLException {
    try {
      this.getDatabase().find(HomeRecord.class).findRowCount();
    } catch (final PersistenceException ex) {
      this.logger.warning(this.getMessage("no-database"));
      this.installDDL();
    }
    this.database = new DatabaseHandler(this.getDatabase());
    this.logger.info(this.getFormattedHomeCount(database.count(HomeRecord.class)));
  }
  

  private String getFormattedHomeCount(int count) {
    Object[] arguments = {count};
    double[] limits = {0, 1, 2};
    String[] formats = {this.getMessage("no-homes"), this.getMessage("one-home"), this.getMessage("many-homes")};
    return this.getChoiceFormattedMessage("homes-loaded", arguments, formats, limits);
  }

  
  public String getArtifactID() {
    return "hearthstone";
  }

  public String getGroupID() {
    return "name.richardson.james.bukkit";
  }

}
