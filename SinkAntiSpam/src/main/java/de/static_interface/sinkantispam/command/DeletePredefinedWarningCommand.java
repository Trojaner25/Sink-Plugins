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

package de.static_interface.sinkantispam.command;

import static de.static_interface.sinklibrary.configuration.LanguageConfiguration.m;

import de.static_interface.sinkantispam.SinkAntiSpam;
import de.static_interface.sinkantispam.WarnUtil;
import de.static_interface.sinkantispam.database.row.PredefinedWarning;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.exception.NotEnoughArgumentsException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class DeletePredefinedWarningCommand extends SinkCommand {

    public DeletePredefinedWarningCommand(@Nonnull Plugin plugin) {
        super(plugin);
        getCommandOptions().setIrcOpOnly(true);
        getCommandOptions().setCliOptions(new Options());
        getCommandOptions().setCmdLineSyntax("{PREFIX}{ALIAS} [WarningName]");
    }

    @Override
    protected boolean onExecute(CommandSender sender, String label, String[] args) throws ParseException {
        if (args.length < 1) {
            throw new NotEnoughArgumentsException();
        }

        PredefinedWarning pWarning = WarnUtil.getPredefinedWarning(args[1]);
        if (pWarning == null) {
            sender.sendMessage(m("SinkAntiSpam.UnknownWarning", args[1]));
            return true;
        }

        SinkAntiSpam.getInstance().getPredefinedWarningsTable().executeUpdate("DELETE FROM `{TABLE}` WHERE `id` = ?", pWarning.id);
        sender.sendMessage(ChatColor.DARK_GREEN + "Success");
        return true;
    }
}