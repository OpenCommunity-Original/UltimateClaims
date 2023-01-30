package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is unbanned from a claim.
 */
public class ClaimPlayerUnbanEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player executor;
    private final OfflinePlayer unbannedPlayer;
    private boolean cancel = false;

    public ClaimPlayerUnbanEvent(Claim claim, Player executor, OfflinePlayer unbannedPlayer) {
        super(claim);
        this.executor = executor;
        this.unbannedPlayer = unbannedPlayer;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public OfflinePlayer getUnbannedPlayer() {
        return unbannedPlayer;
    }

    public Player getExecutor() {
        return executor;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}