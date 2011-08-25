
package name.richardson.james.hearthstone;

import java.util.HashMap;

import name.richardson.james.hearthstone.commands.UseCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {

  private final HashMap<String, CommandExecutor> commands = new HashMap<String, CommandExecutor>();
  private final UseCommand defaultCommand;

  public CommandManager(final Hearthstone plugin) {
    super();
    defaultCommand = new UseCommand(plugin);
  }

  public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
    if (args.length != 0) {
      if (commands.containsKey(args[0])) {
        commands.get(args[0]).onCommand(sender, command, label, args);
        return true;
      }
    } else {
      defaultCommand.onCommand(sender, command, label, args);
    }
    return true;
  }

  public void registerCommand(final String command, final CommandExecutor executor) {
    commands.put(command, executor);
  }

}
