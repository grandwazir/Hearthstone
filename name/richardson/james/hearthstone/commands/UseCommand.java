
package name.richardson.james.hearthstone.commands;

import java.util.List;
import java.util.Map;

import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.exceptions.CommandIsPlayerOnlyException;
import name.richardson.james.hearthstone.exceptions.CooldownNotExpiredException;
import name.richardson.james.hearthstone.exceptions.LocationBlockedException;
import name.richardson.james.hearthstone.exceptions.NoHomeFoundException;
import name.richardson.james.hearthstone.exceptions.NotEnoughArgumentsException;
import name.richardson.james.hearthstone.exceptions.PlayerNotAuthorisedException;
import name.richardson.james.hearthstone.persistant.HomeRecord;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class UseCommand extends Command {

  public UseCommand(final Hearthstone plugin) {
    super(plugin);
    registerPermission(permission, plugin.getMessage(className + "PermissionDescription"), PermissionDefault.TRUE);
    registerPermission(plugin.getName().toLowerCase() + "." + plugin.getMessage("CooldownPermission"), plugin.getMessage("CooldownPermissionDescription"),
        PermissionDefault.TRUE);
  }

  @Override
  public void execute(final CommandSender sender, final Map<String, String> arguments) throws PlayerNotAuthorisedException, CommandIsPlayerOnlyException,
      NoHomeFoundException, LocationBlockedException, CooldownNotExpiredException {
    if (sender instanceof ConsoleCommandSender)
      throw new CommandIsPlayerOnlyException();
    final Player player = (Player) sender;
    final HomeRecord home = HomeRecord.findFirst(player);
    plugin.teleportHome(home, player);
    sender.sendMessage(ChatColor.GREEN + plugin.getMessage(className + "Successful"));
  }

  @Override
  protected Map<String, String> parseArguments(final List<String> arguments) throws NotEnoughArgumentsException {
    return null;
  }

}
