package dev.gegy.colored_lights.mixin.render.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {
    @Shadow
    protected abstract void render(PoseStack.Pose entry, VertexConsumer writer, int light, int overlay, float red, float green, float blue, float alpha);
    
    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
            require = 1)
    private void render(ModelPart part, PoseStack.Pose entry, VertexConsumer writer, int light, int overlay, float red, float green, float blue, float alpha) {
        var ctx = ColoredLightEntityRenderContext.get();
        if (ctx != null) {
            float factor = ctx.getLightColorFactor(light);
            red *= Mth.lerp(factor, 1.0F, ctx.red);
            green *= Mth.lerp(factor, 1.0F, ctx.green);
            blue *= Mth.lerp(factor, 1.0F, ctx.blue);
        }
        
        this.render(entry, writer, light, overlay, red, green, blue, alpha);
    }
}
