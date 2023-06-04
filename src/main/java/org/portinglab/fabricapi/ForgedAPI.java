package org.portinglab.fabricapi;

import link.infra.indium.Indium;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import net.fabricmc.fabric.impl.client.rendering.RenderingCallbackInvoker;
import net.fabricmc.fabric.impl.event.lifecycle.LegacyEventInvokers;
import net.fabricmc.fabric.impl.event.lifecycle.client.LegacyClientEventInvokers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod(ForgedAPI.MODID)
public class ForgedAPI {
    public static final String MODID = "forgedapi";

    public static final Logger LOGGER = LogManager.getLogger("ForgedAPI");

    public ForgedAPI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(RenderingCallbackInvoker::onInitializeClient);
        //modEventBus.addListener(Indigo::onInitializeClient);

        modEventBus.addListener(LegacyEventInvokers::onInitialize);
        modEventBus.addListener(LegacyClientEventInvokers::onInitializeClient);

        modEventBus.addListener(Indium::onInitializeClient);
        if (ModList.get().isLoaded("rubidium")) {
            modEventBus.register(Indium.class);
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
