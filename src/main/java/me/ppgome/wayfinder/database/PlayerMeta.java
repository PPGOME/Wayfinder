package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;

/**
 * Player metadata, including homes, waypoints, and teleport relationships.
 */
public class PlayerMeta {

    @DatabaseField(canBeNull = false, id = true)
    private UUID uuid;

    @DatabaseField(canBeNull = false)
    private String name;

    // Last location (used for offline /tphere)
    @DatabaseField
    private double x;

    @DatabaseField
    private double y;

    @DatabaseField
    private double z;

    @DatabaseField
    private String world;

    @DatabaseField
    private float pitch;

    @DatabaseField
    private float yaw;

    @DatabaseField
    private boolean wasMoved;

    private HashMap<String, Home> homes;
    private HashMap<String, Waypoint> waypoints;

    // Teleport relationships
    private Set<UUID> friends;
    private Set<UUID> blocked;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Constructor for ORM.
     */
    public PlayerMeta() {
        this.homes = new HashMap<>();
        this.waypoints = new HashMap<>();
    }

    /**
     * Constructor to create a new PlayerMeta.
     * @param uuid UUID of the player.
     * @param name Name of the player.
     */
    public PlayerMeta(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.homes = new HashMap<>();
        this.waypoints = new HashMap<>();
    }

    // Generic ---------------------------------------------------------------------------------------------------------

    /**
     * Get the player's UUID.
     * @return UUID of the player.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the player's name.
     * @return Name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the player's last known location. Updated when the player leaves the server.
     * @return Last known Location of the player.
     */
    public Location getLastLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
    }

    /**
     * Sets the player's last known location. Updated when the player leaves the server.
     * @param location Last known Location of the player.
     */
    public void setLastLocation(Location location) {
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    /**
     * Checks if a player was moved with <code>/tphere</code> while they were offline.
     * @return True if they were, false if not.
     */
    public boolean wasMoved() {
        return wasMoved;
    }

    /**
     * Sets the status of a player being moved with <code>/tphere</code> while they were offline.
     * @param status True if the player was moved, false if not.
     */
    public void setMoved(boolean status) {
        this.wasMoved = status;
    }

    // Homes -----------------------------------------------------------------------------------------------------------

    /**
     * Add a home to the player's homes.
     * @param home The home to add.
     */
    public void addHome(Home home) {
        homes.put(home.getName(), home);
    }

    /**
     * Get a home by name.
     * @param name The name of the home.
     * @return Home or null if not found.
     */
    public Home getHome(String name) {
        return homes.get(name);
    }

    /**
     * Get all of a player's homes.
     * @return A map of all of a player's homes.
     */
    public Map<String, Home> getAllHomes() {
        return homes;
    }

    /**
     * Remove a home by name.
     * @param name The name of the home to remove.
     */
    public void removeHome(String name) {
        homes.remove(name);
    }

    // Waypoints -------------------------------------------------------------------------------------------------------

    /**
     * Add a waypoint to the player's waypoints.
     * @param waypoint The waypoint to add.
     */
    public void addWaypoint(Waypoint waypoint) {
        waypoints.put(waypoint.getName(), waypoint);
    }

    /**
     * Get a waypoint by name.
     * @param name The name of the waypoint.
     * @return Waypoint or null if not found.
     */
    public Waypoint getWaypoint(String name) {
        return waypoints.get(name);
    }

    /**
     * Remove a waypoint by name.
     * @param name The name of the waypoint to remove.
     */
    public void removeWaypoint(String name) {
        waypoints.remove(name);
    }

    // Friends ---------------------------------------------------------------------------------------------------------

    /**
     * Add a friend by UUID.
     * @param friendUuid UUID of the friend to add.
     */
    public void addFriend(UUID friendUuid) {
        friends.add(friendUuid);
    }

    /**
     * Check if a UUID is a friend.
     * @param friendUuid UUID to check.
     * @return True if friend, false otherwise.
     */
    public boolean isFriend(UUID friendUuid) {
        return friends.contains(friendUuid);
    }

    /**
     * Remove a friend by UUID.
     * @param friendUuid UUID of the friend to remove.
     */
    public void removeFriend(UUID friendUuid) {
        friends.remove(friendUuid);
    }

    // Blocked ---------------------------------------------------------------------------------------------------------

    /**
     * Add a blocked player by UUID.
     * @param blockedUuid UUID of the blocked player to add.
     */
    public void addBlocked(UUID blockedUuid) {
        friends.add(blockedUuid);
    }

    /**
     * Check if a UUID is blocked.
     * @param blockedUuid UUID to check.
     * @return True if blocked, false otherwise.
     */
    public boolean isBlocked(UUID blockedUuid) {
        return friends.contains(blockedUuid);
    }

    /**
     * Remove a blocked player by UUID.
     * @param blockedUuid UUID of the blocked player to remove.
     */
    public void removeBlocked(UUID blockedUuid) {
        friends.remove(blockedUuid);
    }

}
