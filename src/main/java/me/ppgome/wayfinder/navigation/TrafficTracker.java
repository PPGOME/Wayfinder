package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Wayfinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrafficTracker {

    Wayfinder plugin;

    Map<UUID, Location> lastLoggedLoc = new HashMap<>();
    Map<GridCell, Long> cellCooldowns = new ConcurrentHashMap<>();
    Map<GridCell, Integer> cellVisitCounts = new ConcurrentHashMap<>();

    private static final int GRID_SIZE = 4;
    private static final int TRAFFIC_THRESHOLD = 3;

    public void test() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                Location currentLocation = player.getLocation();
                Location lastLocation = lastLoggedLoc.get(player.getUniqueId());

                if(lastLocation == null || (lastLocation.distanceSquared(currentLocation) > 144.0 && isLinearRoadMovement(lastLocation, currentLocation))) {
                    lastLoggedLoc.put(player.getUniqueId(), currentLocation.clone());
                    recordStep(currentLocation);
                }
            }
        }, 20, 20);
    }

    private void recordStep(Location location) {
        GridCell cell = new GridCell(location.getWorld().getName(), location.getBlockX() / GRID_SIZE, location.getBlockZ() / GRID_SIZE);
    }

    private boolean isLinearRoadMovement(Location previous, Location current) {
        double distance = previous.distance(current);
        if (distance < 8.0) {
            return false;
        }
        Vector currentDir = current.toVector().subtract(previous.toVector()).normalize();
        return true;
    }



    public record GridCell(String world, int x, int z) {}

}
