package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class Category {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField
    private String bluemapIcon;

    @DatabaseField(canBeNull = false)
    private int priority;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] item;

    ItemStack itemStack;

    private Set<Place> places;

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Empty constructor as required by ORMLite.
     */
    public Category() {
        places = new HashSet<>();
    }

    public Category(String name, int priority, ItemStack itemStack) {
        this();
        this.name = name;
        this.priority = priority;
        this.item = itemStack.serializeAsBytes();
        this.itemStack = itemStack;
    }

    public Category(String name, int priority, String bluemapIcon, ItemStack itemStack) {
        this();
        this.name = name;
        this.priority = priority;
        this.bluemapIcon = bluemapIcon;
        this.item = itemStack.serializeAsBytes();
        this.itemStack = itemStack;
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

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {}

    public String getBluemapIcon() {
        return bluemapIcon;
    }

    public void setBluemapIcon(String bluemapIcon) {
        this.bluemapIcon = bluemapIcon;
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

    public Set<Place> getPlaces() {
        return places;
    }

    public void addPlace(Place place) {
        this.places.add(place);
    }

    public void removePlace(Place place) {
        this.places.remove(place);
    }
}
