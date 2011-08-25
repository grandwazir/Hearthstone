
package name.richardson.james.hearthstone.persistant;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.exceptions.NoHomeFoundException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avaje.ebean.ExampleExpression;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "hearthstone_homes")
public class HomeRecord {

  @Id
  private long createdAt;

  @NotNull
  private String createdBy;

  @NotNull
  private long lastAccessed;

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

  static public HomeRecord create(final Player player) {
    final HomeRecord record = new HomeRecord();
    final long currentTime = System.currentTimeMillis();
    final Location location = player.getLocation();
    record.setCreatedAt(currentTime);
    record.setCreatedBy(player.getName());
    record.setX(location.getX());
    record.setY(location.getY());
    record.setZ(location.getZ());
    record.setYaw(location.getYaw());
    record.setPitch(location.getPitch());
    record.setLastAccessed(currentTime);
    record.setWorldUUID(player.getLocation().getWorld().getUID());
    Hearthstone.getDb().save(record);
    return record;
  }

  static public HomeRecord create(final Player player, final Location location) {
    final HomeRecord record = new HomeRecord();
    final long currentTime = System.currentTimeMillis();
    record.setCreatedAt(currentTime);
    record.setCreatedBy(player.getName());
    record.setX(location.getX());
    record.setY(location.getY());
    record.setZ(location.getZ());
    record.setYaw(location.getYaw());
    record.setPitch(location.getPitch());
    record.setLastAccessed(currentTime);
    record.setWorldUUID(player.getLocation().getWorld().getUID());
    Hearthstone.getDb().save(record);
    return record;
  }

  static public void destroy(final HomeRecord record) {
    Hearthstone.getDb().delete(record);
  }

  static public int destroy(final List<HomeRecord> records) {
    return Hearthstone.getDb().delete(records);
  }

  static public List<HomeRecord> find(final Player player) {
    // create the example
    final HomeRecord example = new HomeRecord();
    example.setCreatedBy(player.getName());
    example.setWorldUUID(player.getLocation().getWorld().getUID());
    // create the example expression
    final ExampleExpression expression = Hearthstone.getDb().getExpressionFactory().exampleLike(example, true, LikeType.EQUAL_TO);
    // find and return all bans that match the expression
    return Hearthstone.getDb().find(HomeRecord.class).where().add(expression).orderBy("created_at DESC").findList();
  }

  static public HomeRecord findFirst(final Player player) throws NoHomeFoundException {
    // create the example
    final HomeRecord example = new HomeRecord();
    example.setCreatedBy(player.getName());
    example.setWorldUUID(player.getLocation().getWorld().getUID());
    // create the example expression
    final ExampleExpression expression = Hearthstone.getDb().getExpressionFactory().exampleLike(example, true, LikeType.EQUAL_TO);
    // find and return all bans that match the expression
    try {
      return Hearthstone.getDb().find(HomeRecord.class).where().add(expression).orderBy("created_at DESC").findList().get(0);
    } catch (final IndexOutOfBoundsException e) {
      throw new NoHomeFoundException();
    }
  }

  static public HomeRecord findFirst(final String playerName, final UUID worldUUID) throws NoHomeFoundException {
    // create the example
    final HomeRecord example = new HomeRecord();
    example.setCreatedBy(playerName);
    example.setWorldUUID(worldUUID);
    // create the example expression
    final ExampleExpression expression = Hearthstone.getDb().getExpressionFactory().exampleLike(example, true, LikeType.EQUAL_TO);
    // find and return all bans that match the expression
    try {
      return Hearthstone.getDb().find(HomeRecord.class).where().add(expression).orderBy("created_at DESC").findList().get(0);
    } catch (final IndexOutOfBoundsException e) {
      throw new NoHomeFoundException();
    }
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public long getLastAccessed() {
    return lastAccessed;
  }

  public Location getLocation() {
    final World world = Hearthstone.getInstance().getServer().getWorld(worldUUID);
    return new Location(world, x, y, z, yaw, pitch);
  }

  public float getPitch() {
    return pitch;
  }

  public UUID getWorldUUID() {
    return worldUUID;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public float getYaw() {
    return yaw;
  }

  public double getZ() {
    return z;
  }

  public void setCreatedAt(final long createdAt) {
    this.createdAt = createdAt;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public void setLastAccessed(final long lastAccessed) {
    this.lastAccessed = lastAccessed;
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
