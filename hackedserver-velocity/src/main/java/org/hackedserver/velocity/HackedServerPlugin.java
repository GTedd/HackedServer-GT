package org.hackedserver.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;

import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;

import org.hackedserver.core.config.ConfigsManager;
import org.hackedserver.velocity.commands.HackedCommands;
import org.hackedserver.velocity.listeners.CustomPayloadListener;
import org.hackedserver.velocity.listeners.HackedPlayerListeners;
import org.hackedserver.velocity.logs.Logs;

import javax.inject.Inject;

import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;

public class HackedServerPlugin {

    private final ProxyServer server;
    private final HackedCommands commands;
    private final File folder;

    @Inject
    public HackedServerPlugin(ProxyServer server, Logger logger, final PluginContainer pluginContainer,
            @DataDirectory Path dataDirectory) {
        this.server = server;
        this.folder = dataDirectory.toFile();
        Logs.onEnable(logger, server);
        ConfigsManager.init(folder);
        commands = new HackedCommands(logger, folder, server.getCommandManager(), server);
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new HackedPlayerListeners());
        PacketEvents.getAPI().getEventManager().registerListener(
                new CustomPayloadListener(server), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
        commands.create();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        // Unregister all event listeners for this plugin
        server.getEventManager().unregisterListeners(this);

        // Terminate and reinit PacketEvents
        PacketEvents.getAPI().terminate();
        PacketEvents.getAPI().init();

        // Reload configs
        ConfigsManager.reload(folder);

        // Recreate commands
        commands.create();

        // Re-register event listeners
        server.getEventManager().register(this, new HackedPlayerListeners());
        PacketEvents.getAPI().getEventManager().registerListener(
                new CustomPayloadListener(server), PacketListenerPriority.NORMAL);
    }

}
