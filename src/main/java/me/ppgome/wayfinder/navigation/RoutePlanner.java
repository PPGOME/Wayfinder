package me.ppgome.wayfinder.navigation;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.WConfig;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.PlayerMeta;
import me.ppgome.wayfinder.database.place.NetherPortalLink;
import me.ppgome.wayfinder.database.place.Place;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class RoutePlanner {

    private final Wayfinder plugin;
    private final Cache cache;
    private final WConfig config;
    private Logger log;

    private final static double PORTAL_COST = 20.0;
    private final static double DIRECT_BIAS = 0.9;

    Player player;
    private PlayerMeta playerMeta;
    private final Place toPlace;

    public RoutePlanner(Wayfinder plugin, Player player, Place place) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
        this.config = plugin.getConfiguration();
        this.player = player;
        this.toPlace = place;
        this.playerMeta = plugin.getCache().getPlayerMeta(player.getUniqueId());
        log = plugin.getLogger();
    }

    private static final class Node {
        final Location loc;
        final Place place;
        final List<Edge> edges = new ArrayList<>();
        Node(Location loc, Place place) { this.loc = loc; this.place = place; }
    }
    private record Edge(Node to, double cost) {}
    private record Result(List<Node> path, Map<Node, Double> distance, double cost) {}

    public enum Type { DIRECT, NETHER, END }
    public record RouteWaypoint(Location location, Place place, double remaining) {}
    public record Route(List<RouteWaypoint> routeWaypoints, double estimatedDistance, Type type) {}

    // -----------------------------------------------------------------------------------------------------------------

    public Route plan() {
        Location from = player.getLocation();
        Location to = toPlace.getLocation();

        Node start = new Node(from, null);
        Node end = new Node(to, null);
        List<Node> nodes = new ArrayList<>(List.of(start, end));

        boolean overworldToOverworld = from.getWorld().equals(to.getWorld()) &&
                from.getWorld().getEnvironment() == World.Environment.NORMAL;
        boolean skipPortals = overworldToOverworld && !playerMeta.isEnablePortalSkipping();

        debugMessage("from=" + fmt(from) + " enabled=" + config.ENABLE_NETHER_PORTALS);
        Node endPlatform = null;
        if(!skipPortals) {
            if(config.ENABLE_NETHER_PORTALS) addNetherPortals(nodes);
            endPlatform = addEnd(nodes);
        }
        connectWalking(nodes);

        for (Node n : nodes) {
            StringBuilder sb = new StringBuilder(fmt(n.loc) + " -> ");
            for (Edge e : n.edges) sb.append(fmt(e.to().loc)).append(" (").append(Math.round(e.cost())).append("), ");
            debugMessage(sb.toString());
        }
        debugMessage("PORTAL_COST=" + PORTAL_COST);

        Result result = pathfind(start, end);
        debugMessage("result=" + (result == null ? "null" : result.cost() + " via " + result.path().stream().map(n -> fmt(n.loc)).toList()));
        if(result == null) return null;

        if(from.getWorld().equals(to.getWorld())) {
            double direct = from.distance(to);
            debugMessage("direct=" + direct + " bias threshold=" + (direct * DIRECT_BIAS));
            if(result.cost() >= direct * DIRECT_BIAS) {
                return new Route(List.of(new RouteWaypoint(to, toPlace, 0)), direct, Type.DIRECT);
            }
        }
        return toRoute(result, endPlatform);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private void addNetherPortals(List<Node> nodes) {

        int added = 0, noLoc = 0, noLink = 0, noPartner = 0;

        for(Place netherPortalPlace : cache.getAllNetherPortalPlaces()) {
            if(netherPortalPlace == null || netherPortalPlace.getLocation() == null) {
                noLoc++;
                debugMessage("skipped (no location): " + (netherPortalPlace == null ? "null place" : netherPortalPlace.getName()));
                continue;
            }
            NetherPortalLink link = netherPortalPlace.getNetherPortalLink();
            if(link == null) { noLink++; continue; }
            if(link == null || link.getPartnerLocation() == null) {
                noPartner++;
                debugMessage("skipped (no partner): " + netherPortalPlace.getName() + " at " + fmt(netherPortalPlace.getLocation()));
                continue;
            }

            Node a = new Node(netherPortalPlace.getLocation(), netherPortalPlace);
            Node b = new Node(link.getPartnerLocation(), netherPortalPlace);

            a.edges.add(new Edge(b, PORTAL_COST));
            b.edges.add(new Edge(a, PORTAL_COST));

            nodes.add(a);
            nodes.add(b);

            added++;
            debugMessage("portal " + netherPortalPlace.getName() + ": " + fmt(netherPortalPlace.getLocation()) + " <-> " + fmt(link.getPartnerLocation()));
        }
        debugMessage("nether portals: added=" + added + " noLoc=" + noLoc
                + " noLink=" + noLink + " noPartner=" + noPartner);
    }

    private static String fmt(Location l) {
        return l.getWorld().getName() + " " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    private Node addEnd(List<Node> nodes) {
        World end = findWorld(World.Environment.THE_END);
        if(end == null) return null;

        Node platform = new Node(new Location(end, 100.5, 49, 0.5), null);
        Node exit = new Node(new Location(end, 0.5, 64, 0.5), null);
        Node respawn = new Node(getEndReturnPoint(), null);

        exit.edges.add(new Edge(respawn, PORTAL_COST));

        for(Place endPortalPlace : cache.getAllEndPortalPlaces()) {
            if(endPortalPlace == null || endPortalPlace.getLocation() == null) continue;
            Node entryPortal = new Node(endPortalPlace.getLocation(), endPortalPlace);
            entryPortal.edges.add(new Edge(platform, PORTAL_COST));
            nodes.add(entryPortal);
        }
        nodes.addAll(List.of(platform, exit, respawn));
        return platform;
    }

    private void connectWalking(List<Node> nodes) {
        Map<World, List<Node>> byWorld = new HashMap<>();
        for(Node node : nodes) {
            byWorld.computeIfAbsent(node.loc.getWorld(),  world -> new ArrayList<>()).add(node);
        }
        for(List<Node> group : byWorld.values()) {
            for(int i = 0; i < group.size(); i++) {
                for(int p = i + 1; p < group.size(); p++) {
                    Node a = group.get(i);
                    Node b = group.get(p);
                    double distance = a.loc.distance(b.loc);
                    a.edges.add(new Edge(b, distance));
                    b.edges.add(new Edge(a, distance));
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    // Find using Dijkstra's algorithm
    private Result pathfind(Node start, Node destination) {
        record Entry(Node node, double distance) {}

        Map<Node, Double> distance = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparingDouble(Entry::distance));

        distance.put(start, 0.0);
        queue.add(new Entry(start, 0));

        while(!queue.isEmpty()) {
            Entry current = queue.poll();
            if(current.distance() > distance.getOrDefault(current.node, Double.MAX_VALUE)) continue;
            if(current.node() == destination) break;

            for(Edge edge : current.node().edges) {
                double nd = current.distance() + edge.cost();
                if(nd < distance.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    distance.put(edge.to, nd);
                    previous.put(edge.to, current.node);
                    queue.add(new Entry(edge.to, nd));
                }
            }
        }

        if(!distance.containsKey(destination)) return null;
        LinkedList<Node> path = new LinkedList<>();
        for(Node n = destination; n != null; n = previous.get(n)) path.addFirst(n);
        return new Result(path, distance, distance.get(destination));
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Route toRoute(Result result, Node endPlatform) {
        List<Node> path = result.path;
        Node last = path.get(path.size() - 1);
        List<RouteWaypoint> routeWaypoints = new ArrayList<>();
        boolean usedEnd = false;
        boolean crossed = false;

        for(int i = 1; i < path.size(); i++) {
            Node previous = path.get(i - 1);
            Node current = path.get(i);
            boolean arrival = !previous.loc.getWorld().equals(current.loc.getWorld());

            if(current == endPlatform) usedEnd = true;
            if(arrival) crossed = true;

            if(arrival && current != last) continue;
            double remaining = result.cost - result.distance.get(current);
            routeWaypoints.add(new RouteWaypoint(current.loc, current.place, remaining));
        }
        Type type = usedEnd ? Type.END : crossed ? Type.NETHER : Type.DIRECT;
        return new Route(routeWaypoints, result.cost, type);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private static World findWorld(World.Environment env) {
        for(World world : Bukkit.getWorlds()) {
            if(world.getEnvironment() == env) return world;
        }
        return null;
    }

    /** Where the player ends up after leaving the End: their bed, or 0,0 if none. */
    private Location getEndReturnPoint() {
        Location bed = player.getRespawnLocation();
        if (bed != null) return bed;
        return findWorld(World.Environment.NORMAL).getSpawnLocation();
    }

    private void debugMessage(String message) {
        if(config.DEBUG) log.info(message);
    }
}
