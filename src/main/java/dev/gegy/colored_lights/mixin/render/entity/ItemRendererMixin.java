package dev.gegy.colored_lights.mixin.render.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Redirect(method = "renderQuadList", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFII)V"),
            require = 1)
    private void renderQuadList(VertexConsumer consumer, PoseStack.Pose transform, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
        var ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            float factor = ctx.getLightColorFactor(light);
            red *= Mth.lerp(factor, 1.0F, ctx.red);
            green *= Mth.lerp(factor, 1.0F, ctx.green);
            blue *= Mth.lerp(factor, 1.0F, ctx.blue);
        }
        
        consumer.putBulkData(transform, quad, red, green, blue, light, overlay);
    }
}
