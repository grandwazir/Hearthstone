/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * HomeRecord.java is part of Hearthstone.
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

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotNull;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

@Entity()
@Table(name = "hearthstone_homes")
public class HomeRecord {

  @Id
  private long createdAt;

  @NotNull
  private String createdBy;

  @NotNull
  private float pitch;

  @NotNull
  private UUID worldUUID;

  @NotNull
  private double x;

  @NotNull
  private double y;

  @NotNull
  private float yaw;

  @NotNull
  private double z;

  public long getCreatedAt() {
    return this.createdAt;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public Location getLocation(final Server server) {
    final World world = server.getWorld(this.worldUUID);
    return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
  }

  public float getPitch() {
    return this.pitch;
  }

  public UUID getWorldUUID() {
    return this.worldUUID;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public float getYaw() {
    return this.yaw;
  }

  public double getZ() {
    return this.z;
  }

  public void setCreatedAt(final long createdAt) {
    this.createdAt = createdAt;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public void setPitch(final float pitch) {
    this.pitch = pitch;
  }

  public void setWorldUUID(final UUID worldUUID) {
    this.worldUUID = worldUUID;
  }

  public void setX(final double x) {
    this.x = x;
  }

  public void setY(final double y) {
    this.y = y;
  }

  public void setYaw(final float yaw) {
    this.yaw = yaw;
  }

  public void setZ(final double z) {
    this.z = z;
  }

}
