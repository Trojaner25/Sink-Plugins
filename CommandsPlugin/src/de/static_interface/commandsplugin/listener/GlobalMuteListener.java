package de.static_interface.commandsplugin.listener;

import de.static_interface.commandsplugin.CommandsPlugin;
import de.static_interface.commandsplugin.commands.GlobalmuteCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GlobalmuteListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (CommandsPlugin.globalmuteEnabled && ! event.getPlayer().hasPermission("commandsplugin.globalmute.bypass"))
        {
            event.getPlayer().sendMessage(GlobalmuteCommand.prefix + "Du kannst nicht schreiben wenn der Globale Mute aktiviert ist.");
            event.setCancelled(true);
        }
    }
}
