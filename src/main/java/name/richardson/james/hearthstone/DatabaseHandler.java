/*******************************************************************************
 * Copyright (c) 2012 James Richardson.
 * 
 * DatabaseHandler.java is part of Hearthstone.
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

  // this is to get around a bug where optimistic lock errors will occur if you
  // attempt to delete the records normally
  public int deleteHomes(String plyaerName, final UUID uuid) {
    return this.database.createSqlUpdate("DELETE from hearthstone_homes WHERE world_uuid='" + uuid.toString() + "' AND created_by='" + plyaerName + "'").execute();
  }

  public List<HomeRecord> findHomeRecordsByOwner(final String playerName) {
    this.logger.debug(String.format("Attempting to return HomeRecords created by %s.", playerName));
    return this.getEbeanServer().find(HomeRecord.class).where().ieq("createdBy", playerName).findList();
  }

  public List<HomeRecord> findHomeRecordsByOwnerAndWorld(final String playerName, final UUID uuid) {
    this.logger.debug(String.format("Attempting to return HomeRecords created by %s.", playerName));
    return this.getEbeanServer().find(HomeRecord.class).where().ieq("createdBy", playerName).eq("worldUUID", uuid).findList();
  }

}
