package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Home {

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

    @DatabaseField(canBeNull = false)
    private float pitch;

    @DatabaseField(canBeNull = false)
    private float yaw;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = false)
    private byte[] item;

    private ItemStack itemStack;

    // -----------------------------------------------------------------------------------------------------------------

    public Home() {}

    public Home(UUID ownerUuid, String name, ItemStack itemStack, Location location) {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.item = itemStack.serializeAsBytes();
        this.itemStack = itemStack;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    //------------------------------------------------------------------------------------------------------------------

    public void loadItem() {
        this.itemStack = ItemStack.deserializeBytes(item);
    }

    //------------------------------------------------------------------------------------------------------------------


    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
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
