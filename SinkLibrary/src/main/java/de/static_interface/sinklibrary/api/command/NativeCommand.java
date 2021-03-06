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

package de.static_interface.sinklibrary.api.command;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.annotation.PermissionMessage;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.configuration.GeneralLanguage;
import de.static_interface.sinklibrary.util.CommandUtil;
import de.static_interface.sinklibrary.util.StringUtil;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.List;

import javax.annotation.Nullable;

public class NativeCommand extends Command implements PluginIdentifiableCommand, ConfigurableCommand {

    private Plugin owningPlugin;
    private CommandExecutor executor;
    private TabCompleter completer;

    public NativeCommand(String name, Plugin plugin, CommandExecutor executor) {
        super(name);
        Validate.notNull(executor);
        Validate.notNull(plugin);
        this.owningPlugin = plugin;
        this.executor = executor;
        completer = SinkLibrary.getInstance().getDefaultTabCompleter();
        CommandUtil.initAnnotations(this, executor.getClass());
        PermissionMessage permissionMessage = executor.getClass().getAnnotation(PermissionMessage.class);
        if (permissionMessage != null && !StringUtil.isEmptyOrNull(permissionMessage.value())) {
            setPermissionMessage(permissionMessage.value());
        } else {
            setPermissionMessage(GeneralLanguage.PERMISSIONS_GENERAL.format());
        }

    }

    public TabCompleter getTabCompleter() {
        return this.completer;
    }

    public void setTabCompleter(TabCompleter completer) {
        this.completer = completer;
    }

    public CommandExecutor getExecutor() {
        return this.executor;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor == null ? this.owningPlugin : executor;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws CommandException, IllegalArgumentException {
        org.apache.commons.lang.Validate.notNull(sender, "Sender cannot be null");
        org.apache.commons.lang.Validate.notNull(args, "Arguments cannot be null");
        org.apache.commons.lang.Validate.notNull(alias, "Alias cannot be null");
        List completions = null;

        try {
            if (completer != null) {
                completions = completer.onTabComplete(sender, this, alias, args);
            }

            if (completions == null && executor instanceof TabCompleter) {
                completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
            }
        } catch (Throwable thr) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command \'/").append(alias).append(' ');

            for (String arg : args) {
                message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("\' in plugin ").append(getPlugin().getDescription().getFullName());
            throw new CommandException(message.toString(), thr);
        }

        return completions == null ? super.tabComplete(sender, alias, args) : completions;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!owningPlugin.isEnabled()) {
            return false;
        }

        if (!StringUtil.isEmptyOrNull(getPermission()) && !testPermission(sender)) {
            return false;
        }

        boolean success;
        try {
            success = executor.onCommand(sender, this, commandLabel, args);
        } catch (Throwable thr) {
            throw new CommandException(
                    "Unhandled exception executing command \'" + commandLabel + "\' in plugin " + owningPlugin.getDescription().getFullName(), thr);
        }

        if (!success && usageMessage.length() > 0) {
            String[] lines = usageMessage.replace("<command>", commandLabel).split("\n");

            for (String line : lines) {
                sender.sendMessage(line);
            }
        }
        return success;
    }

    @Override
    public Plugin getPlugin() {
        return owningPlugin;
    }

    @Override
    public String getConfigPath() {
        return "Commands." + WordUtils.capitalizeFully(getName());
    }

    @Nullable
    @Override
    public List<String> getCommandAliases() {
        return getAliases();
    }

    @Override
    public void setCommandAliases(List<String> aliases) {
        setAliases(aliases);
    }

    @Nullable
    @Override
    public String getCommandUsage() {
        return getUsage();
    }

    @Override
    public void setCommandUsage(String usage) {
        setUsage(usage);
    }

    @Nullable
    @Override
    public String getCommandDescription() {
        return getDescription();
    }

    @Override
    public void setCommandDescription(String description) {
        setDescription(description);
    }

    @Nullable
    @Override
    public String getDefaultPermission() {
        return getPlugin().getName().replace(" ", "_").toLowerCase() + ".command." + getName().toLowerCase();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(", ").append(this.owningPlugin.getDescription().getFullName()).append(')');
        return stringBuilder.toString();
    }

    @Nullable
    @Override
    public Configuration getConfig() {
        if (executor instanceof SinkCommand) {
            return ((SinkCommand) executor).getConfig();
        }
        return null;
    }

    @Nullable
    @Override
    public String getCommandPermission() {
        return getPermission();
    }

    @Override
    public void setCommandPermission(String permission) {
        setPermission(permission);
    }
}
