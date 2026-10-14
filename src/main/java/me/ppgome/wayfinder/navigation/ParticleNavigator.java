package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.Waypoint;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.navigation.RoutePlanner.Route;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.text.TextUtils;
import me.ppgome.wayfinder.navigation.RoutePlanner.RouteWaypoint;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ParticleNavigator {

    private final Wayfinder plugin;
    private final Cache cache;
    private final WConfig config;
    private final Player player;
    private final Place destination;

    private BossBar infoBar;
    private BukkitTask repeatingTask;

    private List<RouteWaypoint> waypoints;
    private double totalDistance;
    private int step = 0;

    public ParticleNavigator(Wayfinder plugin, Player player, Place destination) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.player = player;
        this.destination = destination;
    }

    //------------------------------------------------------------------------------------------------------------------

    public void startDisplaying() {
        Route route = new RoutePlanner(plugin, player, destination).plan();
        if (route == null || route.routeWaypoints().isEmpty()) {
            // TODO: add a config message, e.g. config.PLACE_NO_ROUTE
            player.sendMessage(Component.text("No route could be found to that place.", NamedTextColor.RED));
            return;
        }

        this.waypoints = route.routeWaypoints();
        this.totalDistance = Math.max(route.estimatedDistance(), 1.0);

        cache.addRouting(player.getUniqueId(), this);

        infoBar = BossBar.bossBar(
                updateBossBar(totalDistance),
                0.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS);
        player.showBossBar(infoBar);

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 255), 0.75f);

        this.repeatingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stop();
                    return;
                }

                Location playerLocation = player.getLocation();
                int last = waypoints.size() - 1;

                while (step < last && !sameWorld(playerLocation, waypoints.get(step).location())) {
                    step++;
                }

                Location target = waypoints.get(step).location();
                if (!sameWorld(playerLocation, target)) return;

                double distToTarget = playerLocation.distance(target);

                if (step == last && distToTarget < 5) {
                    player.sendMessage(PlaceholderParser.of(config.PLACE_REACHED_DESTINATION)
                            .place(destination.getName()).parse());
                    stop();
                    return;
                }

                updateProgress(distToTarget);
                infoBar.name(updateBossBar(distToTarget));

                if (distToTarget < 1) return;
                Vector direction = target.toVector().subtract(playerLocation.toVector());
                direction.setY(0);
                if (direction.lengthSquared() < 1) return;
                direction.normalize();

                for (double i = 1; i < 10; i += 0.5) {
                    Location point = playerLocation.clone().add(direction.clone().multiply(i));
                    point.setY(groundY(point, playerLocation.getBlockY()) + 0.3);
                    player.spawnParticle(Particle.DUST, point, 1, dustOptions);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void updateProgress(double distToTarget) {
        double remaining = distToTarget;

        float progress = (float) (1.0 - remaining / totalDistance);
        infoBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
    }

    private Component updateBossBar(double distToTarget) {
        return Component.text("Routing to: ", NamedTextColor.YELLOW)
                .append(Component.text(destination.getName(), NamedTextColor.GOLD))
                .append(Component.text(" | ", NamedTextColor.BLUE))
                .append(TextUtils.parseMiniMessage("<red>Press <key:key.swapOffhand> to cancel</red>"))
                .append(Component.text(" | ", NamedTextColor.BLUE))
                .append(Component.text("Distance: ", NamedTextColor.YELLOW))
                .append(Component.text(String.valueOf(distToTarget), NamedTextColor.GOLD));
    }

    private boolean sameWorld(Location a, Location b) {
        return a.getWorld() != null && a.getWorld().equals(b.getWorld());
    }

    private void stop() {
        cancelTask();
        cache.removeRouting(player.getUniqueId());
    }

    private double groundY(Location point, int refY) {
        World world = point.getWorld();
        int x = point.getBlockX();
        int z = point.getBlockZ();

        for (int y = refY + 3; y >= refY - 6; y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);
            if (!block.isPassable() && above.isPassable()) {
                return y + 1;
            }
        }
        return refY;
    }

    public void cancelTask() {
        if (infoBar != null) {
            player.hideBossBar(infoBar);
            infoBar = null;
        }
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
    }
}