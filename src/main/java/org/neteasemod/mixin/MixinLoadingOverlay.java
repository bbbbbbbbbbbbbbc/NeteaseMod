//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.neteasemod.mixin;

import org.neteasemod.DepartWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LoadingOverlay.class})
public abstract class MixinLoadingOverlay {
    public MixinLoadingOverlay() {
    }

    @Inject(
            method = {"render"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void netease$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        DepartWrapper.renderWrapper((LoadingOverlay)(Object)this, guiGraphics, mouseX, mouseY, partialTicks);

        ci.cancel();
    }
}
