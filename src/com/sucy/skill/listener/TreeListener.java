/**
 * SkillAPI
 * com.sucy.skill.listener.TreeListener
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.listener;

import com.rit.sucy.config.FilterType;
import com.rit.sucy.items.InventoryManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.ErrorNodes;
import com.sucy.skill.tree.basic.InventoryTree;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

/**
 * Listener for Skill Tree click events
 */
public class TreeListener implements Listener
{
    public static final String CLASS_LIST_KEY = "sapiClassInv";

    /**
     * Initializes a new listener for skill trees. You should not
     * use this as it is handled by the API.
     *
     * @param plugin API plugin reference
     */
    public TreeListener(SkillAPI plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles skill tree interaction
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        // Class selection
        if (InventoryManager.isMatching(event.getInventory(), CLASS_LIST_KEY))
        {
            event.setCancelled(true);
            PlayerData data = SkillAPI.getPlayerData((Player) event.getWhoClicked());
            Collection<PlayerClass> classes = data.getClasses();
            boolean top = event.getRawSlot() < event.getView().getTopInventory().getSize();
            if (classes.size() > event.getSlot() && top && event.getSlot() >= 0)
            {

                PlayerClass c = classes.toArray(new PlayerClass[classes.size()])[event.getSlot()];
                if (c.getData().getSkills().size() > 0)
                {
                    event.getWhoClicked().openInventory(((InventoryTree) c.getData().getSkillTree()).getInventory(data));
                }
                else
                {
                    SkillAPI.getLanguage().sendMessage(ErrorNodes.NO_SKILLS, (Player) event.getWhoClicked(), FilterType.COLOR);
                }
            }
        }

        // Make sure its a skill tree inventory
        else if (InventoryManager.isMatching(event.getInventory(), InventoryTree.INVENTORY_KEY))
        {
            InventoryTree tree = (InventoryTree) SkillAPI.getClass(event.getInventory().getName()).getSkillTree();

            // Do nothing when clicking outside the inventory
            if (event.getSlot() == -999)
            {
                return;
            }

            boolean top = event.getRawSlot() < event.getView().getTopInventory().getSize();

            // Interact with the skill tree when clicking in the top region
            if (top)
            {
                event.setCancelled(tree.checkClick(event.getSlot()));

                // If they clicked on a skill, try upgrading it
                if (tree.isSkill(event.getWhoClicked(), event.getSlot()))
                {
                    PlayerData player = SkillAPI.getPlayerData((Player) event.getWhoClicked());
                    if (event.isLeftClick())
                    {
                        if (player.upgradeSkill(tree.getSkill(event.getSlot())))
                        {
                            tree.show(player.getPlayer());
                        }
                    }
                    else if (event.isRightClick() && SkillAPI.getSettings().isAllowDowngrade())
                    {
                        if (player.downgradeSkill(tree.getSkill(event.getSlot())))
                        {
                            tree.update(player);
                        }
                    }
                }
            }

            // Do not allow shift clicking items into the inventory
            else if (event.isShiftClick())
            {
                event.setCancelled(true);
            }
        }
    }
}
