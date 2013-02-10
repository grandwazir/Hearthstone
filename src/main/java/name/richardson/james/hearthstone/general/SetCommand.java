/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * SetCommand.java is part of Hearthstone.
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
package name.richardson.james.hearthstone.general;

import java.util.List;

import com.avaje.ebean.EbeanServer;
import com.sk89q.worldguard.protection.GlobalRegionManager;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
import name.richardson.james.bukkit.utilities.command.CommandUsageException;
import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.HomeRecord;

public class SetCommand extends AbstractCommand {

  private final Server server;
  
  private final EbeanServer database;
  
  /** The name of the players home we are setting */
  private String playerName;

  /** The location of the home */
  private Location location;
  
  private final GlobalRegionManager manager;
  
  /** The player who is setting the home */
  private Player player;
  
  private Permission own;

  private Permission others;
  
  public SetCommand(Hearthstone plugin) {
    super(plugin, true);
    this.manager = plugin.getGlobalRegionManager();
    this.server = plugin.getServer();
    this.database = plugin.getDatabase();
  }

  private void createHome() throws CommandUsageException {
    // check if location is obstructed
    if (isLocationObstructed()) throw new CommandUsageException(this.getLocalisation().getMessage(this, "location-is-obstructed"));
    // check if the location is buildable
    if (!isPlayerAllowedToBuild()) throw new CommandUsageException(this.getLocalisation().getMessage(this, "not-allowed-to-build-here"));
    // delete any existing homes
    HomeRecord.deleteHomes(database, playerName, location.getWorld().getUID());
    // create the home
    final HomeRecord record = new HomeRecord();
    record.setCreatedAt(System.currentTimeMillis());
    record.setCreatedBy(playerName);
    record.setX(location.getX());
    record.setY(location.getY());
    record.setZ(location.getZ());
    record.setYaw(location.getYaw());
    record.setPitch(location.getPitch());
    record.setWorldUUID(location.getWorld().getUID());
    database.save(record);
  }

  public void execute(CommandSender sender) throws CommandArgumentException, CommandUsageException, CommandPermissionException {
    this.location = ((Player) sender).getLocation(); 
    if (sender.hasPermission(own) && this.playerName.equalsIgnoreCase(sender.getName())) {
      this.createHome();
      sender.sendMessage(this.getLocalisation().getMessage(this, "home-set"));
      return;
    } else if (this.playerName.equalsIgnoreCase(sender.getName())) {
      throw new CommandPermissionException(null, others);
    }
    
    if (sender.hasPermission(others) && !this.playerName.equalsIgnoreCase(sender.getName())) {
      this.createHome();
      sender.sendMessage(this.getLocalisation().getMessage(this, "home-set-others", this.playerName));
    } else if (!this.playerName.equalsIgnoreCase(sender.getName())) {
      throw new CommandPermissionException(null, others);
    }

  }

  private boolean isLocationObstructed() {
    Location location = this.location.clone();
    // check the block that the player's legs occupy.
    if (!location.getBlock().isEmpty()) return true;
    // check the block that the player's head occupies.
    if (!location.add(0, 1, 0).getBlock().isEmpty()) return true;
    return false;
  }

  private boolean isPlayerAllowedToBuild() {
    if (this.manager != null) {
      return manager.canBuild(this.player, this.location);
    } else {
      return true;
    }
  }

  private String matchPlayerName(String playerName) {
    List<Player> matches = this.server.matchPlayer(playerName);
    if (matches.isEmpty()) {
      return playerName;
    } else {
      return matches.get(0).getName();
    }
  }
  
  protected void registerPermissions(boolean wildcard) {
    super.registerPermissions(wildcard);
    final String prefix = this.getPermissionManager().getRootPermission().getName().replace("*", "");
    own = new Permission(prefix + this.getName() + "." + this.getLocalisation().getMessage(this, "own-permission-name"), this.getLocalisation().getMessage(this, "own-permission-description"), PermissionDefault.TRUE);
    own.addParent(this.getRootPermission(), true);
    this.getPermissionManager().addPermission(own, false);
    // add ability to pardon the bans of others
    others = new Permission(prefix + this.getName() + "." + this.getLocalisation().getMessage(this, "others-permission-name"), this.getLocalisation().getMessage(this, "others-permission-description"), PermissionDefault.OP);
    others.addParent(this.getRootPermission(), true);
    this.getPermissionManager().addPermission(others, false);
    // provide access to this command by default
    this.getPermissionManager().getPermission(prefix + this.getName()).setDefault(PermissionDefault.TRUE);
  }
  
  public void parseArguments(String[] arguments, CommandSender sender) throws CommandArgumentException {
    this.player = (Player) sender;
    if (arguments.length == 0) {
      this.playerName = sender.getName(); 
    } else {
      this.playerName = matchPlayerName(arguments[0]);
    }
  }

  


}
