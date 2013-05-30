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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.hearthstone.Hearthstone;
import name.richardson.james.bukkit.hearthstone.Home;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;
import name.richardson.james.bukkit.utilities.command.AbstractCommand;
import name.richardson.james.bukkit.utilities.command.CommandMatchers;
import name.richardson.james.bukkit.utilities.command.CommandPermissions;
import name.richardson.james.bukkit.utilities.matchers.OfflinePlayerMatcher;
import name.richardson.james.bukkit.utilities.matchers.WorldMatcher;

@CommandMatchers(matchers = { OfflinePlayerMatcher.class, WorldMatcher.class })
@CommandPermissions(permissions = { "hearthstone.set", "hearthstone.set.own", "hearthstone.set.others" })
public class SetCommand extends AbstractCommand implements TabExecutor {

	private final Server server;

	private final EbeanServer database;

	/** The name of the players home we are setting */
	private String playerName;

	/** The player who is setting the home */
	private Player player;

	public SetCommand(final Hearthstone plugin) {
		super();
		this.server = plugin.getServer();
		this.database = plugin.getDatabase();
		Bukkit.getPluginManager().getPermission("hearthstone.set.own").setDefault(PermissionDefault.TRUE);
	}

	public void execute(final List<String> arguments, final CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(this.getMessage("error.player-command-sender-required"));
			return;
		}
		this.player = this.server.getPlayerExact(sender.getName());
		if (arguments.isEmpty()) {
			this.playerName = sender.getName();
		} else {
			this.playerName = arguments.remove(0);
		}
		if (this.hasPermission(sender) && this.createHome()) {
			this.createHome();
			sender.sendMessage(this.getMessage("notice.home-set"));
		} else {
			sender.sendMessage(this.getMessage("warning.permission-denied"));
		}
	}

	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
		if (this.isAuthorized(sender)) {
			this.execute(new LinkedList<String>(Arrays.asList(arguments)), sender);
		} else {
			sender.sendMessage(this.getMessage("warning.permission-denied"));
		}
		return true;
	}

	public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] arguments) {
		return this.onTabComplete(Arrays.asList(arguments), sender);
	}

	private boolean createHome() {
		final Home home = new Home(this.player.getLocation());
		// check if location is obstructed
		if (home.isObstructed()) {
			this.player.sendMessage(this.getMessage("error.location-obstructed"));
			return false;
		}
		// check if the location is buildable
		if (!home.isBuildable(this.player)) {
			this.player.sendMessage(this.getMessage("error.location-indestructible"));
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
		if (sender.hasPermission("hearthstone.set.own") && isSenderTargetingSelf) { return true; }
		if (sender.hasPermission("hearthstone.set.others") && !isSenderTargetingSelf) { return true; }
		return false;
	}

}
