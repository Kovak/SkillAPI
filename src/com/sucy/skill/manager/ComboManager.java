/**
 * SkillAPI
 * com.sucy.skill.manager.ComboManager
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
package com.sucy.skill.manager;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.data.Click;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manages click combos with what combos are active and
 * what skills result from various combinations
 */
public class ComboManager
{
    private int       comboSize;
    private Click[]   buffer;
    private boolean[] clicks;

    /**
     * Initializes the combo manager, grabbing settings from
     * the configuration to prepare data
     */
    public ComboManager()
    {
        comboSize = Math.min(SkillAPI.getSettings().getComboSize(), Click.MAX_COMBO_SIZE);
        clicks = new boolean[] {
                SkillAPI.getSettings().isComboLeft(),
                SkillAPI.getSettings().isComboRight(),
                SkillAPI.getSettings().isComboShift()
        };
        buffer = new Click[comboSize];
    }

    /**
     * Retrieves the accepted size of combos
     *
     * @return the accepted size of combos
     */
    public int getComboSize()
    {
        return comboSize;
    }

    /**
     * Checks whether or not the click with the given ID is enabled
     *
     * @param id ID of the click
     *
     * @return true if enabled, false otherwise
     */
    public boolean isClickEnabled(int id)
    {
        return id < clicks.length && id >= 0 && clicks[id];
    }

    /**
     * Checks whether or not the combo is a valid one
     *
     * @param id ID of the combo
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidCombo(int id)
    {
        for (int i = 0; i < comboSize; i++)
        {
            if (!isClickEnabled(Click.BIT_MASK & (id >> (i * Click.BITS))))
            {
                return false;
            }
        }
        return id > 0 && id < (1 << (Click.BITS * comboSize));
    }

    /**
     * Converts a combo ID to clicks
     *
     * @param id combo ID
     *
     * @return click combination
     */
    public List<Click> convertId(int id)
    {
        List<Click> clicks = new ArrayList<Click>(comboSize);
        for (int i = 0; i < comboSize; i++)
        {
            clicks.add(Click.getById(id & Click.BIT_MASK));
            id >>= Click.BITS;
        }
        Collections.reverse(clicks);
        return clicks;
    }

    /**
     * Converts a click combination to an ID
     *
     * @param clicks clicks to convert
     *
     * @return combo ID
     */
    public int convertCombo(Click[] clicks)
    {
        int id = 0;
        for (Click click : clicks)
        {
            id <<= Click.BITS;
            id |= click.getId();
        }
        return id;
    }

    /**
     * Converts a click combination to an ID
     *
     * @param clicks clicks to convert
     *
     * @return combo ID
     */
    public int convertCombo(Collection<Click> clicks)
    {
        return convertCombo(clicks.toArray(buffer));
    }
}
