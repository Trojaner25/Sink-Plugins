/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkchat;

import de.static_interface.sinkchat.config.ScSettings;
import de.static_interface.sinklibrary.configuration.IngameUserConfiguration;
import de.static_interface.sinklibrary.user.IngameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Util {

    public static boolean isInRange(IngameUser sender, Player p, int range) {
        return range < 1 || p.getWorld() == sender.getPlayer().getWorld() && sender.getDistance(p) <= range;
    }

    public static String getSpyPrefix() {
        return ScSettings.SC_PREFIX_SPY.format() + ' ' + ChatColor.RESET;
    }

    public static boolean canSpySender(IngameUser user, IngameUser toSpy) {
        IngameUserConfiguration config = user.getConfiguration();

        // Check for spy
        boolean canSpy = user.hasPermission("sinkchat.spy.all") || (user.hasPermission("sinkchat.spy")
                                                                    && !toSpy.hasPermission("sinkchat.spy.bypass"));
        return canSpy && config.isSpyEnabled();
    }
}
