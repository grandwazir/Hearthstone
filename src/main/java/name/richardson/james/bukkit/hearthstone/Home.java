package name.richardson.james.bukkit.hearthstone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.GlobalRegionManager;

public class Home {

	private static GlobalRegionManager regionManager;

	public static void setGlobalRegionManager(final GlobalRegionManager regionManager) {
		Home.regionManager = regionManager;
	}

	private final Location location;

	public Home(final Location location) {
		this.location = location;
	}

	public Home(final String worldName, final int x, final int y, final int z, final int yaw, final int pitch) {
		final World world = Bukkit.getServer().getWorld(worldName);
		this.location = new Location(world, x, y, z, yaw, pitch);
	}

	public Location getLocation() {
		return this.location;
	}

	public boolean isBuildable(final Player player) {
		return Home.regionManager.canBuild(player, this.location);
	}

	public boolean isObstructed() {
		// check the block that the player's legs occupy.
		if (!this.location.getBlock().isEmpty()) { return true; }
		// check the block that the player's head occupies.
		if (!this.location.add(0, 1, 0).getBlock().isEmpty()) { return true; }
		return false;
	}

}
