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
package name.richardson.james.hearthstone;

import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ScheduledTeleport implements Runnable {

  private Player player;
  
  private Location lastLocation;

  private int health;

  private Location homeLocation;

  private Map<String, Long> cooldownTracker;

  private long cooldownTime;

  public ScheduledTeleport(Player player, Location location, Map<String, Long> cooldownTracker, long cooldownTime) {
    this.player = player;
    this.health = player.getHealth();
    this.lastLocation = player.getLocation().clone();
    this.homeLocation = location;
    this.cooldownTracker = cooldownTracker;
    this.cooldownTime = cooldownTime;
  }
  
  public void run() {
    if (!(hasPlayerMoved()) && !(hasPlayerTakenDamage())) {
      this.player.getLocation().getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0);
      this.player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
      this.player.teleport(homeLocation);
      this.cooldownTracker.put(player.getName(), cooldownTime);
      this.player.getLocation().getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0);
      this.player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 0);
    } else {
      this.player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
    }
  }
  
  private boolean hasPlayerTakenDamage() {
    return player.getHealth() != this.health;
  }
  
  private boolean hasPlayerMoved() {
    System.out.format("Distance: %s", String.valueOf(player.getLocation().distance(this.lastLocation)));
    return player.getLocation().distance(this.lastLocation) >= 1;
  }
  
}
