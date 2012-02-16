package name.richardson.james.hearthstone;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
  
  public List<HomeRecord> findHomeRecordsByOwnerAndWorld(final String playerName, final UUID uuid) {
    this.logger.debug(String.format("Attempting to return HomeRecords created by %s.", playerName));
    return this.getEbeanServer().find(HomeRecord.class).where().ieq("createdBy", playerName).eq("worldUUID", uuid).findList();
  }

  // this is to get around a bug where optimistic lock errors will occur if you attempt to delete the records normally
  public int deleteHomes(String plyaerName, final UUID uuid) {
    return this.database.createSqlUpdate("DELETE from hearthstone_homes WHERE world_uuid='" + uuid.toString() + "' AND created_by='" + plyaerName + "'").execute();
  }
  
}
