/*
 * Copyright (c) 2014 http://adventuria.eu, http://static-interface.de and contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkcommands.command;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.user.IngameUser;
import de.static_interface.sinklibrary.util.BukkitUtil;
import de.static_interface.sinklibrary.util.StringUtil;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownCommand extends SinkCommand {

    public static final String PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "CountDown" + ChatColor.DARK_RED + "] " + ChatColor.RESET;
    static long secondsLeft;

    public CountdownCommand(Plugin plugin) {
        super(plugin);
        getCommandOptions().setIrcOpOnly(true);
        getCommandOptions().setCliOptions(buildOptions());
        getCommandOptions().setCmdLineSyntax("{PREFIX}{ALIAS} <options> <reason>");
    }

    private Options buildOptions() {
        Options options = new Options();
        Option global = OptionBuilder
                .withDescription("Announce countdown globally")
                .withLongOpt("global")
                .create("g");

        Option radius = OptionBuilder
                .hasArg()
                .withLongOpt("radius")
                .withDescription("Set countdown announce radius")
                .withType(Number.class)
                .withArgName("value")
                .create("r");

        Option time = OptionBuilder
                .hasArg()
                .withLongOpt("time")
                .withDescription("Set time")
                .withType(Number.class)
                .withArgName("seconds")
                .create("t");

        Option command = OptionBuilder
                .hasArg()
                .withLongOpt("command")
                .withArgName("command")
                .withDescription("Execute command on finish")
                .withType(String.class)
                .create("c");

        Option skipLastMsg = OptionBuilder
                .withLongOpt("skipmsg")
                .withDescription("Skip message when countdown finishes")
                .create("s");

        options.addOption(global);
        options.addOption(radius);
        options.addOption(time);
        options.addOption(command);
        options.addOption(skipLastMsg);

        return options;
    }

    @Override
    public boolean onExecute(CommandSender sender, String label, String[] args) throws ParseException {
        if (args.length < 1) {
            return false;
        }
        if (secondsLeft > 0) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Es läuft bereits ein Countdown!");
        }

        int seconds = 30;
        if (getCommandLine().hasOption("t")) {
            seconds = ((Number) getCommandLine().getParsedOptionValue("t")).intValue();
        }

        if (seconds < 0) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Countdown darf nicht negative sein!");
            return true;
        }

        if (seconds > 60) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Max Zeit: 60 Sekunden");
            return true;
        }

        if (getCommandLine().hasOption("g") && getCommandLine().hasOption("r")) {
            sender.sendMessage(ChatColor.DARK_RED + "You can't use the -g and the -r flag at the same time!");
            return true;
        }

        String message = StringUtil.formatArrayToString(args, " ");
        secondsLeft = seconds;

        String command = null;
        if (getCommandLine().hasOption("c")) {
            command = (String) getCommandLine().getParsedOptionValue("c");
        }

        boolean skipLastMsg = getCommandLine().hasOption("s");

        if (getCommandLine().hasOption("g")) {
            broadcastCounterGlobal(sender, message, command, skipLastMsg);
            return true;
        }

        int radius = 30;
        if (getCommandLine().hasOption("r")) {
            radius = ((Number) getCommandLine().getParsedOptionValue("r")).intValue();
        }

        SinkUser executor = SinkLibrary.getInstance().getUser((Object) sender);
        if (!(executor instanceof IngameUser)) {
            sender.sendMessage(ChatColor.DARK_RED + "Only ingame players can use this without the -g flag");
            sender.sendMessage(ChatColor.DARK_RED + "Please use " + getCommandPrefix() + "countdown -g <options> <message>");
            return true;
        }
        broadcastCounterLocal((IngameUser) executor, message, command, radius, skipLastMsg);
        return true;
    }

    private void broadcastCounterGlobal(final CommandSender sender, String message, final String command, final boolean skipLastMsg) {
        BukkitUtil.broadcastMessage(PREFIX + ChatColor.GOLD + "Countdown für " + message + " gestartet!", true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    secondsLeft = 0;
                    if (!skipLastMsg) {
                        BukkitUtil.broadcastMessage(PREFIX + ChatColor.GREEN + "Los!", true);
                    }
                    if (command != null) {
                        Bukkit.dispatchCommand(sender, command);
                    }
                    cancel();
                    return;
                }
                if (secondsLeft > 10) {
                    if (secondsLeft % 10 == 0) {
                        BukkitUtil.broadcastMessage(PREFIX + ChatColor.RED + secondsLeft + "...", true);
                    }
                } else {
                    BukkitUtil.broadcastMessage(PREFIX + ChatColor.DARK_RED + secondsLeft, true);
                }
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void broadcastCounterLocal(final IngameUser executor, String message, final String command, final int radius, final boolean skipLastMsg) {
        BukkitUtil.broadcastMessage(PREFIX + ChatColor.GOLD + "Countdown für " + message + " gestartet!", true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    secondsLeft = 0;
                    if (!skipLastMsg) {
                        for (IngameUser user : executor.getUsersAround(radius)) {
                            user.sendMessage(PREFIX + ChatColor.GREEN + "Los!");
                        }
                    }
                    if (command != null) {
                        Bukkit.dispatchCommand(executor.getSender(), command);
                    }
                    cancel();
                    return;
                }
                if (secondsLeft > 10) {
                    if (secondsLeft % 10 == 0) {
                        for (IngameUser user : executor.getUsersAround(radius)) {
                            user.sendMessage(PREFIX + ChatColor.RED + secondsLeft + "...");
                        }
                    }
                } else {
                    for (IngameUser user : executor.getUsersAround(radius)) {
                        user.sendMessage(PREFIX + ChatColor.DARK_RED + secondsLeft);
                    }
                }
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0, 20L);
    }
}
