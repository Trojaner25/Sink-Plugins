/*
 * Copyright (c) 2013 - 2014 http://static-interface.de and contributors
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

package de.static_interface.sinklibrary.user;

import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.sender.IrcCommandSender;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.api.user.SinkUserProvider;
import de.static_interface.sinklibrary.configuration.GeneralSettings;
import de.static_interface.sinklibrary.util.Debug;
import de.static_interface.sinklibrary.util.SinkIrcReflection;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.pircbotx.User;

import javax.annotation.Nonnull;

public class IrcUser extends SinkUser<User> {

    private User base;
    private boolean online;
    private IrcCommandSender sender;

    public IrcUser(@Nonnull User base, @Nonnull SinkUserProvider provider, String source) {
        super(base, provider);
        this.base = base;
        online = true;
        sender = new IrcCommandSender(this, source);
    }

    public String getName() {
        return base.getNick();
    }

    @Override
    public String getDisplayName() {
        if (isOp()) {
            return ChatColor.DARK_RED + getName() + ChatColor.RESET;
        }
        return ChatColor.DARK_AQUA + getName() + ChatColor.RESET;
    }

    @Override
    public CommandSender getSender() {
        return sender;
    }

    @Override
    public boolean hasPermission(SinkCommand command) {
        return (command.getCommandOptions().isIrcOnly() && isOp()) || !command.getCommandOptions().isIrcOpOnly();
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return true;
    }

    @Override
    public boolean isOp() {
        return SinkIrcReflection.isIrcOp(getBase());
    }

    @Override
    public void setOp(boolean value) {
        throw new IllegalArgumentException("Invalid Operation: Cannot op IrcUser!");
    }

    @Override
    public String getPrimaryGroup() {
        return isOp() ? "OP" : "Default";
    }

    @Override
    public String getChatPrefix() {
        return isOp() ? ChatColor.DARK_RED.toString() : ChatColor.DARK_AQUA.toString();
    }

    @Override
    public void sendMessage(String msg) {
        SinkLibrary.getInstance().sendIrcMessage(getDisplayName() + ": " + msg, sender.getSource());
    }

    public void sendMessage(String msg, boolean privateMessage) {
        if (privateMessage) {
            SinkLibrary.getInstance().sendIrcMessage(msg, getName());
        } else {
            sendMessage(msg);
        }
    }

    public void sendMessage(String msg, String target) {
        SinkLibrary.getInstance().sendIrcMessage(getName() + ": " + msg, target);
    }

    @Override
    public void sendDebugMessage(String msg) {
        if (GeneralSettings.GENERAL_DEBUG.getValue()) {
            sendMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "Debug" + ChatColor.GRAY + "] "
                        + Debug.getCallerClassName() + "#" + Debug.getCallerMethodName() + ": " + ChatColor.RESET + msg, false);
        }
    }

    @Override
    public boolean isOnline() {
        return online; //??
    }

    public void setOnline(boolean value) {
        online = value;
    }

    @Override
    public User getBase() {
        return base;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IrcUser)) {
            return false;
        }

        return ((IrcUser) o).getName().equals(getName());
    }
}
