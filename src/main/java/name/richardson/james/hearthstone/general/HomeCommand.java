package name.richardson.james.hearthstone.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.hearthstone.Hearthstone;

public class HomeCommand implements CommandExecutor {

	private final AbstractCommand teleport;
	private final AbstractCommand set;

	public HomeCommand(final Hearthstone plugin, final AbstractCommand teleport, final AbstractCommand set) {
		this.teleport = teleport;
		this.set = set;
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (args.length == 0) {
			this.teleport.onCommand(sender, command, label, args);
		} else
			if (args[0].equalsIgnoreCase(this.set.getName())) {
				String[] arguments;
				arguments = this.prepareArguments(args);
				this.set.onCommand(sender, command, label, arguments);
			}
		return true;
	}

	private String[] prepareArguments(final String[] args) {
		final String[] arguments = new String[args.length - 1];
		System.arraycopy(args, 1, arguments, 0, args.length - 1);
		return arguments;
	}

}
