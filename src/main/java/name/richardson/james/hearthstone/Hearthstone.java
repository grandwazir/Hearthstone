package name.richardson.james.hearthstone;

import java.io.IOException;

import name.richardson.james.bukkit.util.Logger;
import name.richardson.james.bukkit.util.plugin.SkeletonPlugin;

public class Hearthstone extends SkeletonPlugin {

  public void onEnable() {
    try {
      Logger.setDebugging(this, true);
      setLoggerPrefix();
      setRootPermission();
      setResourceBundle();
    } catch (IOException e) {
      logger.severe("Unable to close file stream!");
    }
    logger.info(String.format(getMessage("plugin-enabled"), this.getDescription().getName()));
  }
  
}
