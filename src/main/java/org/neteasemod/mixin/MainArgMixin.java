package org.neteasemod.mixin;

import org.neteasemod.ControlledMode;
import org.neteasemod.QuickPlayParseHelper;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainArgMixin {

    @Inject(at = @At("HEAD"), method = "main")
    private static void hookGameMain(String[] args, CallbackInfo ci) {
        QuickPlayParseHelper.parseLaunchArgs(args);

        if(QuickPlayParseHelper.hasServerToConnect()){


            ControlledMode.ENABLED = true;
            ControlledMode.TARGET_SERVER = QuickPlayParseHelper.SERVER_HOST;
            ControlledMode.TARGET_PORT = QuickPlayParseHelper.SERVER_PORT;
        }else{

            ControlledMode.ENABLED = false;
            ControlledMode.TARGET_SERVER = "";
            ControlledMode.TARGET_PORT = 25565;
        }
    }
}