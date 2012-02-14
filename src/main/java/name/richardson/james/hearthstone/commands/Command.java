
package name.richardson.james.hearthstone.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import name.richardson.james.hearthstone.HearthstoneOld;
import name.richardson.james.hearthstone.exceptions.CommandIsPlayerOnlyException;
import name.richardson.james.hearthstone.exceptions.CooldownNotExpiredException;
import name.richardson.james.hearthstone.exceptions.LocationBlockedException;
import name.richardson.james.hearthstone.exceptions.LocationIsNotBuildableException;
import name.richardson.james.hearthstone.exceptions.NoHomeFoundException;
import name.richardson.james.hearthstone.exceptions.NotEnoughArgumentsException;
import name.richardson.james.hearthstone.exceptions.PlayerNotAuthorisedException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public abstract class Command implements CommandExecutor {

  protected String className;
  protected String description;
  protected String name;
  protected String permission;
  protected HearthstoneOld plugin;
  protected String usage;

  public Command(final HearthstoneOld plugin) {
    super();
    this.plugin = plugin;
    className = this.getClass().getSimpleName();
    name = this.plugin.getMessage(className + "Name");
    description = this.plugin.getMessage(className + "Description");
    usage = this.plugin.getMessage(className + "Usage");
    permission = this.plugin.getName().toLowerCase() + "." + name;
  }

  public abstract void execute(CommandSender sender, Map<String, String> arguments) throws PlayerNotAuthorisedException, CommandIsPlayerOnlyException,
      NoHomeFoundException, LocationBlockedException, LocationIsNotBuildableException, CooldownNotExpiredException;

  public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
    try {
      authorisePlayer(sender, permission);
      final LinkedList<String> arguments = new LinkedList<String>();
      arguments.addAll(Arrays.asList(args));
      final Map<String, String> parsedArguments = parseArguments(arguments);
      execute(sender, parsedArguments);
    } catch (final PlayerNotAuthorisedException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("PlayerNotAuthorisedException"));
    } catch (final NotEnoughArgumentsException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("NotEnoughArgumentsException"));
      sender.sendMessage(ChatColor.YELLOW + usage);
    } catch (final CommandIsPlayerOnlyException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("CommandIsPlayerOnlyException"));
    } catch (final NoHomeFoundException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("NoHomeFoundException"));
    } catch (final LocationBlockedException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("LocationBlockedException"));
    } catch (final LocationIsNotBuildableException e) {
      sender.sendMessage(ChatColor.RED + plugin.getMessage("LocationIsNotBuildableException"));
    } catch (final CooldownNotExpiredException e) {
      final int minutes = (int) ((e.getCooldownTime() - System.currentTimeMillis()) / 1000 / 60);
      sender.sendMessage(ChatColor.RED + plugin.getMessage("CooldownNotExpiredException"));
      sender.sendMessage(String.format(ChatColor.YELLOW + plugin.getMessage("CooldownNotExpiredExceptionHint"), minutes));
    }
    return true;
  }

  protected void authorisePlayer(final CommandSender sender, String node) throws PlayerNotAuthorisedException {
    node = node.toLowerCase();
    if (sender instanceof ConsoleCommandSender) {
      return;
    } else {
      final Player player = (Player) sender;
      if (player.hasPermission(node) || player.hasPermission("hearthstone.*")) { return; }
    }
    throw new PlayerNotAuthorisedException();
  }

  protected String getSenderName(final CommandSender sender) {
    if (sender instanceof ConsoleCommandSender)
      return "console";
    else {
      final Player player = (Player) sender;
      return player.getName();
    }
  }

  protected abstract Map<String, String> parseArguments(List<String> arguments) throws NotEnoughArgumentsException;

  protected void registerPermission(final String name, final String description, final PermissionDefault defaultValue) {
    final Permission permission = new Permission(name, description, defaultValue);
    plugin.getPluginManager().addPermission(permission);
  }

}
