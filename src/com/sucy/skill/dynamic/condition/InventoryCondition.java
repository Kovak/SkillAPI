/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.InventoryCondition
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
package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A condition for dynamic skills that requires the target to have a specified item
 */
public class InventoryCondition extends EffectComponent
{
    private static final String MATERIAL = "material";
    private static final String DATA     = "data";
    private static final String AMOUNT   = "amount";

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        String item = settings.getString(MATERIAL, "").toUpperCase().replace(" ", "_");
        short data = (short) settings.getInt(DATA, 0);
        int amount = settings.getInt(AMOUNT, 1);
        ItemStack check;
        try
        {
            check = new ItemStack(Material.valueOf(item), 1, data);
        }
        catch (Exception ex)
        {
            return false;
        }

        for (LivingEntity target : targets)
        {
            if (!(target instanceof Player)) continue;

            if (((Player) target).getInventory().containsAtLeast(check, amount))
            {
                list.add(target);
            }
        }

        return list.size() > 0 && executeChildren(caster, level, list);
    }
}
