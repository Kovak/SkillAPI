/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.DamageLoreMechanic
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
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deals damage based on a held item's lore to each target
 */
public class DamageLoreMechanic extends EffectComponent
{
    private static final String REGEX      = "regex";
    private static final String MULTIPLIER = "multiplier";

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
        boolean isSelf = targets.size() == 1 && targets.get(0) == caster;
        String regex = settings.getString(REGEX, "Damage: {value}");
        regex = regex.replace("{value}", "([0-9]+)");
        Pattern pattern = Pattern.compile(regex);
        double m = attr(caster, MULTIPLIER, level, 1.0, isSelf);
        boolean worked = false;
        for (LivingEntity target : targets)
        {
            if (target.getEquipment() == null || target.getEquipment().getItemInHand() == null)
            {
                continue;
            }
            ItemStack hand = caster.getEquipment().getItemInHand();
            if (!hand.hasItemMeta() || !hand.getItemMeta().hasLore())
            {
                continue;
            }
            List<String> lore = hand.getItemMeta().getLore();
            for (String line : lore)
            {
                line = ChatColor.stripColor(line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                {
                    String value = matcher.group(1);
                    try
                    {
                        double base = Double.parseDouble(value);
                        if (base * m > 0)
                        {
                            skill.damage(target, base * m, caster);
                            worked = true;
                            break;
                        }
                    }
                    catch (Exception ex)
                    {
                        // Not a valid value
                    }
                }
            }
        }
        return worked;
    }
}
