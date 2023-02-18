package com.songoda.ultimateclaims.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.songoda.ultimateclaims.utils.LocaleAPI.getFormattedMessage;

public class MembersGui extends CustomizableGui {

    private final UltimateClaims plugin;
    private final Claim claim;
    private ClaimRole displayedRole = ClaimRole.OWNER;
    private SortType sortType = SortType.DEFAULT;

    public MembersGui(UltimateClaims plugin, Claim claim, Player player) {
        super(plugin, "members");
        this.claim = claim;
        this.plugin = plugin;
        this.setRows(6);
        this.setTitle(getFormattedMessage(player, "interface.members.title"));

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

        // Member Stats (update on refresh)
        this.setItem("stats", 4, CompatibleMaterial.PAINTING.getItem());

        // Filters
        this.setButton("type", 3, CompatibleMaterial.HOPPER.getItem(), (event) -> toggleFilterType(player));
        this.setButton("sort", 5, CompatibleMaterial.HOPPER.getItem(), (event) -> toggleSort(player));

        // Settings shortcuts
        this.setButton("visitor_settings", 5, 3, GuiUtils.createButtonItem(CompatibleMaterial.OAK_SIGN,
                        getFormattedMessage(player, "interface.members.visitorsettingstitle"),
                        getFormattedMessage(player, "interface.members.visitorsettingslore").split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this,
                        ClaimRole.VISITOR, player)));

        this.setButton("member_settings", 5, 5, GuiUtils.createButtonItem(CompatibleMaterial.PAINTING,
                        getFormattedMessage(player, "interface.members.membersettingstitle"),
                        getFormattedMessage(player, "interface.members.membersettingslore").split("\\|")),
                (event) -> event.manager.showGUI(event.player, new SettingsMemberGui(plugin, claim, this,
                        ClaimRole.MEMBER, player)));

        // enable page events
        setNextPage(5, 6, GuiUtils.createButtonItem(CompatibleMaterial.ARROW,
                getFormattedMessage(player, "general.interface.next")));
        setPrevPage(5, 2, GuiUtils.createButtonItem(CompatibleMaterial.ARROW,
                getFormattedMessage(player, "general.interface.previous")));
        setOnPage((event) -> showPage(player));
        showPage(player);
    }

    private void showPage(Player player) {
        // refresh stats
        this.setItem("stats", 4, GuiUtils.updateItem(this.getItem(4),
                getFormattedMessage(player, "interface.members.statstitle"),
                getFormattedMessage(player, "interface.members.statslore",
                        "%totalmembers%", Integer.toString(claim.getOwnerAndMembers().size()),
                        "%maxmembers%", Integer.toString(Settings.MAX_MEMBERS.getInt()),
                        "%members%", Integer.toString(claim.getMembers().size()))
                        .split("\\|")));

        // Filters
        this.setItem("type", 3, GuiUtils.updateItem(this.getItem(3),
                getFormattedMessage(player, "interface.members.changetypetitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%", displayedRole == ClaimRole.OWNER ?
                                getFormattedMessage(player, "interface.role.all") :
                                getFormattedMessage(player, displayedRole.getLocalePath()))
                        .split("\\|")));
        this.setItem("sort", 5, GuiUtils.updateItem(this.getItem(5),
                getFormattedMessage(player, "interface.members.changesorttitle"),
                getFormattedMessage(player, "general.interface.current",
                        "%current%",
                        getFormattedMessage(player, sortType.getLocalePath()))
                        .split("\\|")));

        // show members
        List<ClaimMember> toDisplay = new ArrayList<>(claim.getOwnerAndMembers());
        toDisplay = toDisplay.stream()
                .filter(m -> m.getRole() == displayedRole || displayedRole == ClaimRole.OWNER)
                .sorted(Comparator.comparingInt(claimMember -> claimMember.getRole().getIndex()))
                .collect(Collectors.toList());

        if (sortType == SortType.PLAYTIME) {
            toDisplay = toDisplay.stream().sorted(Comparator.comparingLong(ClaimMember::getPlayTime))
                    .collect(Collectors.toList());
        }
        if (sortType == SortType.MEMBER_SINCE) {
            toDisplay = toDisplay.stream().sorted(Comparator.comparingLong(ClaimMember::getPlayTime))
                    .collect(Collectors.toList());
        }

        Collections.reverse(toDisplay);
        this.pages = (int) Math.max(1, Math.ceil(toDisplay.size() / (7 * 4)));
        this.page = Math.max(page, pages);

        int currentMember = 21 * (page - 1);
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                if (toDisplay.size() - 1 < currentMember) {
                    this.clearActions(row, col);
                    this.setItem(row, col, AIR);
                    continue;
                }

                ClaimMember claimMember = toDisplay.get(currentMember);
                final UUID playerUUID = toDisplay.get(currentMember).getUniqueId();
                OfflinePlayer skullPlayer = Bukkit.getOfflinePlayer(playerUUID);

                this.setItem(row, col, GuiUtils.createButtonItem(ItemUtils.getPlayerSkull(skullPlayer),
                        ChatColor.AQUA + skullPlayer.getName(),
                        getFormattedMessage(player, "interface.members.skulllore",
                                "%role%",
                                getFormattedMessage(player, toDisplay.get(currentMember).getRole().getLocalePath()),
                                "%playtime%", TimeUtils.makeReadable(claimMember.getPlayTime()),
                                "%membersince%",
                                new SimpleDateFormat("dd/MM/yyyy").format(new Date(claimMember.getMemberSince())))
                                .split("\\|")));

                currentMember++;
            }
        }
    }

    void toggleFilterType(Player player) {
        switch (displayedRole) {
            case OWNER:
                displayedRole = ClaimRole.VISITOR;
                break;
            case MEMBER:
                displayedRole = ClaimRole.OWNER;
                break;
            case VISITOR:
                displayedRole = ClaimRole.MEMBER;
                break;
        }
        showPage(player);
    }

    void toggleSort(Player player) {
        switch (sortType) {
            case DEFAULT:
                sortType = SortType.PLAYTIME;
                break;
            case PLAYTIME:
                sortType = SortType.MEMBER_SINCE;
                break;
            case MEMBER_SINCE:
                sortType = SortType.DEFAULT;
                break;
        }
        showPage(player);
    }

    public enum SortType {
        DEFAULT("interface.sortingmode.default"),
        PLAYTIME("interface.sortingmode.playtime"),
        MEMBER_SINCE("interface.sortingmode.membersince");

        private final String localePath;

        SortType(String localePath) {
            this.localePath = localePath;
        }

        public String getLocalePath() {
            return localePath;
        }
    }

}
