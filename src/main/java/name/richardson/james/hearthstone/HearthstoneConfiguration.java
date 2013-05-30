/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * HearthstoneConfiguration.java is part of Hearthstone.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import name.richardson.james.bukkit.utilities.configuration.SimplePluginConfiguration;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;

public class HearthstoneConfiguration extends SimplePluginConfiguration {

	public HearthstoneConfiguration(final File file, final InputStream defaults) throws IOException {
		super(file, defaults);
	}

	public long getCooldown() {
		return TimeFormatter.parseTime(this.getConfiguration().getString("cooldown"));
	}

	public long getWarmUp() {
		return TimeFormatter.parseTime(this.getConfiguration().getString("warmup"));
	}

}
