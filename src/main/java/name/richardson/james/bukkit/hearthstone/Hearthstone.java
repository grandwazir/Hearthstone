/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * Hearthstone.java is part of Hearthstone.
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
package name.richardson.james.bukkit.hearthstone;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;

import name.richardson.james.bukkit.hearthstone.general.HomeCommand;
import name.richardson.james.bukkit.hearthstone.general.SetCommand;
import name.richardson.james.bukkit.hearthstone.persistence.HearthstoneConfiguration;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;
import name.richardson.james.bukkit.hearthstone.teleport.ScheduledTeleport;
import name.richardson.james.bukkit.hearthstone.teleport.TeleportCommand;
import name.richardson.james.bukkit.utilities.command.CommandManager;
import name.richardson.james.bukkit.utilities.plugin.AbstractPlugin;
import name.richardson.james.bukkit.utilities.plugin.PluginPermissions;

@PluginPermissions(permissions = { "hearthstone" })
public class Hearthstone extends AbstractPlugin {

	/* Configuration for the plugin */
	private HearthstoneConfiguration configuration;

	/* Cooldown tracker for the plugin */
	private final Map<String, Long> cooldown = new HashMap<String, Long>();

	/* Reference to the WorldGuard plugin if loaded */
	private WorldGuardPlugin worldGuard;

	public String getArtifactID() {
		return "hearthstone";
	}

	public Map<String, Long> getCooldownTracker() {
		return this.cooldown;
	}

	@Override
	public List<Class<?>> getDatabaseClasses() {
		final List<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(HomeRecord.class);
		return classes;
	}

	public GlobalRegionManager getGlobalRegionManager() {
		if (this.worldGuard != null) {
			return this.worldGuard.getGlobalRegionManager();
		} else {
			return null;
		}
	}

	public String getVersion() {
		return this.getDescription().getVersion();
	}

	@Override
	public void onEnable() {
		try {
			this.loadConfiguration();
			this.loadDatabase();
			this.setPermissions();
			this.initaliseWorldGuard();
			this.registerCommands();
			this.setupMetrics();
			this.updatePlugin();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * name.richardson.james.bukkit.utilities.plugin.SkeletonPlugin#loadConfiguration
	 * ()
	 */
	@Override
	protected void loadConfiguration() throws IOException {
		super.loadConfiguration();
		final File file = new File(this.getDataFolder().getAbsolutePath() + File.separatorChar + "config.yml");
		final InputStream defaults = this.getResource("config.yml");
		this.configuration = new HearthstoneConfiguration(file, defaults);
	}

	protected void registerCommands() {
		Home.setGlobalRegionManager(this.getGlobalRegionManager());
		ScheduledTeleport.setCooldownTime(this.configuration.getCooldown());
		ScheduledTeleport.setWarmupTime(this.configuration.getWarmUp());
		final CommandManager commandManager = new CommandManager("hs");
		final SetCommand setCommand = new SetCommand(this);
		commandManager.addCommand(setCommand);
		final TeleportCommand teleportCommand = new TeleportCommand(this, this.configuration.getWarmUp(), this.configuration.getCooldown());
		commandManager.addCommand(teleportCommand);
		this.getCommand("home").setExecutor(new HomeCommand(this, teleportCommand, setCommand));
	}

	private void initaliseWorldGuard() {
		this.worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
	}

}
