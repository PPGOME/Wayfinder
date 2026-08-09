package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Place {

    @DatabaseField(canBeNull = false, id = true)
    private String name;

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

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] item;

    private ItemStack itemStack;

    @DatabaseField
    private boolean pinned;

    @DatabaseField
    private long expires;

    private Set<Category> categories;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Empty constructor as required by ORMLite.
     */
    public Place() {
        this.categories = new HashSet<>();
    }

    public Place(String name, Location location, String builder, ItemStack itemStack, boolean pinned,
                 Map<String, Category> categories) {
        this.name = name;
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.world = location.getWorld().getName();
        this.builder = builder;
        this.item = itemStack.serializeAsBytes();
        this.itemStack = itemStack;
        this.pinned = pinned;
        setCategories(categories);
    }

    //------------------------------------------------------------------------------------------------------------------

    public void loadItem() {
        this.itemStack = ItemStack.deserializeBytes(item);
    }

    //------------------------------------------------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
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

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Category> categories) {
        this.categories = new HashSet<>();
        this.categories.addAll(categories.values());
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }
}
