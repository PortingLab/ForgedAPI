package org.portinglab.fabricapi;

import net.fabricmc.fabric.impl.event.lifecycle.LegacyEventInvokers;
import net.fabricmc.fabric.impl.event.lifecycle.client.LegacyClientEventInvokers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForgedAPI.MODID)
public class ForgedAPI {
    public static final String MODID = "forgedapi";
    private static final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    public static final Logger LOGGER = LogManager.getLogger("FrogeAPI");

    public ForgedAPI() {
        bus.addListener(LegacyEventInvokers::onInitialize);
        bus.addListener(LegacyClientEventInvokers::onInitializeClient);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
