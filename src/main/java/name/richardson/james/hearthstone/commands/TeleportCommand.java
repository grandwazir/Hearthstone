
package name.richardson.james.hearthstone.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import name.richardson.james.hearthstone.HearthstoneOld;
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

public class TeleportCommand extends Command {

  public TeleportCommand(final HearthstoneOld plugin) {
    super(plugin);
    registerPermission(permission, plugin.getMessage(className + "PermissionDescription"), PermissionDefault.OP);
  }

  @Override
  public void execute(final CommandSender sender, final Map<String, String> arguments) throws PlayerNotAuthorisedException, CommandIsPlayerOnlyException,
      NoHomeFoundException, LocationBlockedException, CooldownNotExpiredException {
    if (sender instanceof ConsoleCommandSender)
      throw new CommandIsPlayerOnlyException();
    final UUID worldUUID;
    final Player player = (Player) sender;

    if (arguments.containsKey("world")) {
      worldUUID = plugin.getServer().getWorld(arguments.get("world")).getUID();
    } else {
      worldUUID = player.getLocation().getWorld().getUID();
    }

    final HomeRecord home = HomeRecord.findFirst(arguments.get("player"), worldUUID);
    plugin.teleportHome(home, player);
    sender.sendMessage(String.format(ChatColor.GREEN + plugin.getMessage(className + "Successful"), arguments.get("player")));
  }

  @Override
  protected Map<String, String> parseArguments(final List<String> arguments) throws NotEnoughArgumentsException {
    final Map<String, String> m = new HashMap<String, String>();

    for (final String argument : arguments) {
      if (argument.startsWith("p:")) {
        m.put("player", argument.replace("p:", ""));
      } else if (argument.startsWith("w:")) {
        m.put("world", argument.replace("w:", ""));
      }
    }

    if (!m.containsKey("player")) {
      throw new NotEnoughArgumentsException();
    } else {
      return m;
    }
  }

}
