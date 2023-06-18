package org.portinglab.fabricapi;

import net.fabricmc.fabric.impl.client.rendering.RenderingCallbackInvoker;
import net.fabricmc.fabric.impl.event.lifecycle.ClientLifecycleEventsImpl;
import net.fabricmc.fabric.impl.event.lifecycle.LegacyEventInvokers;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.fabricmc.fabric.impl.event.lifecycle.client.LegacyClientEventInvokers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ForgedAPI.MODID)
public class ForgedAPI {
    public static final String MODID = "forgedapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ForgedAPI");

    public ForgedAPI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(RenderingCallbackInvoker::onInitializeClient);

        modEventBus.addListener(ClientLifecycleEventsImpl::onInitializeClient);
        modEventBus.addListener(LifecycleEventsImpl::onInitialize);

        modEventBus.addListener(LegacyEventInvokers::onInitialize);
        modEventBus.addListener(LegacyClientEventInvokers::onInitializeClient);

        modEventBus.addListener(this::onInitialize);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onInitialize(FMLCommonSetupEvent event) {

    }
}
