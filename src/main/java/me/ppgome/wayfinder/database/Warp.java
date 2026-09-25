package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Warp {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String permission;

    @DatabaseField(canBeNull = false)
    private double x;

    @DatabaseField(canBeNull = false)
    private double y;

    @DatabaseField(canBeNull = false)
    private double z;

    @DatabaseField(canBeNull = false)
    private String world;

    @DatabaseField(canBeNull = true)
    private String builder;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = false)
    private byte[] item;

    private ItemStack itemStack;

    // -----------------------------------------------------------------------------------------------------------------

    public Warp() {}

    public Warp(String name, String permission, Location location, String builder, ItemStack itemStack) {
        this.name = name;
        this.permission = permission;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
        this.builder = builder;
        this.item = itemStack.serializeAsBytes();
        this.itemStack = itemStack;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void loadItem() {
        this.itemStack = ItemStack.deserializeBytes(item);
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

    public void setLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.world = location.getWorld().getName();
    }

    public String getBuilder() {
        return builder;
    }

    public void setBuilder(String builder) {
        this.builder = builder;
    }

    public byte[] getItem() {
        return item;
    }

    public void setItem(byte[] item) {
        this.item = item;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
