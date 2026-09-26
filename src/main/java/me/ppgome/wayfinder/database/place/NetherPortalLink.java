package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.field.DatabaseField;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NetherPortalLink {

    @DatabaseField(id = true)
    private String placeName;

    @DatabaseField(foreign = true)
    private Place place;

    @DatabaseField
    private double partnerX;

    @DatabaseField
    private double partnerY;

    @DatabaseField
    private double partnerZ;

    @DatabaseField
    private String partnerWorld;

    private transient Location partnerLocation;

    //------------------------------------------------------------------------------------------------------------------

    public NetherPortalLink() {}

    public NetherPortalLink(String placeName) {
        this.placeName = placeName;
    }

    public NetherPortalLink(Place place, Location partnerLocation) {
        this.placeName = place.getName();
        this.place = place;
        setPartnerLocation(partnerLocation);
    }

    //------------------------------------------------------------------------------------------------------------------

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Location getPartnerLocation() {
        if (partnerLocation == null) {
            if (partnerWorld == null || partnerWorld.isEmpty()) {
                return null;
            }
            org.bukkit.World world = Bukkit.getWorld(partnerWorld);
            if (world == null) {
                return null;
            }
            partnerLocation = new Location(world, partnerX, partnerY, partnerZ);
        }
        return partnerLocation;
    }

    public void setPartnerLocation(Location location) {
        this.partnerWorld = location.getWorld().getName();
        this.partnerX = location.getX();
        this.partnerY = location.getY();
        this.partnerZ = location.getZ();
        this.partnerLocation = location.clone();
    }
}
