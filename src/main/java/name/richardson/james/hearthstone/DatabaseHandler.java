package name.richardson.james.hearthstone;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.EbeanServer;

import name.richardson.james.bukkit.utilities.database.Database;

public class DatabaseHandler extends Database {

  public static List<Class<?>> getDatabaseClasses() {
    final List<Class<?>> list = new ArrayList<Class<?>>();
    list.add(HomeRecord.class);
    return list;
  }

  public DatabaseHandler(final EbeanServer database) {
    super(database);
  }

  public List<HomeRecord> findHomeRecordsByOwner(final String playerName) {
    this.logger.debug(String.format("Attempting to return HomeRecords created by %s.", playerName));
    return this.getEbeanServer().find(HomeRecord.class).where().ieq("createdBy", playerName).findList();
  }

}
