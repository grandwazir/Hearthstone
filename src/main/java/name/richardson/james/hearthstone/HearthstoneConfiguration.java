package name.richardson.james.hearthstone;

import java.io.IOException;

import name.richardson.james.bukkit.utilities.configuration.AbstractConfiguration;
import name.richardson.james.bukkit.utilities.formatters.TimeFormatter;

public class HearthstoneConfiguration extends AbstractConfiguration {

  public static final String FILE_NAME = "config.yml";
  
  public HearthstoneConfiguration(Hearthstone plugin) throws IOException {
    super(plugin, FILE_NAME);
  }
  
  public boolean isDebugging() {
    return configuration.getBoolean("debugging");
  }
  
  public long getCooldown() {
    return TimeFormatter.parseTime(configuration.getString("cooldown"));
  }
  
}
