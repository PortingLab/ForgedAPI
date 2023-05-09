package org.portinglab.fabricapi;

import net.fabricmc.fabric.impl.client.event.lifecycle.ClientLifecycleEventsImpl;
import net.fabricmc.fabric.impl.event.lifecycle.LifecycleEventsImpl;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForgedAPI.MODID)
public class ForgedAPI {
    public static final String MODID = "forgedapi";

    public static final Logger LOGGER = LogManager.getLogger();

    public ForgedAPI() {
        IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_BUS.addListener(ClientLifecycleEventsImpl::onInitializeClient);
        MOD_BUS.addListener(LifecycleEventsImpl::onInitialize);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
