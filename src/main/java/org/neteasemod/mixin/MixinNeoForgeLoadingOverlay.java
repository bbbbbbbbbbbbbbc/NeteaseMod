package org.neteasemod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.neteasemod.DepartWrapper;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeoForgeLoadingOverlay.class)
public abstract class MixinNeoForgeLoadingOverlay extends LoadingOverlay {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private ReloadInstance reload;

    @Shadow
    @Final
    private DisplayWindow displayWindow;

    @Shadow
    @Final
    private ProgressMeter progressMeter;

    @Shadow
    private float currentProgress;

    private MixinNeoForgeLoadingOverlay() {
        super(null, null, null, false);
    }

    @Inject(
            method = {"render"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void netease$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // ====== 反射读取父类字段，不再使用@Shadow ======
        long fadeOutStart;
        Consumer<Optional<Throwable>> onFinish;
        try {
            var fadeField = LoadingOverlay.class.getDeclaredField("fadeOutStart");
            fadeField.setAccessible(true);
            fadeOutStart = (long) fadeField.get(this);

            var finishField = LoadingOverlay.class.getDeclaredField("onFinish");
            finishField.setAccessible(true);
            onFinish = (Consumer<Optional<Throwable>>) finishField.get(this);
        } catch (Exception e) {
            fadeOutStart = -1L;
            onFinish = opt -> {};
        }

        long millis = Util.getMillis();
        float fadeouttimer = fadeOutStart > -1L ? (float) (millis - fadeOutStart) / 1000.0F : -1.0F;
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + this.reload.getActualProgress() * 0.05F, 0.0F, 1.0F);
        this.progressMeter.setAbsolute(Mth.ceil(this.currentProgress * 1000.0F));
        this.displayWindow.renderToFramebuffer();

        int i = guiGraphics.guiWidth();
        int j = guiGraphics.guiHeight();
        float fade = 1.0F - Mth.clamp(fadeouttimer - 1.0F, 0.0F, 1.0F);

        if (fadeouttimer >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(guiGraphics, 0, 0, partialTick);
            }
            int l = Mth.ceil(fade * 255.0F);
            guiGraphics.nextStratum();

            int bgColor;
            try {
                var brandBgField = LoadingOverlay.class.getDeclaredField("BRAND_BACKGROUND");
                brandBgField.setAccessible(true);
                Object brandBgHolder = brandBgField.get(null);

                var getAsIntMethod = brandBgHolder.getClass().getDeclaredMethod("getAsInt");
                getAsIntMethod.setAccessible(true);
                int brandBgInt = (int) getAsIntMethod.invoke(brandBgHolder);

                var replaceAlphaMethod = LoadingOverlay.class.getDeclaredMethod("replaceAlpha", int.class, int.class);
                replaceAlphaMethod.setAccessible(true);
                bgColor = (int) replaceAlphaMethod.invoke(null, brandBgInt, l);
            } catch (Exception e) {
                bgColor = 0xFF000000;
            }
            guiGraphics.fill(0,0,i,j,bgColor);
        } else {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.minecraft.getMainRenderTarget().getColorTexture(), ARGB.color(255, 255, 255, 255));
            DepartWrapper.drawMojang(guiGraphics);
            DepartWrapper.drawUrl(guiGraphics);
        }

        double d0 = Math.min((double) guiGraphics.guiWidth() * (double) 0.75F, (double) guiGraphics.guiHeight()) * (double) 0.25F;
        double d1 = d0 * (double) 4.0F;
        int k1 = (int) (d1 * (double) 0.5F);
        int barY = (int) ((double) guiGraphics.guiHeight() * 0.8325);
        if (fadeouttimer < 1.0F) {
            DepartWrapper.drawProgressBar(guiGraphics, this.currentProgress, i / 2 - k1, barY - 5, i / 2 + k1, barY + 5, 1.0F - Mth.clamp(fadeouttimer, 0.0F, 1.0F));
        }

        if (fadeouttimer >= 2.0F) {
            this.progressMeter.complete();
            Minecraft.getInstance().schedule(() -> {
                Minecraft.getInstance().getTextureManager().release(NeoForgeLoadingOverlay.LOADING_OVERLAY_TEXTURE_ID);
                this.displayWindow.close();
            });
            this.minecraft.setOverlay((Overlay) null);
        }

        if (fadeOutStart == -1L && this.reload.isDone()) {
            // 反射写入fadeOutStart到当前实例
            try {
                var fadeField = LoadingOverlay.class.getDeclaredField("fadeOutStart");
                fadeField.setAccessible(true);
                fadeField.set(this, millis);
            } catch (Exception ignored) {}

            try {
                this.reload.checkExceptions();
                onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                onFinish.accept(Optional.of(throwable));
            }
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        }
        ci.cancel();
    }
}
