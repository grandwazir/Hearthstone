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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.hearthstone.Hearthstone;
import name.richardson.james.bukkit.hearthstone.Home;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;
import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.CommandMatchers;
import name.richardson.james.bukkit.utilities.command.CommandPermissions;
import name.richardson.james.bukkit.utilities.matchers.OfflinePlayerMatcher;
import name.richardson.james.bukkit.utilities.matchers.WorldMatcher;

@CommandPermissions(permissions = { "hearthstone.teleport", "hearthstone.teleport.cooldown", "hearthstone.teleport.warmup", "hearthstone.teleport.own",
	"hearthstone.teleport.others" })
@CommandMatchers(matchers = { OfflinePlayerMatcher.class, WorldMatcher.class })
public class TeleportCommand extends AbstractCommand implements TabExecutor {

	private final Server server;

	private final EbeanServer database;

	// * The name of the player we are teleporting to *//
	private String playerName;

	// * The UID of the world we are teleporting to *//
	private UUID worldUUID;

	// * The player who is teleporting *//
	private Player player;

	public TeleportCommand(final Hearthstone plugin, final long warmup, final long cooldown) {
		super();
		this.server = plugin.getServer();
		this.database = plugin.getDatabase();
	}

	public void execute(final List<String> arguments, final CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.getMessage("error.player-command-sender-required"));
			return;
		}
		this.player = this.server.getPlayerExact(sender.getName());
		if (arguments.isEmpty()) {
			this.playerName = this.player.getName();
			this.worldUUID = this.player.getLocation().getWorld().getUID();
		} else {
			this.playerName = arguments.remove(0);
			if (!arguments.isEmpty()) {
				final World world = this.server.getWorld(arguments.remove(0));
				if (world == null) {
					sender.sendMessage(this.getMessage("error.world-not-loaded"));
					return;
				} else {
					this.worldUUID = world.getUID();
				}
			}
		}
		if (this.hasPermission(sender)) {
			final List<HomeRecord> homes = HomeRecord.findHomeRecordsByOwnerAndWorld(this.database, this.playerName, this.worldUUID);
			if (!homes.isEmpty()) {
				final Home home = new Home(homes.get(0).getLocation(this.server));
				new ScheduledTeleport(this.player, home);
			} else {
				this.player.sendMessage(this.getMessage("warning.no-home-set", this.playerName));
			}
		} else {
			sender.sendMessage(this.getMessage("error.permission-denied"));
		}
		this.player = null;
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
		if (this.isAuthorized(sender)) {
			this.execute(new LinkedList<String>(Arrays.asList(arguments)), sender);
		} else {
			sender.sendMessage(this.getMessage("error.permission-denied"));
		}
		return true;
	}

	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] arguments) {
		return this.onTabComplete(Arrays.asList(arguments), sender);
	}

	private boolean hasPermission(final CommandSender sender) {
		final boolean isSenderTargetingSelf = (this.playerName.equalsIgnoreCase(sender.getName())) ? true : false;
		if (sender.hasPermission("hearthstone.teleport.own") && isSenderTargetingSelf) { return true; }
		if (sender.hasPermission("hearthstone.teleport.others") && !isSenderTargetingSelf) { return true; }
		return false;
	}

}
