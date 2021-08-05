package com.songoda.ultimateclaims.dynmap;

import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/*
public class Pl3xMapTask extends BukkitRunnable {
    private final MapWorld world;
    private final SimpleLayerProvider provider;

    private boolean stop;

    public Pl3xMapTask(MapWorld world, SimpleLayerProvider provider) {
        this.world = world;
        this.provider = provider;
    }

    @Override
    public void run() {
        if (stop) {
            cancel();
        }
        updateClaims();
    }

    void updateClaims() {
        provider.clearMarkers(); // TODO track markers instead of clearing them
        Collection<ClaimManager> topLevelClaims = ClaimManager.getCorners();
        if (topLevelClaims != null) {
            topLevelClaims.stream()
                    .filter(claim -> claim.getGreaterBoundaryCorner().getWorld().getUID().equals(this.world.uuid()))
                    .filter(claim -> claim.parent == null)
                    .forEach(this::handleClaim);
        }
    }

    private void handleClaim(Claim claim) {
        Location min = claim.getLesserBoundaryCorner();
        Location max = claim.getGreaterBoundaryCorner();
        if (min == null) {
            return;
        }

        Rectangle rect = Marker.rectangle(Point.of(min.getBlockX(), min.getBlockZ()), Point.of(max.getBlockX() + 1, max.getBlockZ() + 1));

        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();
        claim.getPermissions(builders, containers, accessors, managers);

        String worldName = min.getWorld().getName();

        MarkerOptions.Builder options = MarkerOptions.builder()
                .strokeColor(Config.STROKE_COLOR)
                .strokeWeight(Config.STROKE_WEIGHT)
                .strokeOpacity(Config.STROKE_OPACITY)
                .fillColor(Config.FILL_COLOR)
                .fillOpacity(Config.FILL_OPACITY)
                .clickTooltip((claim.isAdminClaim() ? Config.ADMIN_CLAIM_TOOLTIP : Config.CLAIM_TOOLTIP)
                        .replace("{world}", worldName)
                        .replace("{id}", Long.toString(claim.getID()))
                        .replace("{owner}", claim.getOwnerName())
                        .replace("{managers}", getNames(managers))
                        .replace("{builders}", getNames(builders))
                        .replace("{containers}", getNames(containers))
                        .replace("{accessors}", getNames(accessors))
                        .replace("{area}", Integer.toString(claim.getArea()))
                        .replace("{width}", Integer.toString(claim.getWidth()))
                        .replace("{height}", Integer.toString(claim.getHeight()))
                );

        rect.markerOptions(options);

        String markerid = "claims_" + worldName + "_region_" + Long.toHexString(claim.getID());
        this.provider.addMarker(Key.of(markerid), rect);
    }

    private static String getNames(List<String> list) {
        List<String> names = new ArrayList<>();
        for (String str : list) {
            try {
                UUID uuid = UUID.fromString(str);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                names.add(offlinePlayer.getName());
            } catch (Exception e) {
                names.add(str);
            }
        }
        return String.join(", ", names);
    }

    public void disable() {
        cancel();
        this.stop = true;
        this.provider.clearMarkers();
    }
}*/
