package org.neteasemod;

import com.mojang.blaze3d.systems.RenderSystem;
import org.neteasemod.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class DepartWrapper {
    // 删除未使用的 LOGGER，消除黄色警告
    private static final ResourceLocation mojangResource = ResourceLocation.parse("depart:textures/gui/mojang_new.png");
    private static final ResourceLocation urlResource = ResourceLocation.parse("depart:textures/gui/url.png");
    private static final Map<String, Component> SPECIAL_PACK_NAMES = Map.of(
            "programmer_art", Component.translatable("经典"),
            "high_contrast", Component.translatable("高对比度")
    );
    private static final DecimalFormat decimalFormat = new DecimalFormat("########0.00");

    public DepartWrapper() {
    }

    public static Component getPackTitleWrapper(ClientPackSource source, String text) {
        Component $$1 = SPECIAL_PACK_NAMES.get(text);
        return ($$1 != null ? $$1 : Component.literal(text));
    }

    public static void drawMojang(GuiGraphics guiGraphics) {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int mojangPicWidth = (int) (516.0F / guiScale);
        int mojangPicHeight = (int) (152.0F / guiScale);
        int x = (width - mojangPicWidth) / 2;
        int y = (height - mojangPicHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, mojangResource, x, y, 0.0F, 0.0F, mojangPicWidth, mojangPicHeight, mojangPicWidth, mojangPicHeight);
    }

    public static void drawUrl(GuiGraphics guiGraphics) {
        int urlPicWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int urlPicHeight = 58 * urlPicWidth / 1920;
        int x = 0;
        int y = Minecraft.getInstance().getWindow().getGuiScaledHeight() - urlPicHeight;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, urlResource, x, y, 0.0F, 0.0F, urlPicWidth, urlPicHeight, urlPicWidth, urlPicHeight);
    }

    public static void drawProgressBar(GuiGraphics guiGraphics, float currentProgress, int minX, int minY, int maxX, int maxY, float alpha) {
        int i = Mth.ceil((float) (maxX - minX - 2) * currentProgress);
        int j = Math.round(alpha * 255.0F);
        int k = ARGB.color(j, 255, 255, 255);
        guiGraphics.fill(minX + 2, minY + 2, minX + i, maxY - 2, k);
        guiGraphics.fill(minX + 1, minY, maxX - 1, minY + 1, k);
        guiGraphics.fill(minX + 1, maxY, maxX - 1, maxY - 1, k);
        guiGraphics.fill(minX, minY, minX + 1, maxY, k);
        guiGraphics.fill(maxX, minY, maxX - 1, maxY, k);
    }

    public static void renderWrapper(LoadingOverlay loadingOverlay, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        int i = guiGraphics.guiWidth();
        int j = guiGraphics.guiHeight();
        long k = Util.getMillis();

        // =====反射读取/写入 LoadingOverlay 私有字段=====
        boolean fadeIn;
        long fadeInStart;
        long fadeOutStart;
        Minecraft overlayMc;
        Object reloadObj;
        float currentProgress;
        // 修复：onFinish 是 Consumer<Optional<Throwable>>，不是 Optional
        Consumer<Optional<Throwable>> onFinish;
        int brandBg;

        try {
            fadeIn = (boolean) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "fadeIn");
            fadeInStart = (long) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "fadeInStart");
            fadeOutStart = (long) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "fadeOutStart");
            overlayMc = (Minecraft) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "minecraft");
            reloadObj = ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "reload");
            currentProgress = (float) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "currentProgress");
            // ✅ 修正类型 Consumer
            onFinish = (Consumer<Optional<Throwable>>) ReflectionHelper.getField(LoadingOverlay.class, loadingOverlay, "onFinish");
            brandBg = (int) ReflectionHelper.getField(LoadingOverlay.class, null, "BRAND_BACKGROUND");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (fadeIn && fadeInStart == -1L) {
            try {
                ReflectionHelper.setField(LoadingOverlay.class, loadingOverlay, "fadeInStart", k);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            fadeInStart = k;
        }

        float f = fadeOutStart > -1L ? (float) (k - fadeOutStart) / 1000.0F : -1.0F;
        float f1 = fadeInStart > -1L ? (float) (k - fadeInStart) / 500.0F : -1.0F;

        if (f >= 1.0F) {
            if (overlayMc.screen != null) {
                overlayMc.screen.render(guiGraphics, 0, 0, partialTicks);
            }
            int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            guiGraphics.nextStratum();
            int finalColor;
            try {
                finalColor = (int) ReflectionHelper.invokeMethod(LoadingOverlay.class, null, "replaceAlpha", brandBg, l);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            guiGraphics.fill(0, 0, i, j, finalColor);
        } else if (fadeIn) {
            if (overlayMc.screen != null && f1 < 1.0F) {
                overlayMc.screen.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
            int l1 = Mth.ceil(Mth.clamp(f1, 0.15D, 1.0D) * 255.0D);
            guiGraphics.nextStratum();
            int finalColor;
            try {
                finalColor = (int) ReflectionHelper.invokeMethod(LoadingOverlay.class, null, "replaceAlpha", brandBg, l1);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            guiGraphics.fill(0, 0, i, j, finalColor);
        } else {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(overlayMc.getMainRenderTarget().getColorTexture(), ARGB.color(255, 255, 255, 255));
            drawMojang(guiGraphics);
            drawUrl(guiGraphics);
        }

        double d1 = Math.min((double) guiGraphics.guiWidth() * 0.75F, guiGraphics.guiHeight()) * 0.25F;
        double d0 = d1 * 4.0F;
        int j1 = (int) (d0 * 0.5F);
        int k1 = (int) (guiGraphics.guiHeight() * 0.8325);

        // 获取reload的实际进度
        float f6;
        try {
            f6 = (float) ReflectionHelper.invokeMethod(reloadObj.getClass(), reloadObj, "getActualProgress");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        currentProgress = Mth.clamp(currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);
        try {
            ReflectionHelper.setField(LoadingOverlay.class, loadingOverlay, "currentProgress", currentProgress);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (f < 1.0F) {
            drawProgressBar(guiGraphics, currentProgress, i / 2 - j1, k1 - 5, i / 2 + j1, k1 + 5, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F) {
            overlayMc.setOverlay((Overlay) null);
        }

        if (fadeOutStart == -1L) {
            boolean reloadDone;
            try {
                reloadDone = (boolean) ReflectionHelper.invokeMethod(reloadObj.getClass(), reloadObj, "isDone");
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (reloadDone && (!fadeIn || f1 >= 2.0F)) {
                try {
                    ReflectionHelper.setField(LoadingOverlay.class, loadingOverlay, "fadeOutStart", Util.getMillis());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                try {
                    ReflectionHelper.invokeMethod(reloadObj.getClass(), reloadObj, "checkExceptions");
                    // ✅ Consumer 调用 accept，现在类型匹配
                    onFinish.accept(Optional.empty());
                } catch (Throwable var23) {
                    onFinish.accept(Optional.of(var23));
                }
                if (overlayMc.screen != null) {
                    overlayMc.screen.init(overlayMc, guiGraphics.guiWidth(), guiGraphics.guiHeight());
                }
            }
        }
    }

    public static String timeWrapper(int number) {
        double d0 = number / 20.0F;
        double d1 = d0 / 60.0F;
        double d2 = d1 / 60.0F;
        double d3 = d2 / 24.0F;
        double d4 = d3 / 365.0F;
        if (d4 > 0.5D) {
            return decimalFormat.format(d4) + "年";
        } else if (d3 > 0.5D) {
            return decimalFormat.format(d3) + "天";
        } else if (d2 > 0.5D) {
            return decimalFormat.format(d2) + "小时";
        } else {
            return d1 > 0.5D ? decimalFormat.format(d1) + "分" : d0 + "秒";
        }
    }

    public static String distanceWrapper(int number) {
        double d0 = number / 100.0F;
        double d1 = d0 / 1000.0F;
        if (d1 > 0.5D) {
            return decimalFormat.format(d1) + "千米";
        } else {
            return d0 > 0.5D ? decimalFormat.format(d0) + "米" : number + "厘米";
        }
    }

    public static String getNameWrapper() {
        return "组件资源包";
    }
}
