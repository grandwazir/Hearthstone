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
import java.util.*;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalRegionManager;

import name.richardson.james.bukkit.utilities.command.Command;
import name.richardson.james.bukkit.utilities.command.HelpCommand;
import name.richardson.james.bukkit.utilities.command.invoker.CommandInvoker;
import name.richardson.james.bukkit.utilities.command.invoker.FallthroughCommandInvoker;
import name.richardson.james.bukkit.utilities.logging.PluginLoggerFactory;
import name.richardson.james.bukkit.utilities.persistence.database.DatabaseLoader;
import name.richardson.james.bukkit.utilities.persistence.database.DatabaseLoaderFactory;
import name.richardson.james.bukkit.utilities.persistence.database.SimpleDatabaseConfiguration;
import name.richardson.james.bukkit.utilities.updater.BukkitDevPluginUpdater;
import name.richardson.james.bukkit.utilities.updater.PluginUpdater;

import name.richardson.james.bukkit.hearthstone.general.SetCommand;
import name.richardson.james.bukkit.hearthstone.persistence.HearthstoneConfiguration;
import name.richardson.james.bukkit.hearthstone.persistence.HomeRecord;
import name.richardson.james.bukkit.hearthstone.teleport.TeleportCommand;

public class Hearthstone extends JavaPlugin {

	private static final String DATABASE_CONFIG_NAME = "database.yml";
	private static final int PROJECT_ID = 31246;

	/* Configuration for the plugin */
	private HearthstoneConfiguration configuration;

	/* Cooldown tracker for the plugin */
	private final Map<String, Long> cooldown = new HashMap<String, Long>();
	private EbeanServer database;
	private Logger logger = PluginLoggerFactory.getLogger(Hearthstone.class);

	/* Reference to the WorldGuard plugin if loaded */
	private WorldGuardPlugin worldGuard;

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
			this.initaliseWorldGuard();
			this.registerCommands();
			//TODO this.setupMetrics();
			//TODO this.updatePlugin();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void loadDatabase()
	throws IOException {
		ServerConfig serverConfig = new ServerConfig();
		getServer().configureDbConfig(serverConfig);
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(HomeRecord.class);
		serverConfig.setClasses(classes);
		final File file = new File(this.getDataFolder().getPath() + File.separatorChar + DATABASE_CONFIG_NAME);
		final InputStream defaults = this.getResource(DATABASE_CONFIG_NAME);
		final SimpleDatabaseConfiguration configuration = new SimpleDatabaseConfiguration(file, defaults, this.getName(), serverConfig);
		final DatabaseLoader loader = DatabaseLoaderFactory.getDatabaseLoader(configuration);
		loader.initalise();
		this.database = loader.getEbeanServer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * name.richardson.james.bukkit.utilities.plugin.SkeletonPlugin#loadConfiguration
	 * ()
	 */
	protected void loadConfiguration() throws IOException {
		final File file = new File(this.getDataFolder().getAbsolutePath() + File.separatorChar + "config.yml");
		final InputStream defaults = this.getResource("config.yml");
		this.configuration = new HearthstoneConfiguration(file, defaults);
		this.logger.setLevel(configuration.getLogLevel());
	}

	protected void registerCommands() {
		Set<Command> commandSet = new HashSet<Command>();
		// create the commands
		commandSet.add(new SetCommand(this.getServer(), this.getDatabase()));
		commandSet.add(new TeleportCommand(this.getServer(), this.getDatabase(), this.configuration.getWarmUp(), this.configuration.getCooldown()));
		// create the invokers
		HelpCommand helpCommand = new HelpCommand("home", commandSet);
		CommandInvoker invoker = new FallthroughCommandInvoker(helpCommand);
		invoker.addCommands(commandSet);
		this.getCommand("home").setExecutor(invoker);
	}

	private void updatePlugin() {
		if (!configuration.getAutomaticUpdaterState().equals(PluginUpdater.State.OFF)) {
			PluginUpdater updater = new BukkitDevPluginUpdater(this.getDescription(), configuration.getAutomaticUpdaterBranch(), configuration.getAutomaticUpdaterState(), PROJECT_ID, this.getServer().getUpdateFolderFile(), this.getServer().getVersion());
			this.getServer().getScheduler().runTaskAsynchronously(this, updater);
			new name.richardson.james.bukkit.utilities.updater.PlayerNotifier(this, this.getServer().getPluginManager(), updater);
		}
	}

	public EbeanServer getDatabase() {
		return this.database;
	}

	private void initaliseWorldGuard() {
		this.worldGuard = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
	}

}
