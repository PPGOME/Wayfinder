package me.ppgome.wayfinder.navigation;

import io.papermc.paper.entity.LookAnchor;
import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.place.Place;
import me.ppgome.wayfinder.navigation.RoutePlanner.Route;
import me.ppgome.wayfinder.navigation.RoutePlanner.RouteWaypoint;
import me.ppgome.wayfinder.text.PlaceholderParser;
import me.ppgome.wayfinder.utils.TextUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static me.ppgome.wayfinder.utils.PlaceUtils.sameWorld;

public class ParticleNavigator {

    // --- Trail tuning ------------------------------------------------------------------------------------------------
    private static final long MAIN_PERIOD_TICKS = 4; // how often the trail/boss bar refresh
    private static final double TRAIL_SPACING = 0.75; // blocks between trail particles
    private static final double TRAIL_LENGTH = 12; // how far ahead of the player the trail extends
    private static final double TRAIL_SCROLL = 0.25; // how far the pattern slides each refresh (flow effect)
    private static final int BEAD_EVERY = 4; // every Nth trail point also gets an END_ROD "bead"

    // --- Beacon tuning -----------------------------------------------------------------------------------------------
    private static final double BEACON_MAX_DISTANCE = 48; // only draw the beacon when this close
    private static final double BEACON_HEIGHT = 20;
    private static final double VERTICAL_HINT_THRESHOLD = 3; // blocks of Y difference before we show hints/beacon

    // --- Arrow tuning ------------------------------------------------------------------------------------------------
    private static final long ARROW_PERIOD_TICKS = 2;
    private static final float ARROW_SCALE = 1.5f;
    private static final float ARROW_FLAT_DEG = 90f; // lays the sprite down so its tip points along "forward"
    private static final float ARROW_ROLL_DEG = 45f; // vanilla arrow sprite points diagonally up-right
    private static final float ARROW_YAW_FIX_DEG = 0f; // set to 180 if the arrow points backward
    private static final double ARROW_FORWARD_OFFSET = 2.5;
    private static final double ARROW_SPEED_OFFSET_MULTIPLIER = 4.0; // Modifies how far forward the arrow appears when going fast
    private static final double ARROW_DOWN_OFFSET = 0.7;

    private final Wayfinder plugin;
    private final Cache cache;
    private final WConfig config;
    private final Player player;
    private final Place destination;

    private PlayerMeta playerMeta;

    private BossBar infoBar;
    private BukkitTask repeatingTask;

    private ItemDisplay arrow;
    private BukkitTask arrowTask;

    private List<RouteWaypoint> waypoints;
    private double totalDistance;
    private int step = 0;
    private int tick = 0;

