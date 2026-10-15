package me.ppgome.wayfinder.utils;

import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import org.bukkit.Location;
import org.bukkit.World;

public class PlaceUtils {

    public static boolean sameWorld(Location a, Location b) {
        return a.getWorld() != null && a.getWorld().equals(b.getWorld());
    }

    public static Place findClosestPlace(Location playerLocation, Category category) {
        Place closestPlace = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Place place : category.getPlaces()) {
            Location placeLocation = place.getLocation();
            if (PlaceUtils.sameWorld(playerLocation, placeLocation)) {
                double distSq = playerLocation.distanceSquared(placeLocation);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestPlace = place;
                }
            }
        }

        if (category.getName().equals("Nether Portals") && playerLocation.getWorld().getEnvironment() == World.Environment.NETHER) {
            for (Place place : category.getPlaces()) {
                if (place.getNetherPortalLink() != null && place.getNetherPortalLink().getPartnerLocation() != null) {
                    Location partnerLoc = place.getNetherPortalLink().getPartnerLocation();
                    if (PlaceUtils.sameWorld(playerLocation, partnerLoc)) {
                        double distSq = playerLocation.distanceSquared(partnerLoc);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closestPlace = place;
                        }
                    }
                }
            }
        }

        return closestPlace;
    }

}
