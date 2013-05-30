package name.richardson.james.bukkit.hearthstone.general;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import name.richardson.james.bukkit.hearthstone.Hearthstone;
import name.richardson.james.bukkit.hearthstone.teleport.TeleportCommand;

public class HomeCommand implements TabExecutor {

	private final TeleportCommand teleport;
	private final SetCommand set;

	public HomeCommand(final Hearthstone plugin, final TeleportCommand teleport, final SetCommand set) {
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

	public List<String> onTabComplete(final CommandSender arg0, final Command arg1, final String arg2, final String[] arg3) {
		return null;
	}

	private String[] prepareArguments(final String[] args) {
		final String[] arguments = new String[args.length - 1];
		System.arraycopy(args, 1, arguments, 0, args.length - 1);
		return arguments;
	}

}
