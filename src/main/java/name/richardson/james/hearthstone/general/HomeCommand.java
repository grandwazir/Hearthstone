package name.richardson.james.hearthstone.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.hearthstone.Hearthstone;

public class HomeCommand implements CommandExecutor {

  private final AbstractCommand teleport; 
  private final AbstractCommand set;
  private final Hearthstone plugin; 
  
  public HomeCommand(Hearthstone plugin, AbstractCommand teleport, AbstractCommand set) {
    this.teleport = teleport;
    this.set = set;
    this.plugin = plugin;
  }
  
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      this.teleport.onCommand(sender, command, label, args);
    } else if (args[0].equalsIgnoreCase(this.set.getName())) {
      String[] arguments;
      arguments = prepareArguments(args);
      this.set.onCommand(sender, command, label, arguments);
    } 
    return true;
  }
  
  private String[] prepareArguments(String[] args) {
    String[] arguments = new String[args.length - 1];
    System.arraycopy(args, 1, arguments, 0, args.length - 1);
    return arguments;
  }
  
}
