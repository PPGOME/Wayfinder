package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class Waypoint {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private UUID ownerUuid;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private double x;

    @DatabaseField(canBeNull = false)
    private double y;

    @DatabaseField(canBeNull = false)
    private double z;

    @DatabaseField(canBeNull = false)
    private String world;

    // -----------------------------------------------------------------------------------------------------------------

    public Waypoint() {}

    public Waypoint(UUID ownerUuid, String name, Location location) {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
    }

    // -----------------------------------------------------------------------------------------------------------------


    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

}
