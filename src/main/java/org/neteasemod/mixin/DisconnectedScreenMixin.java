package org.neteasemod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.neteasemod.ControlledMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("neteasemod");

    @Unique
    private static Field renderablesReflect;

    static {
        try {
            renderablesReflect = Screen.class.getDeclaredField("renderables");
            renderablesReflect.setAccessible(true);
        } catch (NoSuchFieldException e) {
            renderablesReflect = null;

        }
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void afterInit(CallbackInfo ci) {
        if (!ControlledMode.ENABLED) {
            return;
        }
        if(renderablesReflect == null){

            return;
        }

        List<Button> buttonList = new ArrayList<>();
        try {
            Iterable<?> renderables = (Iterable<?>) renderablesReflect.get(this);
            for (Object obj : renderables) {
                if(obj instanceof Button btn){
                    buttonList.add(btn);
                }
            }
        } catch (Exception e) {

            return;
        }



            Button originBtn = buttonList.get(0);
            originBtn.visible = false;
            originBtn.active = false;



    }
}