/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * SetCommand.java is part of Hearthstone.
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
package name.richardson.james.bukkit.hearthstone.general;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.context.CommandContext;
import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
import name.richardson.james.bukkit.utilities.formatters.DefaultColourFormatter;
import name.richardson.james.bukkit.utilities.formatters.PreciseDurationTimeFormatter;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
import name.richardson.james.bukkit.utilities.localisation.Localisation;
import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;

import name.richardson.james.bukkit.hearthstone.Home;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;

public class SetCommand extends AbstractCommand {

	public static final String PERMISSION_ALL = "hearthstone.set";
	public static final String PERMISSION_OWN = "hearthstone.set.own";
	public static final String PERMISSION_OTHERS = "hearthstone.set.others";

	private static final String PLAYER_COMMAND_SENDER_REQUIRED_KEY = "player-command-sender-required";
	private static final String HOME_SET_KEY = "home-set";
	private static final String PERMISSION_DENIED_KEY = "permission-denied";
	private static final String LOCATION_OBSTRUCTED_KEY = "location-obstructed";
	private static final String LOCATION_INDESTRUCTIBLE_KEY = "location-indestructible";

	private final Localisation localisation = new ResourceBundleByClassLocalisation(SetCommand.class);
	private final ColourFormatter colourFormatter = new DefaultColourFormatter();
	private final TimeFormatter timeFormatter = new PreciseDurationTimeFormatter();

	private final Server server;
	private final EbeanServer database;

	private String playerName;
	private Player player;

	public SetCommand(Server server, EbeanServer database) {
		this.server = server;
		this.database = database;
	}

	/**
	 * Execute a command using the provided {@link name.richardson.james.bukkit.utilities.command.context.CommandContext}.
	 *
	 * @param commandContext the command context to execute this command within.
	 * @since 6.0.0
	 */
	@Override
	public void execute(CommandContext commandContext) {
		if (!(commandContext.getCommandSender() instanceof Player)) {
			commandContext.getCommandSender().sendMessage(colourFormatter.format(localisation.getMessage(PLAYER_COMMAND_SENDER_REQUIRED_KEY), ColourFormatter.FormatStyle.ERROR));
			return;
		}
		this.player = this.server.getPlayerExact(commandContext.getCommandSender().getName());
		if (commandContext.size() == 0) {
			this.playerName = commandContext.getCommandSender().getName();
		} else {
			this.playerName = commandContext.getString(0);
		}
		if (this.hasPermission(commandContext.getCommandSender()) && this.createHome()) {
			this.createHome();
			commandContext.getCommandSender().sendMessage(colourFormatter.format(localisation.getMessage(HOME_SET_KEY), ColourFormatter.FormatStyle.INFO, this.playerName));
		} else {
			commandContext.getCommandSender().sendMessage(colourFormatter.format(localisation.getMessage(PERMISSION_DENIED_KEY), ColourFormatter.FormatStyle.ERROR));
		}
	}

	/**
	 * Returns {@code true} if the user is authorised to use this command.
	 * <p/>
	 * Authorisation does not guarantee that the user may use all the features associated with a command.
	 *
	 * @param permissible the permissible requesting authorisation
	 * @return {@code true} if the user is authorised; {@code false} otherwise
	 * @since 6.0.0
	 */
	@Override
	public boolean isAuthorised(Permissible permissible) {
		if (permissible.hasPermission(PERMISSION_ALL)) return true;
		if (permissible.hasPermission(PERMISSION_OTHERS)) return true;
		if (permissible.hasPermission(PERMISSION_OWN)) return true;
		return false;
	}

	private boolean createHome() {
		final Home home = new Home(this.player.getLocation());
		// check if location is obstructed
		if (home.isObstructed()) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(LOCATION_OBSTRUCTED_KEY), ColourFormatter.FormatStyle.ERROR));
			return false;
		}
		// check if the location is buildable
		if (!home.isBuildable(this.player)) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(LOCATION_INDESTRUCTIBLE_KEY), ColourFormatter.FormatStyle.ERROR));
			return false;
		}
		// delete any existing homes
		HomeRecord.deleteHomes(this.database, this.playerName, home.getLocation().getWorld().getUID());
		// create the home
		final HomeRecord record = new HomeRecord();
		record.setCreatedAt(System.currentTimeMillis());
		record.setCreatedBy(this.playerName);
		record.setX(home.getLocation().getX());
		record.setY(home.getLocation().getY());
		record.setZ(home.getLocation().getZ());
		record.setYaw(home.getLocation().getYaw());
		record.setPitch(home.getLocation().getPitch());
		record.setWorldUUID(home.getLocation().getWorld().getUID());
		this.database.save(record);
		return true;
	}

	private boolean hasPermission(final CommandSender sender) {
		final boolean isSenderTargetingSelf = (this.playerName.equalsIgnoreCase(sender.getName())) ? true : false;
		if (sender.hasPermission(PERMISSION_OWN) && isSenderTargetingSelf) { return true; }
		if (sender.hasPermission(PERMISSION_OTHERS) && !isSenderTargetingSelf) { return true; }
		return false;
	}

}
