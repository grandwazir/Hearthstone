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
package name.richardson.james.hearthstone.scheduler;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import name.richardson.james.hearthstone.general.Home;

public class ScheduledTeleport implements Runnable {

	private final Player player;

	private final Location lastLocation;

	private final int health;

	private final Home homeLocation;

	private final Map<String, Long> cooldownTracker;

	private final long cooldownTime;

	public ScheduledTeleport(final Player player, final Home location, final Map<String, Long> cooldownTracker, final long cooldownTime) {
		this.player = player;
		this.health = player.getHealth();
		this.lastLocation = player.getLocation().clone();
		this.homeLocation = location;
		this.cooldownTracker = cooldownTracker;
		this.cooldownTime = cooldownTime;
	}

	public void run() {
		if (!(this.hasPlayerMoved()) && !(this.hasPlayerTakenDamage())) {
			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), Effect.ENDER_SIGNAL, 0);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
			this.player.teleport(this.homeLocation.getLocation());
			this.cooldownTracker.put(this.player.getName(), this.cooldownTime);
			this.player.getLocation().getWorld().playEffect(this.player.getLocation(), Effect.ENDER_SIGNAL, 0);
			this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
		} else {
			this.player.playSound(this.player.getLocation(), Sound.ITEM_BREAK, 1, 1);
		}
	}

	private boolean hasPlayerMoved() {
		return this.player.getLocation().distance(this.lastLocation) >= 1;
	}

	private boolean hasPlayerTakenDamage() {
		return this.player.getHealth() != this.health;
	}

}
