/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * ScheduledTeleport.java is part of Hearthstone.
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

import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
import name.richardson.james.bukkit.utilities.formatters.DefaultColourFormatter;
import name.richardson.james.bukkit.utilities.formatters.PreciseDurationTimeFormatter;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;
import name.richardson.james.bukkit.utilities.localisation.Localisation;
import name.richardson.james.bukkit.utilities.localisation.ResourceBundleByClassLocalisation;

import name.richardson.james.bukkit.hearthstone.Home;

//TODO This is a mixture of static and non-static and needs to be refactored at some point.
public class ScheduledTeleport implements Runnable {

	public static final int MOVEMENT_THRESHOLD = 2;
	public static final Sound TELEPORT_FAILED_SOUND = Sound.ITEM_BREAK;
	public static final Effect TELEPORT_SUCCESS_EFFECT = Effect.ENDER_SIGNAL;
	public static final Sound TELEPORT_SUCCESS_SOUND = Sound.ENDERMAN_TELEPORT;

	private static final BukkitScheduler BUKKIT_SCHEDULER = Bukkit.getScheduler();
	private static final Map<String, Long> COOLDOWN_TRACKER = new Hashtable<String, Long>();
	private static final String TELEPORT_COOLDOWN_KEY = "teleport-cooldown";
	private static final String PLAYER_MOVED_TOO_FAR_KEY = "player-moved-too-far";
	private static final String PLAYER_TAKEN_DAMAGE_KEY = "error.player-taken-damage";
	private static final String LOCATION_OBSTRUCTED_KEY = "location-obstructed";
	private static final String TELEPORT_WARMUP_KEY = "teleport-warmup";

	private static long cooldown = 0;
	private static Plugin plugin = null;
	private static long warmup = 0;
	private static String warmupTime;

	private final Localisation localisation = new ResourceBundleByClassLocalisation(ScheduledTeleport.class);
	private final ColourFormatter colourFormatter = new DefaultColourFormatter();
	private static final TimeFormatter timeFormatter = new PreciseDurationTimeFormatter();

	public static Plugin getPlugin() {
		if (ScheduledTeleport.plugin == null) {
			ScheduledTeleport.plugin = Bukkit.getPluginManager().getPlugin("Hearthstone");
		}
		return ScheduledTeleport.plugin;
	}

	public static void setCooldownTime(final long milliseconds) {
		ScheduledTeleport.cooldown = milliseconds;
	}

	public static void setWarmupTime(final long milliseconds) {
		ScheduledTeleport.warmup = milliseconds;
		ScheduledTeleport.warmupTime = timeFormatter.getHumanReadableDuration(warmup);
	}

	private final int health;

	private final Home home;

	private final Location lastLocation;

	private final Player player;

	public ScheduledTeleport(final Player player, final Home location) {
		this.player = player;
		this.health = player.getHealth();
		this.lastLocation = player.getLocation().clone();
		this.home = location;
		if (!this.hasCooldown()) {
			this.schedule();
		}
	}

	public void run() {
		if (!(this.hasPlayerMoved()) && !(this.hasPlayerTakenDamage()) && !(this.isLocationObstructed())) {
			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), TELEPORT_SUCCESS_EFFECT, 0);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), TELEPORT_SUCCESS_SOUND, 1, 0);
			this.player.teleport(this.home.getLocation());
			this.setCooldown();
			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), TELEPORT_SUCCESS_EFFECT, 0);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), TELEPORT_SUCCESS_SOUND, 1, 0);
		} else {
			this.player.playSound(this.player.getLocation(), TELEPORT_FAILED_SOUND, 1, 1);
		}
	}

	private boolean hasCooldown() {
		if (!COOLDOWN_TRACKER.containsKey(this.player.getName())) { return false; }
		long timeRemaining = COOLDOWN_TRACKER.get(this.player.getName()) - System.currentTimeMillis();
		timeRemaining = Math.round(timeRemaining / 1000) * 1000;
		if (timeRemaining > 0) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(TELEPORT_COOLDOWN_KEY), ColourFormatter.FormatStyle.WARNING, timeFormatter.getHumanReadableDuration(timeRemaining)));
			return true;
		} else {
			return false;
		}
	}

	private boolean hasPlayerMoved() {
		if (this.player.getLocation().distance(this.lastLocation) >= MOVEMENT_THRESHOLD) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(PLAYER_MOVED_TOO_FAR_KEY), ColourFormatter.FormatStyle.ERROR));
			return true;
		} else {
			return false;
		}
	}

	private boolean hasPlayerTakenDamage() {
		if (this.player.getHealth() < this.health) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(PLAYER_TAKEN_DAMAGE_KEY), ColourFormatter.FormatStyle.ERROR));
			return true;
		} else {
			return false;
		}
	}

	private boolean isLocationObstructed() {
		if (this.home.isObstructed()) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(LOCATION_OBSTRUCTED_KEY), ColourFormatter.FormatStyle.ERROR));
			return true;
		} else {
			return false;
		}
	}

	private void schedule() {
		if (this.player.hasPermission("hearthstone.teleport.warmup")) {
			this.player.sendMessage(colourFormatter.format(localisation.getMessage(TELEPORT_WARMUP_KEY), ColourFormatter.FormatStyle.INFO, ScheduledTeleport.warmupTime));
			BUKKIT_SCHEDULER.scheduleSyncDelayedTask(ScheduledTeleport.getPlugin(), this, ((ScheduledTeleport.warmup / 1000) * 20));
		} else {
			BUKKIT_SCHEDULER.scheduleSyncDelayedTask(plugin, this);
		}
	}

	private void setCooldown() {
		if (this.player.hasPermission("hearthstone.teleport.cooldown")) {
			COOLDOWN_TRACKER.put(this.player.getName(), System.currentTimeMillis() + ScheduledTeleport.cooldown);
		}
	}

}
