package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimDeleteReason;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a claim is deleted.
 */
public class ClaimDeleteEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final ClaimDeleteReason deleteReason;
    private boolean cancel = false;

    public ClaimDeleteEvent(Claim claim, ClaimDeleteReason deleteReason) {
        super(claim);
        this.deleteReason = deleteReason;
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

    public ClaimDeleteReason getDeleteReason() {
        return deleteReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}