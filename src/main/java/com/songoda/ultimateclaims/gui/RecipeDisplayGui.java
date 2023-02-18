package com.songoda.ultimateclaims.gui;

import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.items.PowerCellItem;
import com.songoda.ultimateclaims.settings.Settings;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.songoda.ultimateclaims.utils.LocaleAPI.getFormattedMessage;

public class RecipeDisplayGui extends Gui {

    public RecipeDisplayGui(Player player) {
        this.setRows(3);
        this.setTitle(getFormattedMessage(player, "interface.recipe.title"));
        this.setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial()));

        Map<Integer, PowerCellItem> recipe = UltimateClaims.getInstance().getItemManager().getRecipe();
        for (Map.Entry<Integer, PowerCellItem> item : recipe.entrySet()) {
            setItem(item.getKey(), item.getValue().getDisplayItem());
        }
    }
}
