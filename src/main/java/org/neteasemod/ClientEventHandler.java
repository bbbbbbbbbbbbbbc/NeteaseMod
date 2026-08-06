package org.neteasemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

@SuppressWarnings("ConstantConditions")
public class ClientEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientEventHandler.class);
    private static boolean wasInMultiplayerSession = false;

    public static void init() {
        NeoForge.EVENT_BUS.register(new ClientEventHandler());

    }


    @SubscribeEvent
    public void onDisconnectedScreenInit(ScreenEvent.Init.Post event) {
        if (!ControlledMode.ENABLED) {
            return;
        }
        Screen screen = event.getScreen();
        if (!(screen instanceof DisconnectedScreen disconnectedScreen)) {
            return;
        }


        int btnX = screen.width / 2 - 100;
        int btnY = screen.height / 2 + 72;

        Button reconnectBtn = Button.builder(Component.literal("重新连接"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            btn.active = false;

            String host = ControlledMode.TARGET_SERVER;
            int port = ControlledMode.TARGET_PORT;

            if (host == null || host.isBlank() || port <= 0 || port > 65535) {
                btn.active = true;
                return;
            }

            if (mc.getConnection() != null) {

                btn.active = true;
                return;
            }

            try {
                ServerAddress address = ServerAddress.parseString(host + ":" + port);
                ServerData serverData = new ServerData("ReconnectServer", address.toString(), ServerData.Type.OTHER);

                ConnectScreen.startConnecting(disconnectedScreen, mc, address, serverData, false, null);
            } catch (Exception ex) {

                btn.active = true;
            }
        }).bounds(btnX, btnY, 200, 24).build();

        event.addListener(reconnectBtn);
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening event) {
        if (!ControlledMode.ENABLED) return;
        Screen newScreen = event.getNewScreen();
        if (newScreen instanceof JoinMultiplayerScreen) {


            Minecraft mc = Minecraft.getInstance();

            mc.stop();
            }

    }
    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!ControlledMode.ENABLED) return;
        Screen screen = event.getScreen();
        List<GuiEventListener> widgetList = new ArrayList<>(screen.children());


        if (screen instanceof OptionsScreen) {
            for (GuiEventListener guiEventListener : widgetList) {
                if (guiEventListener instanceof Button btn) {
                    Component msg = btn.getMessage();
                    if (msg.getContents() instanceof TranslatableContents tc) {
                        String key = tc.getKey();
                        if (key.equals("options.language")
                                || key.equals("options.telemetry")
                                || key.equals("fml.menu.mods")
                                || key.equals("neoforge.menu.mods.button")) {
                            btn.visible = false;
                            btn.active = false;

                        }
                    }
                }
            }
        }


        if (screen instanceof PauseScreen) {
            for (GuiEventListener guiEventListener : widgetList) {
                if (guiEventListener instanceof Button btn) {
                    Component msg = btn.getMessage();
                    if (msg.getContents() instanceof TranslatableContents tc) {
                        String key = tc.getKey();
                        if (key.equals("fml.menu.mods")) {
                            btn.visible = false;
                            btn.active = false;
                        }
                    }
                }
            }
        }
    }


}