    public ParticleNavigator(Wayfinder plugin, Player player, Place destination) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.player = player;
        this.destination = destination;
        this.playerMeta = cache.getPlayerMeta(player.getUniqueId());
    }

    //------------------------------------------------------------------------------------------------------------------

    public void startDisplaying() {
        Route route = new RoutePlanner(plugin, player, destination).plan();
        if (route == null || route.routeWaypoints().isEmpty()) {
            // TODO: add a config message, e.g. config.PLACE_NO_ROUTE
            player.sendMessage(Component.text("No route could be found to that place.", NamedTextColor.RED));
            return;
        }

        Location firstLoc = route.routeWaypoints().getFirst().location();

        if(sameWorld(player.getLocation(), destination.getLocation())) player.lookAt(firstLoc, LookAnchor.EYES);
        player.sendMessage(PlaceholderParser.of(config.PLACE_ROUTING_TO).place(destination.getName()).parse());
        player.sendMessage(Component.text("Located at: ", NamedTextColor.GOLD).append(TextUtils.locationToComponent(destination.getLocation())));
        player.sendMessage(Component.text("You can change the route helper (particles/arrow/both) in your place GUI's settings!", NamedTextColor.RED));
        if(!cache.getPlayerMeta(player.getUniqueId()).isEnablePortalSkipping()) {
            player.sendMessage(Component.text("You're currently avoiding portals. If you don't want this, change it in the place GUI settings.", NamedTextColor.LIGHT_PURPLE));
        } else if(route.routeWaypoints().size() > 2) {
            player.sendMessage(Component.text("This route takes you through portals. Please disable portals in the place GUI settings if you don't want this.", NamedTextColor.LIGHT_PURPLE));
        }

        this.waypoints = route.routeWaypoints();
        this.totalDistance = Math.max(route.estimatedDistance(), 1.0);

        cache.addRouting(player.getUniqueId(), this);

        infoBar = BossBar.bossBar(
                buildBossBar(totalDistance, Component.empty()),
                0.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS);
        player.showBossBar(infoBar);

        Particle.DustTransition dust = new Particle.DustTransition(Color.FUCHSIA, Color.AQUA, 1.2f);

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

                double remaining = distToTarget + waypoints.get(step).remaining();
                updateProgress(remaining);
                infoBar.name(buildBossBar(remaining, verticalHint(playerLocation, target)));

                tick++;
                if(playerMeta.isPlaceParticlesEnabled()) {
                    drawTrail(playerLocation, target, dust);
                    drawBeacon(playerLocation, target);
                }
            }
        }.runTaskTimer(plugin, 0, MAIN_PERIOD_TICKS);

        if(playerMeta.isPlaceArrowEnabled()) {
            spawnArrow();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private void drawTrail(Location from, Location target, Particle.DustTransition dust) {
        Vector dir = target.toVector().subtract(from.toVector()).setY(0);
        double horiz = dir.length();
        if (horiz < 0.5) return;
        dir.multiply(1.0 / horiz);

        double phase = (tick * TRAIL_SCROLL) % TRAIL_SPACING;
        int lastY = from.getBlockY();
        double end = Math.min(TRAIL_LENGTH, horiz);
        int n = 0;

        for (double d = 1 + phase; d < end; d += TRAIL_SPACING, n++) {
            Location p = from.clone().add(dir.clone().multiply(d));
            double y = groundY(p, lastY);
            lastY = (int) Math.floor(y);
            p.setY(y + 0.3);

            player.spawnParticle(Particle.DUST_COLOR_TRANSITION, p, 1, dust);
            if (n % BEAD_EVERY == 0) {
                player.spawnParticle(Particle.END_ROD, p.clone().add(0, 0.3, 0), 1, 0, 0, 0, 0);
            }
        }
    }

    private void drawBeacon(Location playerLoc, Location target) {
        if (playerLoc.distanceSquared(target) > BEACON_MAX_DISTANCE * BEACON_MAX_DISTANCE) return;
        if (Math.abs(target.getY() - playerLoc.getY()) < VERTICAL_HINT_THRESHOLD) return;

        for (double y = 0; y < BEACON_HEIGHT; y += 1.0) {
            player.spawnParticle(Particle.END_ROD, target.clone().add(0, y, 0), 1, 0, 0, 0, 0);
        }
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

    //------------------------------------------------------------------------------------------------------------------

    private void spawnArrow() {
        Location eye = player.getEyeLocation();
        arrow = eye.getWorld().spawn(eye, ItemDisplay.class, d -> {
            d.setVisibleByDefault(false);
            d.setPersistent(false);
            d.setItemStack(new ItemStack(Material.ARROW));
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            d.setBillboard(Display.Billboard.FIXED);
            d.setBrightness(new Display.Brightness(15, 15));
            d.setTeleportDuration(2);
            d.setGlowing(true);
            d.setGlowColorOverride(Color.FUCHSIA);
            d.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf()
                            .rotateY((float) Math.toRadians(ARROW_YAW_FIX_DEG))
                            .rotateX((float) Math.toRadians(ARROW_FLAT_DEG))
                            .rotateZ((float) Math.toRadians(ARROW_ROLL_DEG)),
                    new Vector3f(ARROW_SCALE, ARROW_SCALE, ARROW_SCALE),
                    new Quaternionf()));
        });
        player.showEntity(plugin, arrow);

        arrowTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || arrow == null || !arrow.isValid() || waypoints == null) return;

                Location target = waypoints.get(step).location();
                if (!sameWorld(player.getLocation(), target)) return;

                updateArrow(target);
            }
        }.runTaskTimer(plugin, 0, ARROW_PERIOD_TICKS);
    }

    private void updateArrow(Location target) {
        Location eye = player.getEyeLocation();
        Vector velocity = player.getVelocity();

        double speed = velocity.length();

        double dynamicForwardOffset = ARROW_FORWARD_OFFSET + (speed * ARROW_SPEED_OFFSET_MULTIPLIER);

        Location pos = eye.clone()
                .add(eye.getDirection().multiply(dynamicForwardOffset))
                .subtract(0, ARROW_DOWN_OFFSET, 0);

        Vector aim = target.clone().add(0, 1, 0).toVector().subtract(pos.toVector());
        pos.setDirection(aim);

        boolean changedWorld = !pos.getWorld().equals(arrow.getWorld());
        arrow.teleport(pos);
        if (changedWorld) {
            player.showEntity(plugin, arrow);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Boss bar

    private void updateProgress(double remaining) {
        float progress = (float) (1.0 - remaining / totalDistance);
        infoBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));
    }

    private Component buildBossBar(double remaining, Component hint) {
        return Component.text("Routing to: ", NamedTextColor.YELLOW)
                .append(Component.text(destination.getName(), NamedTextColor.GOLD))
                .append(Component.text(" | ", NamedTextColor.BLUE))
                .append(TextUtils.parseMiniMessage("<red>Press <key:key.swapOffhand> to cancel</red>"))
                .append(Component.text(" | ", NamedTextColor.BLUE))
                .append(Component.text("Distance: ", NamedTextColor.YELLOW))
                .append(Component.text((int) remaining, NamedTextColor.GOLD))
                .append(hint);
    }

    private Component verticalHint(Location playerLoc, Location target) {
        int dy = (int) Math.round(target.getY() - playerLoc.getY());
        if (Math.abs(dy) < VERTICAL_HINT_THRESHOLD) return Component.empty();
        return Component.text(" " + (dy > 0 ? "\u2191" : "\u2193") + Math.abs(dy), NamedTextColor.AQUA);
    }

    //------------------------------------------------------------------------------------------------------------------

    private void stop() {
        cancelTask();
        cache.removeRouting(player.getUniqueId());
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
        if (arrowTask != null) {
            arrowTask.cancel();
            arrowTask = null;
        }
        if (arrow != null) {
            arrow.remove();
            arrow = null;
        }
    }
}