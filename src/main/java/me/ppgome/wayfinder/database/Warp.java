package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Warp {

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField
    private String permission;

    @DatabaseField(canBeNull = false)
    private double x;

    @DatabaseField(canBeNull = false)
    private double y;

    @DatabaseField(canBeNull = false)
    private double z;

    @DatabaseField(canBeNull = false)
    private String world;

    // -----------------------------------------------------------------------------------------------------------------

    public Warp() {}

    public Warp(String name, String permission, Location location) {
        this.name = name;
        this.permission = permission;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
    }

    // -----------------------------------------------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

}
