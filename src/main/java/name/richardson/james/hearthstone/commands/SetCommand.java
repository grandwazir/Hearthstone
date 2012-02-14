
package name.richardson.james.hearthstone.commands;

import java.util.List;
import java.util.Map;

import name.richardson.james.hearthstone.HearthstoneOld;
import name.richardson.james.hearthstone.exceptions.CommandIsPlayerOnlyException;
import name.richardson.james.hearthstone.exceptions.LocationBlockedException;
import name.richardson.james.hearthstone.exceptions.LocationIsNotBuildableException;
import name.richardson.james.hearthstone.exceptions.NoHomeFoundException;
import name.richardson.james.hearthstone.exceptions.NotEnoughArgumentsException;
import name.richardson.james.hearthstone.exceptions.PlayerNotAuthorisedException;
import name.richardson.james.hearthstone.persistant.HomeRecord;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class SetCommand extends Command {

  public SetCommand(final HearthstoneOld plugin) {
    super(plugin);
    registerPermission(permission, plugin.getMessage(className + "PermissionDescription"), PermissionDefault.TRUE);
  }

  @Override
  public void execute(final CommandSender sender, final Map<String, String> arguments) throws PlayerNotAuthorisedException, CommandIsPlayerOnlyException,
      LocationBlockedException, LocationIsNotBuildableException {
    if (sender instanceof ConsoleCommandSender)
      throw new CommandIsPlayerOnlyException();
    final Player player = (Player) sender;

    if (plugin.isLocationBlocked(player.getLocation()))
      throw new LocationBlockedException();
    if (!plugin.isLocationBuildable(player, player.getLocation()))
      throw new LocationIsNotBuildableException();

    // delete the old home on this world if applicable
    try {
      final HomeRecord record = HomeRecord.findFirst(player);
      record.destroy();
    } catch (NoHomeFoundException e) {
      // do nothing we can ignore this exception safely.
    }
    
    HomeRecord.create(player);
    sender.sendMessage(ChatColor.GREEN + plugin.getMessage("SetCommandSuccessful"));
  }

  @Override
  protected Map<String, String> parseArguments(final List<String> arguments) throws NotEnoughArgumentsException {
    return null;
  }

}
