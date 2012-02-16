package name.richardson.james.hearthstone.general;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import name.richardson.james.bukkit.utilities.command.CommandArgumentException;
import name.richardson.james.bukkit.utilities.command.CommandPermissionException;
import name.richardson.james.bukkit.utilities.command.CommandUsageException;
import name.richardson.james.bukkit.utilities.command.PluginCommand;
import name.richardson.james.hearthstone.DatabaseHandler;
import name.richardson.james.hearthstone.Hearthstone;
import name.richardson.james.hearthstone.HomeRecord;

public class SetCommand extends PluginCommand {

  private final Server server;
  private final DatabaseHandler database;

  public SetCommand(Hearthstone plugin) {
    super(plugin, plugin.getMessage("setcommand-name"), plugin.getMessage("setcommand-description"), plugin.getMessage("setcommand-usage"));
    this.server = plugin.getServer();
    this.database = plugin.getDatabaseHandler();
    // register permissions
    this.registerPermissions();
  }
  
  private void registerPermissions() {
    final String prefix = plugin.getDescription().getName().toLowerCase() + ".";
    final String wildcardDescription = String.format(plugin.getMessage("wildcard-permission-description"), this.getName());
    // create the wildcard permission
    Permission wildcard = new Permission(prefix + this.getName() + ".*", wildcardDescription, PermissionDefault.OP);
    wildcard.addParent(plugin.getRootPermission(), true);
    this.addPermission(wildcard);
    // create the base permission
    Permission base = new Permission(prefix + this.getName(), plugin.getMessage("setcommand-permission-description"), PermissionDefault.TRUE);
    base.addParent(wildcard, true);
    this.addPermission(base);
    // add ability to set other user's homes
    Permission others = new Permission(prefix + this.getName() + "." + plugin.getMessage("setcommand-others-permission-name"), plugin.getMessage("setcommand-others-permission-description"), PermissionDefault.OP);
    others.addParent(wildcard, true);
    this.addPermission(others);
  }

  public void parseArguments(List<String> arguments, CommandSender sender) throws CommandArgumentException {
    final Map<String, Object> map = new HashMap<String, Object>();
    
    // if the arguments are empty do nothing and assume the user is setting their own home.
    if (!arguments.isEmpty()) {
      // otherwise assume the player wants to set the home of another player
      final String playerName = matchPlayerName(arguments.remove(0));
      map.put("player", playerName);
    }
    
    // set the arguments
    this.setArguments(map);
  }

  private String matchPlayerName(String playerName) {
    List<Player> matches = this.server.matchPlayer(playerName);
    if (matches.isEmpty()) {
      return playerName;
    } else {
      return matches.get(0).getName();
    }
  }

  public void execute(CommandSender sender) throws CommandArgumentException, CommandUsageException, CommandPermissionException {
    // do not allow ConsoleCommandSenders to use this command
    if (sender instanceof ConsoleCommandSender) throw new CommandUsageException(this.plugin.getMessage("command-is-player-only"));
    
    // if the playerName is provided use that otherwise get it from the sender's name
    final Player player = (Player) sender;
    final String playerName = (String) ((this.getArguments().containsKey("player")) ? this.getArguments().get("player") : player.getName());
    final Location location = player.getLocation();
    final UUID worldUUID = location.getWorld().getUID();
    
    // check if we are allowed to set the home for..
    if (player.getName().equalsIgnoreCase(playerName)) {
      // ourselves
      if (!player.hasPermission(this.getPermission(1))) throw new CommandPermissionException(plugin.getMessage("command-not-permitted"), this.getPermission(1));
    } else {
      // others
      if (!player.hasPermission(this.getPermission(2))) throw new CommandPermissionException(plugin.getMessage("command-not-permitted"), this.getPermission(2));
    }
    
    // check to see if the location is blocked
    if (isLocationObstructed(location)) throw new CommandUsageException(this.plugin.getMessage("setcommand-location-is-obstructed"));
    
    // check to see if the player can build in that location
    if (!isPlayerAllowedToBuild(location)) throw new CommandUsageException(this.plugin.getMessage("setcommand-location-is-not-buildable"));
    
    database.deleteHomes(playerName, worldUUID);
    
    // create a new home
    this.createHome(playerName, location);
    sender.sendMessage(ChatColor.GREEN + this.plugin.getMessage("setcommand-home-set"));
    
  }
  
  public void createHome(String playerName, Location location) {
    final HomeRecord record = new HomeRecord();
    record.setCreatedAt(System.currentTimeMillis());
    record.setCreatedBy(playerName);
    record.setX(location.getX());
    record.setY(location.getY());
    record.setZ(location.getZ());
    record.setYaw(location.getYaw());
    record.setPitch(location.getPitch());
    record.setWorldUUID(location.getWorld().getUID());
    database.save(record);
  }
  
  private boolean isPlayerAllowedToBuild(Location location) {
    // TODO Auto-generated method stub
    return true;
  }

  private boolean isLocationObstructed(Location location) {
    return false;
  }

}
