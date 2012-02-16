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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
import name.richardson.james.bukkit.utilities.command.CommandUsageException;
import name.richardson.james.bukkit.utilities.command.PluginCommand;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
import name.richardson.james.hearthstone.DatabaseHandler;
import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.HearthstoneConfiguration;
import name.richardson.james.hearthstone.HomeRecord;

public class TeleportCommand extends PluginCommand {

  private final Server server;
  private final DatabaseHandler database;
  private final Map<String, Long> cooldown;
  private final HearthstoneConfiguration configuration;

  public TeleportCommand(Hearthstone plugin) {
    super(plugin, plugin.getMessage("teleportcommand-name"), plugin.getMessage("teleportcommand-description"), plugin.getMessage("teleportcommand-usage"));
    this.server = plugin.getServer();
    this.database = plugin.getDatabaseHandler();
    this.cooldown = plugin.getCooldownTracker();
    this.configuration = plugin.getHearthstoneConfiguration();
    this.registerPermissions();
  }

  public void execute(CommandSender sender) throws CommandArgumentException, CommandPermissionException, CommandUsageException {
    final Player player = (Player) sender;
    final long cooldownTime = System.currentTimeMillis() + this.configuration.getCooldown();

    // if the player has to obey the cooldown check to see if they are allowed
    // to teleport
    final String key = player.getName().toLowerCase();
    if (!sender.hasPermission(this.getPermission(2)) && cooldown.containsKey(key)) {
      long timeLeft = cooldown.get(key) - System.currentTimeMillis();
      // if the cooldown has not expired, block them from teleporting
      if (timeLeft > 0) {
        throw new CommandUsageException(String.format(plugin.getMessage("teleportcommand-cooldown-not-expired"), TimeFormatter.millisToLongDHMS(timeLeft)));
      } else {
        cooldown.remove(key);
      }
    }

    // if the player is attempting to teleport themselves
    if (getArguments().isEmpty()) {
      if (sender.hasPermission(this.getPermission(1))) {
        final UUID worldUUID = player.getWorld().getUID();
        List<HomeRecord> homes = database.findHomeRecordsByOwnerAndWorld(sender.getName(), worldUUID);
        if (!homes.isEmpty()) {
          cooldown.put(sender.getName().toLowerCase(), cooldownTime);
          player.teleport(homes.get(0).getLocation(server));
          return;
        } else {
          throw new CommandUsageException(plugin.getMessage("teleportcommand-no-home-set"));
        }
      } else {
        throw new CommandPermissionException(null, this.getPermission(1));
      }
    }

    final String playerName = (String) this.getArguments().get("player");
    final UUID worldUUID = (UUID) this.getArguments().get("worldId");

    // if the player is attempting to teleport themselves to another player's
    // home
    if (sender.hasPermission(this.getPermission(3))) {
      List<HomeRecord> homes = database.findHomeRecordsByOwnerAndWorld(playerName, worldUUID);
      if (!homes.isEmpty()) {
        cooldown.put(sender.getName().toLowerCase(), cooldownTime);
        player.teleport(homes.get(0).getLocation(server));
        sender.sendMessage(String.format(ChatColor.GREEN + this.plugin.getMessage("teleportcommand-teleported-to-home"), playerName));
        return;
      } else {
        throw new CommandUsageException(String.format(plugin.getMessage("teleportcommand-no-home-set-for-player"), playerName));
      }
    } else {
      throw new CommandPermissionException(plugin.getMessage("teleportcommand-hint-own-home-only"), this.getPermission(3));
    }

  }

  public void parseArguments(List<String> arguments, CommandSender sender) throws CommandArgumentException {
    final Map<String, Object> map = new HashMap<String, Object>();

    // do not allow ConsoleCommandSenders to use this command
    if (sender instanceof ConsoleCommandSender) {
      throw new CommandArgumentException(this.plugin.getMessage("command-is-player-only"), this.plugin.getMessage("teleportcomnmand-teleport-self-only"));
    }

    final UUID defaultWorldId = ((Player) sender).getWorld().getUID();

    // if the arguments are empty do nothing and assume the user is teleporting
    // themselves
    if (!arguments.isEmpty()) {
      // otherwise assume the player wants to teleport to someone else
      // get the name of the player we are teleporting to
      final String playerName = matchPlayerName(arguments.remove(0));
      map.put("player", playerName);

      // attempt to get the world of the player we are teleport to if provided.
      // if no world is provided use the worldName of the CommandSender
      if (arguments.isEmpty()) {
        map.put("worldId", defaultWorldId);
        // check to see if the world is loaded and if it is not default with
        // players current world.
      } else {
        final String worldName = arguments.remove(0);
        final World world = this.server.getWorld(worldName);
        if (world != null) {
          map.put("worldId", world.getUID());
        } else {
          throw new CommandArgumentException(this.plugin.getMessage("teleportcommand-invalid-world"), this.plugin.getMessage("teleportcommand-invalid-world-hint"));
        }
      }
    }

    this.setArguments(map);

  }

  private String matchPlayerName(String playerName) {
    List<Player> matches = this.server.matchPlayer(playerName);
    if (matches.isEmpty()) {
      return playerName;
    } else {
      return matches.get(0).getName();
    }
  }

  private void registerPermissions() {
    final String prefix = plugin.getDescription().getName().toLowerCase() + ".";
    final String wildcardDescription = String.format(plugin.getMessage("wildcard-permission-description"), this.getName());
    // create the wildcard permission
    Permission wildcard = new Permission(prefix + this.getName() + ".*", wildcardDescription, PermissionDefault.OP);
    wildcard.addParent(plugin.getRootPermission(), true);
    this.addPermission(wildcard);
    // create the base permission
    Permission base = new Permission(prefix + this.getName(), plugin.getMessage("teleportcommand-permission-description"), PermissionDefault.TRUE);
    base.addParent(wildcard, true);
    this.addPermission(base);
    // create a permission to allow players to ignore the cooldown
    Permission cooldown = new Permission(prefix + this.getName() + "." + plugin.getMessage("teleportcommand-ignore-cooldown-permission-name"), plugin.getMessage("teleportcommand-ignore-cooldown-permission-description"), PermissionDefault.OP);
    cooldown.addParent(wildcard, true);
    this.addPermission(cooldown);
    // create permission to enable players to teleport to other others
    Permission others = new Permission(prefix + this.getName() + "." + plugin.getMessage("teleportcommand-others-permission-name"), plugin.getMessage("teleportcommand-others-permission-description"), PermissionDefault.OP);
    others.addParent(wildcard, true);
    this.addPermission(others);
  }

}
