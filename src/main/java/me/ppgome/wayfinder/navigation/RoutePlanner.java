package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.NetherPortalLink;
import me.ppgome.wayfinder.database.place.Place;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
        boolean sameWorld = from.getWorld().equals(to.getWorld());

        Route direct = sameWorld
                ? new Route(List.of(toPlace), from.distance(to), Type.DIRECT)
                : null;

        Route bestPortal = config.ENABLE_NETHER_PORTALS ? planNether(from, to) : null;

        Route viaEnd = planEnd(from, to);
        if (viaEnd != null && (bestPortal == null
                || viaEnd.estimatedDistance() < bestPortal.estimatedDistance())) {
            bestPortal = viaEnd;
        }

        if (bestPortal == null) return direct;
        if (direct == null) return bestPortal;
        return bestPortal.estimatedDistance() < direct.estimatedDistance() * 0.9 ? bestPortal : direct;
    }

    private Route planNether(Location from, Location to) {
        Route best = null;
        List<Place> portals = cache.getAllNetherPortalPlaces().stream()
                .filter(p -> {
                    if (p == null || p.getLocation() == null || p.getLocation().getWorld() == null) return false;
                    NetherPortalLink link = p.getNetherPortalLink();
                    if (link == null) return false;
                    Location partner = link.getPartnerLocation();
                    return partner != null
                            && partner.getWorld() != null
                            && partner.getWorld().getEnvironment() == World.Environment.NETHER;
                })
                .toList();

        for (Place a : portals) {
            NetherPortalLink linkA = a.getNetherPortalLink();
            if (linkA == null || !a.getLocation().getWorld().equals(from.getWorld())) continue;

            double leg1 = from.distance(a.getLocation());
            Location netherA = linkA.getPartnerLocation();

            for (Place b : portals) {
                if (b.equals(a)) continue;
                NetherPortalLink linkB = b.getNetherPortalLink();
                if (linkB == null || !b.getLocation().getWorld().equals(to.getWorld())) continue;

                double leg2 = netherA.distance(linkB.getPartnerLocation()) / 8.0;
                double leg3 = b.getLocation().distance(to);
                double total = leg1 + leg2 + leg3;

                if (best == null || total < best.estimatedDistance()) {
                    best = new Route(List.of(a, b, toPlace), total, Type.NETHER);
                }
            }
        }
        return best;
    }

    private Route planEnd(Location from, Location to) {
        if (findEndWorld() == null) return null;

        Location returnPoint = getEndReturnPoint(player, to);
        double leg3 = returnPoint.distance(to);

        Route best = null;
        for (Place e : cache.getAllEndPortalPlaces()) {
            if (!e.getLocation().getWorld().equals(from.getWorld())) continue;

            double total = from.distance(e.getLocation()) + END_SIDE_DISTANCE + leg3;
            if (best == null || total < best.estimatedDistance()) {
                best = new Route(List.of(e, toPlace), total, Type.END);
            }
        }
        return best;
    }

    // -----------------------------------------------------------------------------------------------------------------

    static final double END_SIDE_DISTANCE = 100.0;

    static World findEndWorld() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.THE_END) return w;
        }
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /** Where the player ends up after leaving the End: their bed, or 0,0 if none. */
    static Location getEndReturnPoint(Player player, Location to) {
        Location bed = player.getRespawnLocation();
        if (bed != null && bed.getWorld().equals(to.getWorld())) return bed;
        return new Location(to.getWorld(), 0, to.getY(), 0);
    }

    public enum Type { DIRECT, NETHER, END }

    public record Route(List<Place> places, double estimatedDistance, Type type) {}

}
