package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.NetherPortalLink;
import me.ppgome.wayfinder.database.place.Place;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class RoutePlanner {

    private final Wayfinder plugin;
    private final Cache cache;
    private final WConfig config;

    Player player;
    private final Place toPlace;

    public RoutePlanner(Wayfinder plugin, Player player, Place place) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.player = player;
        this.toPlace = place;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public Route plan() {
        Location from = player.getLocation();
        Location to = toPlace.getLocation();

        return switch(to.getWorld().getEnvironment()) {
            case NORMAL -> planOverworld(from, to);
            case NETHER -> planNether(from, to);
            case THE_END -> planEnd(from, to);
            default -> planOverworld(from, to);
        };
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Route planOverworld(Location from, Location to) {
        Route best = null;
        if (from.getWorld().equals(to.getWorld())) {
            best = new Route(List.of(toPlace), from.distance(to), false);
        }
        if (!config.ENABLE_NETHER_PORTALS) return best;

        Set<Place> portals = cache.getAllNetherPortalPlaces();

        for (Place a : portals) {
            NetherPortalLink linkA = a.getNetherPortalLink();
            if (linkA == null || !a.getLocation().getWorld().equals(from.getWorld())) continue;

            double leg1 = from.distance(a.getLocation());
            Location netherA = linkA.getPartnerLocation();

            for (Place b : portals) {
                if (b == a) continue;
                NetherPortalLink linkB = b.getNetherPortalLink();
                if (linkB == null || !b.getLocation().getWorld().equals(to.getWorld())) continue;

                double leg2 = netherA.distance(linkB.getPartnerLocation());
                double leg3 = b.getLocation().distance(to);
                double total = leg1 + leg2 + leg3;

                if (best == null || total < best.estimatedDistance() * 0.9) {
                    best = new Route(List.of(a, b, toPlace), total, true);
                }
            }
        }
        return best;
    }

    private Route planNether(Location from, Location to) {
        return null;
    }

    private Route planEnd(Location from, Location to) {
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public record Route(List<Place> places, double estimatedDistance, boolean usesPortal) {}

}
