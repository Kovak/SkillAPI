/**
 * SkillAPI
 * com.sucy.skill.task.InventoryTask
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
package com.sucy.skill.task;

import com.rit.sucy.config.FilterType;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.ErrorNodes;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repeating task to check for equipment requirements
 */
public class InventoryTask extends BukkitRunnable
{

    private static SkillAPI plugin;
    private        int      playersPerCheck;
    private int index = -1;

    /**
     * Task constructor
     *
     * @param p               API reference
     * @param playersPerCheck how many players to check each tick
     */
    public InventoryTask(SkillAPI p, int playersPerCheck)
    {
        this.playersPerCheck = playersPerCheck;
        if (plugin != null) return;
        plugin = p;
        runTaskTimer(plugin, 1, 1);
    }

    /**
     * Clears the plugin reference on cancel
     */
    @Override
    public void cancel()
    {
        super.cancel();
        plugin = null;
    }

    /**
     * Checks player equipment for requirements
     */
    @Override
    public void run()
    {
        Player[] players = VersionManager.getOnlinePlayers();
        for (int i = 0; i < playersPerCheck; i++)
        {
            if (!getNextPlayer(players)) return;
            if (i >= players.length) return;

            // Get the player data
            Player player = players[index];
            if (player.getGameMode() == GameMode.CREATIVE) continue;
            PlayerData data = SkillAPI.getPlayerData(player);

            // Check for lore strings
            int index = 0;
            for (ItemStack item : player.getInventory().getArmorContents())
            {
                if (cannotUse(data, item)) removeArmor(player, index);
                index++;
            }
        }
    }

    /**
     * <p>Checks if the player cannot use the item</p>
     * <p>If SkillAPI is not enabled or it's lore requirement setting
     * is disabled, this will always return false</p>
     *
     * @param player player to check for
     * @param item   item to check
     *
     * @return true if cannot use, false otherwise
     */

    public static List<Material> itemsThatCanUse = new ArrayList<Material>(Arrays.asList(
            Material.DIAMOND_AXE,
            Material.DIAMOND_BOOTS,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_SPADE,
            Material.DIAMOND_HOE,
            Material.WOOD_AXE,
            Material.WOOD_PICKAXE,
            Material.WOOD_SWORD,
            Material.WOOD_SPADE,
            Material.WOOD_HOE,
            Material.IRON_AXE,
            Material.LEATHER_BOOTS,
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.IRON_BOOTS,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_PICKAXE,
            Material.IRON_SWORD,
            Material.IRON_SPADE,
            Material.IRON_HOE,
            Material.STONE_AXE,
            Material.STONE_PICKAXE,
            Material.STONE_SWORD,
            Material.STONE_SPADE,
            Material.STONE_HOE,
            Material.GOLD_AXE,
            Material.GOLD_BOOTS,
            Material.GOLD_HELMET,
            Material.GOLD_CHESTPLATE,
            Material.GOLD_LEGGINGS,
            Material.GOLD_PICKAXE,
            Material.GOLD_SWORD,
            Material.GOLD_SPADE,
            Material.GOLD_HOE,
            Material.CHAINMAIL_BOOTS,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_LEGGINGS,
            Material.BOW
        ));

    public static boolean cannotUse(PlayerData player, ItemStack item)
    {
        if (plugin == null) return false;
        if (item == null) return false;
        if (!itemsThatCanUse.contains(item.getType())) return false;
        boolean hasRequirement = false;
        boolean needsRequirement = false;
        if (item.getItemMeta().hasLore())
        {
            List<String> lore = item.getItemMeta().getLore();

            // Check each line of the lore
            for (String line : lore)
            {
                String colorless = ChatColor.stripColor(line);

                // Level requirements
                if (colorless.matches(SkillAPI.getSettings().getLoreLevelText() + "[0-9]+"))
                {
                    int level = Integer.parseInt(colorless.substring(SkillAPI.getSettings().getLoreLevelText().length()));
                    if (!player.hasClass() || player.getMainClass().getLevel() < level)
                    {
                        return true;
                    }
                }

                // Class requirements
                else if (colorless.matches(SkillAPI.getSettings().getLoreClassText() + ".+"))
                {
                    needsRequirement = true;
                    String name = colorless.substring(SkillAPI.getSettings().getLoreClassText().length());
                    if (name.contains(", "))
                    {
                        String[] names = name.split(", ");
                        for (String n : names)
                        {
                            if (player.isClass(SkillAPI.getClass(n)))
                            {
                                hasRequirement = true;
                            }
                        }
                    }
                    else
                    {
                        if (player.isClass(SkillAPI.getClass(name)))
                        {
                            hasRequirement = true;
                        }
                    }
                }

                // Class exclusion
                else if (colorless.matches(SkillAPI.getSettings().getLoreExcludeText() + ".+"))
                {
                    String name = colorless.substring(SkillAPI.getSettings().getLoreExcludeText().length());
                    if (name.contains(", "))
                    {
                        String[] names = name.split(", ");
                        for (String n : names)
                        {
                            if (player.isClass(SkillAPI.getClass(n)))
                            {
                                return true;
                            }
                        }
                    }
                    else
                    {
                        if (player.isClass(SkillAPI.getClass(name)))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return needsRequirement != hasRequirement;
    }

    /**
     * Removes the armor piece at the given index
     *
     * @param player player to remove for
     * @param index  index of the armor piece to remove
     */
    private void removeArmor(Player player, int index)
    {
        ItemStack[] armor = player.getInventory().getArmorContents();
        player.getInventory().addItem(armor[index]);
        armor[index] = null;
        player.getInventory().setArmorContents(armor);
        SkillAPI.getLanguage().sendMessage(ErrorNodes.CANNOT_USE, player, FilterType.COLOR);
    }

    /**
     * Gets the next player to check
     *
     * @return true if found a player, false otherwise
     */
    private boolean getNextPlayer(Player[] players)
    {
        index++;

        // Limit the index
        if (index >= players.length)
        {
            players = VersionManager.getOnlinePlayers();
            index = 0;
        }

        // Make sure its a valid player
        return players.length > 0 && (players[index].isOnline() || getNextPlayer(players));
    }
}
