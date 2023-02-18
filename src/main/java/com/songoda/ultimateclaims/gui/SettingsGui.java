package com.songoda.ultimateclaims.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimSetting;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.songoda.ultimateclaims.utils.LocaleAPI.getFormattedMessage;

public class SettingsGui extends CustomizableGui {

    private final UltimateClaims plugin;
    private final Claim claim;
    private final boolean hostilemobspawning, firespread, pvp, mobgriefing, leafdecay, tnt, fly;

    public SettingsGui(UltimateClaims plugin, Claim claim, Player player) {
        super(plugin, "settings");
        this.claim = claim;
        this.plugin = plugin;
        this.setRows(3);
        this.setTitle(getFormattedMessage(player, "interface.settings.title"));

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        // edges will be type 3
        setDefaultItem(glass3);

        // decorate corners
        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 1, true, true, glass2);

        // exit buttons
        this.setButton("back", 0, GuiUtils.createButtonItem(CompatibleMaterial.OAK_FENCE_GATE,
                        getFormattedMessage(player, "general.interface.back")),
                (event) -> guiManager.showGUI(event.player, claim.getPowerCell().getGui(event.player)));
        this.setButton("back", 8, this.getItem(0),
                (event) -> guiManager.showGUI(event.player, claim.getPowerCell().getGui(event.player)));

        // shortcuts for member settings
        this.setButton("visitors", rows - 1, 3, GuiUtils.createButtonItem(CompatibleMaterial.OAK_SIGN,
                        getFormattedMessage(player, "interface.members.visitorsettingstitle"),
                        getFormattedMessage(player, "interface.members.visitorsettingslore").split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.VISITOR, player)));

        this.setButton("visitors", rows - 1, 5, GuiUtils.createButtonItem(CompatibleMaterial.PAINTING,
                        getFormattedMessage(player, "interface.members.membersettingstitle"),
                        getFormattedMessage(player, "interface.members.membersettingslore").split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this, ClaimRole.MEMBER, player)));

        this.setItem(1, 4, AIR);
        if (hostilemobspawning = player.hasPermission("ultimateclaims.toggle.hostilemobspawning")) {
            this.setButton("hostilemobspawning", 1, 1, CompatibleMaterial.ZOMBIE_SPAWN_EGG.getItem(), (event) -> toggle(ClaimSetting.HOSTILE_MOB_SPAWNING, player));
        }
        if (firespread = player.hasPermission("ultimateclaims.toggle.firespread")) {
            this.setButton("flintandsteal", 1, 2, CompatibleMaterial.FLINT_AND_STEEL.getItem(), (event) -> toggle(ClaimSetting.FIRE_SPREAD, player));
        }
        if (pvp = player.hasPermission("ultimateclaims.toggle.pvp")) {
            this.setButton("pvp", 1, 3, CompatibleMaterial.DIAMOND_SWORD.getItem(), (event) -> toggle(ClaimSetting.PVP, player));
        }
        if (mobgriefing = player.hasPermission("ultimateclaims.toggle.mobgriefing")) {
            this.setButton("mobgriefing", 1, 4, CompatibleMaterial.GUNPOWDER.getItem(), (event) -> toggle(ClaimSetting.MOB_GRIEFING, player));
        }
        if (leafdecay = player.hasPermission("ultimateclaims.toggle.leafdecay")) {
            this.setButton("leafdecay", 1, 5, CompatibleMaterial.OAK_LEAVES.getItem(), (event) -> toggle(ClaimSetting.LEAF_DECAY, player));
        }
        if (tnt = player.hasPermission("ultimateclaims.toggle.tnt")) {
            this.setButton("tnt", 1, 6, CompatibleMaterial.TNT.getItem(), (event) -> toggle(ClaimSetting.TNT, player));
        }
        if (fly = player.hasPermission("ultimateclaims.toggle.fly")) {
            this.setButton("tnt", 1, 7, CompatibleMaterial.ELYTRA.getItem(), (event) -> toggle(ClaimSetting.FLY, player));
        }

        refreshDisplay(player);
    }

    private void refreshDisplay(Player player) {
        if (hostilemobspawning) {
            this.updateItem("hostilemobspawning", 1, 1,
                    getFormattedMessage(player, "interface.settings.hostilemobspawningtitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.HOSTILE_MOB_SPAWNING, player))
                            .split("\\|"));
        }
        if (firespread) {
            this.updateItem("flintandsteal", 1, 2,
                    getFormattedMessage(player, "interface.settings.firespreadtitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.FIRE_SPREAD, player))
                            .split("\\|"));
        }
        if (pvp) {
            this.updateItem("pvp", 1, 3,
                    getFormattedMessage(player, "interface.settings.pvptitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.PVP, player))
                            .split("\\|"));
        }
        if (mobgriefing) {
            this.updateItem("mobgriefing", 1, 4,
                    getFormattedMessage(player, "interface.settings.mobgriefingtitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.MOB_GRIEFING, player))
                            .split("\\|"));
        }
        if (leafdecay) {
            this.updateItem("leafdecay", 1, 5,
                    getFormattedMessage(player, "interface.settings.leafdecaytitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.LEAF_DECAY, player))
                            .split("\\|"));
        }
        if (tnt) {
            this.updateItem("tnt", 1, 6,
                    getFormattedMessage(player, "interface.settings.tnttitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.TNT, player))
                            .split("\\|"));
        }
        if (fly) {
            this.updateItem("tnt", 1, 7,
                    getFormattedMessage(player, "interface.settings.flytitle"),
                    getFormattedMessage(player, "general.interface.current", "%current%", claim.getClaimSettings().getStatus(ClaimSetting.FLY, player))
                            .split("\\|"));
        }
    }

    private void toggle(ClaimSetting setting, Player player) {
        claim.getClaimSettings().setEnabled(setting, !claim.getClaimSettings().isEnabled(setting));
        plugin.getDataManager().updateSettings(claim, claim.getClaimSettings());
        refreshDisplay(player);
    }
}
