package me.ppgome.wayfinder.tasks;

import me.ppgome.wayfinder.Cache;
import me.ppgome.wayfinder.Wayfinder;
import me.ppgome.wayfinder.database.place.Place;

public class TwoMinuteTask implements Runnable {

    private Wayfinder plugin;
    private Cache cache;

    public TwoMinuteTask(Wayfinder plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCache();
    }

    @Override
    public void run() {
        // Remove expiring places
        for(Place place : cache.getAllExpiringPlaces()) {
            long currentTime = System.currentTimeMillis();
            if(place.getExpires() < currentTime) {
                cache.removeExpiringPlace(place);
            }
        }
    }
}
