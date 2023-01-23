package dev.gegy.colored_lights.mixin.render.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.Mth;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @Redirect(method = "putQuadData", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFF[IIZ)V"),
            require = 1)
    private void renderQuad(VertexConsumer consumer, PoseStack.Pose transform, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean useQuadColorData) {
        var ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            // we're very lazy and don't respect smooth lighting
            float factor = ctx.getLightColorFactor(lights[0]);
            red *= Mth.lerp(factor, 1.0F, ctx.red);
            green *= Mth.lerp(factor, 1.0F, ctx.green);
            blue *= Mth.lerp(factor, 1.0F, ctx.blue);
        }
        
        consumer.putBulkData(transform, quad, brightnesses, red, green, blue, lights, overlay, useQuadColorData);
    }
}
