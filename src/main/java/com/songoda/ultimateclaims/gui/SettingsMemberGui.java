package com.songoda.ultimateclaims.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimPerm;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.songoda.ultimateclaims.utils.LocaleAPI.getFormattedMessage;

public class SettingsMemberGui extends CustomizableGui {

    private final UltimateClaims plugin;
    private final Claim claim;
    private final ClaimRole role;

    public SettingsMemberGui(UltimateClaims plugin, Claim claim, Gui returnGui, ClaimRole type, Player player) {
        super(plugin, "membersettings");
        this.claim = claim;
        this.role = type;
        this.plugin = plugin;
        this.setRows(3);
        this.setTitle(getFormattedMessage(player, "interface.permsettings.title",
                "role", TextUtils.formatText(role.toString().toLowerCase(), true)));

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        // edges will be type 3
        setDefaultItem(glass3);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);

        // exit buttons
        this.setButton("back", 0, GuiUtils.createButtonItem(CompatibleMaterial.OAK_FENCE_GATE,
                        getFormattedMessage(player, "general.interface.back"),
                        getFormattedMessage(player, "general.interface.exit")),
                (event) -> event.player.closeInventory());
        this.setButton("back", 8, this.getItem(0), (event) -> guiManager.showGUI(event.player, returnGui));

        // settings
        this.setButton("break", 1, 1, CompatibleMaterial.IRON_PICKAXE.getItem(), (event) -> toggle(ClaimPerm.BREAK, player));
        this.setButton("place", 1, 2, CompatibleMaterial.STONE.getItem(), (event) -> toggle(ClaimPerm.PLACE, player));
        this.setButton("interact", 1, 3, CompatibleMaterial.LEVER.getItem(), (event) -> toggle(ClaimPerm.INTERACT, player));
        this.setButton("trading", 1, 4, CompatibleMaterial.EMERALD.getItem(), (event) -> toggle(ClaimPerm.TRADING, player));
        this.setButton("doors", 1, 5, CompatibleMaterial.OAK_DOOR.getItem(), (event) -> toggle(ClaimPerm.DOORS, player));
        this.setButton("kills", 1, 6, CompatibleMaterial.DIAMOND_SWORD.getItem(), (event) -> toggle(ClaimPerm.MOB_KILLING, player));
        this.setButton("redstone", 1, 7, CompatibleMaterial.REDSTONE.getItem(), (event) -> toggle(ClaimPerm.REDSTONE, player));
        refreshDisplay(player);
    }

    private void refreshDisplay(Player player) {
        this.updateItem("break", 1, 1,
                getFormattedMessage(player, "interface.permsettings.breaktitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.BREAK, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.BREAK, player))
                        .split("\\|"));
        this.updateItem("place", 1, 2,
                getFormattedMessage(player, "interface.permsettings.placetitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.PLACE, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.PLACE, player))
                        .split("\\|"));
        this.updateItem("interact", 1, 3,
                getFormattedMessage(player, "interface.permsettings.interacttitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.INTERACT, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.INTERACT, player))
                        .split("\\|"));

        this.updateItem("trading", 1, 4,
                getFormattedMessage(player, "interface.permsettings.tradingtitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.TRADING, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.TRADING, player))
                        .split("\\|"));

        this.updateItem("doors", 1, 5,
                getFormattedMessage(player, "interface.permsettings.doorstitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.DOORS, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.DOORS, player))
                        .split("\\|"));
        this.updateItem("kills", 1, 6,
                getFormattedMessage(player, "interface.permsettings.mobkilltitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.MOB_KILLING, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.MOB_KILLING, player))
                        .split("\\|"));
        this.updateItem("redstone", 1, 7,
                getFormattedMessage(player, "interface.permsettings.redstonetitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", role == ClaimRole.MEMBER
                                ? claim.getMemberPermissions().getStatus(ClaimPerm.REDSTONE, player) : claim.getVisitorPermissions().getStatus(ClaimPerm.REDSTONE, player))
                        .split("\\|"));

    }

    private void toggle(ClaimPerm perm, Player player) {
        if (role == ClaimRole.MEMBER) {
            claim.getMemberPermissions().setAllowed(perm, !claim.getMemberPermissions().hasPermission(perm));
            plugin.getDataManager().updatePermissions(claim, claim.getMemberPermissions(), ClaimRole.MEMBER);
        } else {
            claim.getVisitorPermissions().setAllowed(perm, !claim.getVisitorPermissions().hasPermission(perm));
            plugin.getDataManager().updatePermissions(claim, claim.getVisitorPermissions(), ClaimRole.VISITOR);
        }
        refreshDisplay(player);
    }
}
