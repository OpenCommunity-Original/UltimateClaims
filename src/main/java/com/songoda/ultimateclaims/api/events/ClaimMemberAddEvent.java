package com.songoda.ultimateclaims.api.events;

import com.songoda.ultimateclaims.claim.Claim;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a member gets added to a claim.
 */
public class ClaimMemberAddEvent extends ClaimEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer player;
    private boolean cancel = false;

    public ClaimMemberAddEvent(Claim claim, OfflinePlayer player) {
        super(claim);
        this.player = player;
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

    public OfflinePlayer getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
