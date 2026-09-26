package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Category;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.text.PlaceholderParser;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ParticleNavigator {

    private Wayfinder plugin;
    private Cache cache;
    private WConfig config;
    private Player player;
    private Location endLocation;
    private String locationName;
    private BukkitScheduler scheduler;
    private BossBar infoBar;
    private double travelled;
    private BukkitTask repeatingTask;

    public ParticleNavigator(Wayfinder plugin, Player player, Location endLocation, String locationName) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.player = player;
        this.endLocation = endLocation;
        this.locationName = locationName;
    }

    //------------------------------------------------------------------------------------------------------------------

    // TODO: See https://www.spigotmc.org/threads/can-i-make-a-line-of-particles-from-point-a-to-point-b-if-so-how-do-i-do-it.471775/

    // TODO: Figure out end and nether portal math

    public void startDisplaying() {
        cache.addRouting(player.getUniqueId(), this);
        float originalDistance = (float) player.getLocation().distance(endLocation);
        infoBar = BossBar.bossBar(
                Component.text("Routing to: ", NamedTextColor.YELLOW)
                        .append(Component.text(locationName, NamedTextColor.GOLD))
                        .append(Component.text(" | ", NamedTextColor.BLUE))
                        .append(Component.text("Press F to cancel", NamedTextColor.RED)),
                0.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS);
        player.showBossBar(infoBar);

        this.repeatingTask = new BukkitRunnable() {

            Location playerLocation;

            @Override
            public void run() {

                playerLocation = player.getLocation().clone();

                if(playerLocation.distance(endLocation) < 5) {
                    player.sendMessage(PlaceholderParser.of(config.PLACE_REACHED_DESTINATION).place(locationName).parse());
                    cache.removeRouting(player.getUniqueId());
                    return;
                }

                travelled = playerLocation.distance(endLocation);
                float progress = (float) Math.min(1.0, 1.0 - (travelled / originalDistance));
                if(progress <= 0.0) {
                    progress = 0.0f;
                } else if(progress >= 1.0) {
                    progress = 1.0f;
                }
                infoBar.progress(progress);

                Location startLocation = playerLocation.clone().add(0, 1, 0);
                Vector direction = endLocation.clone().subtract(startLocation).toVector().normalize();
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(78, 235, 255), 0.75f);
                for (double i = 0; i < 10; i += 0.5) {
                    Location point = playerLocation.clone().add(direction.clone().multiply(i));
                    player.spawnParticle(Particle.DUST, point, 1, dustOptions);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    private Place findNearestNetherPortal(Location location) {
        if(config.ENABLE_NETHER_PORTALS) {
            Category category = cache.getCategory("Nether Portals");
            if(category == null || category.getPlaces().isEmpty()) return null;
            Place nearestPortal;
            double nearestDistance;
            for(Place place : category.getPlaces()) {
                Location placeLocation = place.getLocation();
                nearestDistance = placeLocation.distance(location);
            }
        }
        return null;
    }

    public void cancelTask() {
        this.player.hideBossBar(infoBar);
        this.infoBar = null;
        this.repeatingTask.cancel();
    }

}
