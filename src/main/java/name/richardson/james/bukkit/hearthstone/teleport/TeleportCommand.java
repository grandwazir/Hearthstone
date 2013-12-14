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
package name.richardson.james.bukkit.hearthstone.teleport;

import java.util.List;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.context.CommandContext;
import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
import name.richardson.james.bukkit.utilities.formatters.DefaultColourFormatter;
import name.richardson.james.bukkit.utilities.localisation.Localisation;
import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;

import name.richardson.james.bukkit.hearthstone.Home;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;

public class TeleportCommand extends AbstractCommand {

	public static final String PERMISSION_ALL = "hearthstone.teleport";
	public static final String PERMISSION_COOLDOWN = "hearthstone.teleport.cooldown";
	public static final String PERMISSION_WARMUP = "hearthstone.teleport.warmup";
	public static final String PERMISSION_OWN = "hearthstone.teleport.own";
	public static final String PERMISSION_OTHERS = "hearthstone.teleport.others";

	private static final String PLAYER_COMMAND_SENDER_REQUIRED_KEY = "player-command-sender-required";
	private static final String WORLD_NOT_LOADED_KEY = "world-not-loaded";
	private static final String NO_HOME_SET_KEY = "no-home-set";
	private static final String PERMISSION_DENIED_KEY = "permission-denied";

	private final Localisation localisation = new ResourceBundleByClassLocalisation(TeleportCommand.class);
	private final ColourFormatter colourFormatter = new DefaultColourFormatter();

	private final Server server;

	private final EbeanServer database;

	// * The name of the player we are teleporting to *//
	private String playerName;

	// * The UID of the world we are teleporting to *//
	private UUID worldUUID;

	// * The player who is teleporting *//
	private Player player;

	public TeleportCommand(Server server, EbeanServer database, final long warmup, final long cooldown) {
		this.server = server;
		this.database = database;
		ScheduledTeleport.setCooldownTime(cooldown);
		ScheduledTeleport.setWarmupTime(warmup);
	}

	/**
	 * Execute a command using the provided {@link name.richardson.james.bukkit.utilities.command.context.CommandContext}.
	 *
	 * @param commandContext the command context to execute this command within.
	 * @since 6.0.0
	 */
	@Override
	public void execute(CommandContext commandContext) {
		CommandSender sender = commandContext.getCommandSender();
		if (!(sender instanceof Player)) {
			sender.sendMessage(colourFormatter.format(localisation.getMessage(PLAYER_COMMAND_SENDER_REQUIRED_KEY), ColourFormatter.FormatStyle.ERROR));
			return;
		}
		this.player = this.server.getPlayerExact(sender.getName());
		if (commandContext.size() == 0) {
			this.playerName = this.player.getName();
			this.worldUUID = this.player.getLocation().getWorld().getUID();
		} else {
			this.playerName = commandContext.getString(0);
			if (commandContext.has(1)) {
				final World world = this.server.getWorld(commandContext.getString(1));
				if (world == null) {
					sender.sendMessage(colourFormatter.format(localisation.getMessage(WORLD_NOT_LOADED_KEY), ColourFormatter.FormatStyle.ERROR));
					return;
				} else {
					this.worldUUID = world.getUID();
				}
			} else {
				this.worldUUID = this.player.getWorld().getUID();
			}
		}
		if (this.hasPermission(sender)) {
			final List<HomeRecord> homes = HomeRecord.findHomeRecordsByOwnerAndWorld(this.database, this.playerName, this.worldUUID);
			if (!homes.isEmpty()) {
				final Home home = new Home(homes.get(0).getLocation(this.server));
				new ScheduledTeleport(this.player, home);
			} else {
				this.player.sendMessage(colourFormatter.format(localisation.getMessage(NO_HOME_SET_KEY), ColourFormatter.FormatStyle.WARNING, playerName));
			}
		} else {
			sender.sendMessage(colourFormatter.format(localisation.getMessage(PERMISSION_DENIED_KEY), ColourFormatter.FormatStyle.ERROR));
		}
		this.player = null;
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
		if (permissible.hasPermission(PERMISSION_OWN)) return true;
		if (permissible.hasPermission(PERMISSION_OTHERS)) return true;
		return false;
	}

	private boolean hasPermission(final CommandSender sender) {
		final boolean isSenderTargetingSelf = (this.playerName.equalsIgnoreCase(sender.getName())) ? true : false;
		if (sender.hasPermission(PERMISSION_OWN) && isSenderTargetingSelf) { return true; }
		if (sender.hasPermission(PERMISSION_OTHERS) && !isSenderTargetingSelf) { return true; }
		return false;
	}

}
