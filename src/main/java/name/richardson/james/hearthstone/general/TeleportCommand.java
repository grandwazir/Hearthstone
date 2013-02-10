/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * TeleportCommand.java is part of Hearthstone.
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
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
import name.richardson.james.bukkit.utilities.command.CommandUsageException;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.HomeRecord;

public class TeleportCommand extends AbstractCommand {

  private final Server server;
  
  private final EbeanServer database;
  
  private final Map<String, Long> cooldownTracker;
  
  //* The name of the player we are teleporting to *//
  private String playerName;
  
  //* The UID of the world we are teleporting to *//
  private UUID worldUUID;
  
  //* The player who is teleporting *//
  private Player player;
  
  //* The cooldown to apply to the teleporting player *//
  private long cooldownTime;
  
  private Permission own;
  
  private Permission others;
  
  private Permission cooldown;

  public TeleportCommand(Hearthstone plugin) {
    super(plugin, true);
    this.server = plugin.getServer();
    this.database = plugin.getDatabase();
    this.cooldownTracker = plugin.getCooldownTracker();
    this.cooldownTime = plugin.getHearthstoneConfiguration().getCooldown();
  }

  public void execute(CommandSender sender) throws CommandArgumentException, CommandPermissionException, CommandUsageException {
    final String senderName = sender.getName().toLowerCase();

    if (!isPlayerCooldownExpired() && player.hasPermission(cooldown)) {
      throw new CommandUsageException(this.getLocalisation().getMessage(this, "cooldown-not-expired", TimeFormatter.millisToLongDHMS(cooldownTracker.get(senderName) - System.currentTimeMillis())));
    }
    
    if (sender.hasPermission(own) && senderName.equalsIgnoreCase(playerName)) {
      teleportPlayer();
      return;
    } else if (sender.hasPermission(others) && !senderName.equalsIgnoreCase(playerName)) {
      teleportPlayer();
      sender.sendMessage(this.getLocalisation().getMessage(this, "teleported-home", playerName));
    } else {
      throw new CommandPermissionException(this.getLocalisation().getMessage(this, "can-only-teleport-to-own-home"), others);
    }

  }

  private boolean isPlayerCooldownExpired() {
    final String playerName = player.getName().toLowerCase();
    if (!cooldownTracker.containsKey(playerName)) return true;
    final long cooldown = cooldownTracker.get(playerName);
    if ((cooldown - System.currentTimeMillis()) > 0) return false;
    cooldownTracker.remove(playerName);
    return true;
  }
  
  private boolean isLocationObstructed(Location orginalLocation) {
    Location location = orginalLocation.clone();
    // check the block that the player's legs occupy.
    if (!location.getBlock().isEmpty()) return true;
    // check the block that the player's head occupies.
    if (!location.add(0, 1, 0).getBlock().isEmpty()) return true;
    return false;
  }

  private void teleportPlayer() throws CommandUsageException {
    List<HomeRecord> homes = HomeRecord.findHomeRecordsByOwnerAndWorld(database, playerName, worldUUID);
    if (!homes.isEmpty()) {
      if (isLocationObstructed(homes.get(0).getLocation(server))) throw new CommandUsageException(this.getLocalisation().getMessage(this, "home-is-obstructed"));
      cooldownTracker.put(playerName, System.currentTimeMillis() + cooldownTime);
      player.teleport(homes.get(0).getLocation(server));
    } else {
      throw new CommandUsageException(this.getLocalisation().getMessage(this, "no-home-set", playerName));
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
    own.addParent(this.getRootPermission(), false);
    this.getPermissionManager().addPermission(own, false);
    // add ability to pardon the bans of others
    others = new Permission(prefix + this.getName() + "." + this.getLocalisation().getMessage(this, "others-permission-name"), this.getLocalisation().getMessage(this, "others-permission-description"), PermissionDefault.OP);
    others.addParent(this.getRootPermission(), false);
    this.getPermissionManager().addPermission(others, false);
    cooldown = new Permission(prefix + this.getName() + "." + this.getLocalisation().getMessage(this, "cooldown-permission-name"), this.getLocalisation().getMessage(this, "cooldown-permission-description"), PermissionDefault.TRUE);
    cooldown.addParent(this.getRootPermission(), false);
  }
  
  private UUID getWorldUUID(String worldName) throws CommandArgumentException {
    World world = server.getWorld(worldName);
    if (world != null) {
      return world.getUID();
    } else {
      throw new CommandArgumentException(this.getLocalisation().getMessage(this, "invalid-world"), this.getLocalisation().getMessage(this, "world-must-be-loaded"));
    }
  }
  
  public void parseArguments(String[] arguments, CommandSender sender) throws CommandArgumentException {
    this.player = (Player) sender;
    if (arguments.length == 0) {
      this.playerName = player.getName();
      this.worldUUID = player.getLocation().getWorld().getUID();
    } else if (arguments.length == 1) {
      String playerName = matchPlayerName(arguments[0]);
      this.playerName = playerName;
      this.worldUUID = player.getLocation().getWorld().getUID();
    } else if (arguments.length == 2) {
      this.playerName = matchPlayerName(arguments[0]);
      this.worldUUID = getWorldUUID(arguments[1]);
    }
  }

}
