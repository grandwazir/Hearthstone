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
package name.richardson.james.hearthstone.general;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.CommandMatchers;
import name.richardson.james.bukkit.utilities.command.CommandPermissions;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
import name.richardson.james.bukkit.utilities.matchers.OfflinePlayerMatcher;
import name.richardson.james.bukkit.utilities.matchers.WorldMatcher;
import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.HomeRecord;
import name.richardson.james.hearthstone.scheduler.ScheduledTeleport;

@CommandPermissions(permissions = { "hearthstone.teleport", "hearthstone.teleport.cooldown", "hearthstone.teleport.warmup", "hearthstone.teleport.own",
	"hearthstone.teleport.others" })
@CommandMatchers(matchers = { OfflinePlayerMatcher.class, WorldMatcher.class })
public class TeleportCommand extends AbstractCommand implements TabExecutor {

	private final Server server;

	private final EbeanServer database;

	private final Map<String, Long> cooldownTracker;

	// * The name of the player we are teleporting to *//
	private String playerName;

	// * The UID of the world we are teleporting to *//
	private UUID worldUUID;

	// * The player who is teleporting *//
	private Player player;

	// * The cooldown to apply to the teleporting player *//
	private final long cooldownTime;

	private final long warmupTicks;

	private final Plugin plugin;

	private final String warmupTime;

	public TeleportCommand(final Hearthstone plugin, final long warmup, final long cooldown) {
		super();
		this.server = plugin.getServer();
		this.plugin = plugin;
		this.warmupTime = TimeFormatter.millisToLongDHMS(warmup);
		this.warmupTicks = (warmup / 1000) * 20;
		this.database = plugin.getDatabase();
		this.cooldownTracker = plugin.getCooldownTracker();
		this.cooldownTime = cooldown;
	}

	public void execute(final List<String> arguments, final CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.getMessage("misc.error.not-available-to-console-command-sender"));
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
					sender.sendMessage(this.getMessage("misc.error.world-not-loaded"));
					return;
				} else {
					this.worldUUID = world.getUID();
				}
			}
		}
		if (this.hasPermission(sender)) {
			this.teleportPlayer();
		} else {
			sender.sendMessage(this.getMessage("misc.error.permission-denied"));

		}
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
		if (this.isAuthorized(sender)) {
			this.execute(new LinkedList<String>(Arrays.asList(arguments)), sender);
		} else {
			sender.sendMessage(this.getMessage("misc.warning.permission-denied"));
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

	private void teleportPlayer() {
		final List<HomeRecord> homes = HomeRecord.findHomeRecordsByOwnerAndWorld(this.database, this.playerName, this.worldUUID);
		if (!homes.isEmpty()) {
			final Home location = new Home(homes.get(0).getLocation(this.server));
			if (location.isObstructed()) {
				this.player.sendMessage(this.getMessage("shared.error.location-obstructed"));
			}
			this.cooldownTracker.put(this.playerName, System.currentTimeMillis() + this.cooldownTime);
			final ScheduledTeleport task = new ScheduledTeleport(this.player, location, this.cooldownTracker, this.cooldownTime);
			if (!this.player.hasPermission("hearthstone.teleport.warmup")) {
				this.server.getScheduler().scheduleSyncDelayedTask(this.plugin, task);
			} else {
				this.server.getScheduler().scheduleSyncDelayedTask(this.plugin, task, this.warmupTicks);
				this.player.sendMessage(this.getMessage("teleportcommand.warming-up", this.warmupTime));
			}
		} else {
			this.player.sendMessage(this.getMessage("teleportcommand.no-home-set", this.playerName));
		}
	}

}
