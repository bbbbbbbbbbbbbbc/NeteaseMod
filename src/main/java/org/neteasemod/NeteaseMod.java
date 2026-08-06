package org.neteasemod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NeteaseMod.MOD_ID)
public class NeteaseMod {
    public static final String MOD_ID = "neteasemod";
    public static final Logger LOGGER = LoggerFactory.getLogger("MOD_DEBUG");

    public NeteaseMod(IEventBus bus) {
        bus.addListener(this::clientSetup);

        ClientEventHandler.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("FML客户端初始化完成，ControlledMode状态：enabled={}, server={}, port={}",
                    ControlledMode.ENABLED, ControlledMode.TARGET_SERVER, ControlledMode.TARGET_PORT);
        });
    }
}