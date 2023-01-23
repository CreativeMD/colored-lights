package dev.gegy.colored_lights.mixin.render.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import dev.gegy.colored_lights.render.ColoredLightReader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("HEAD"), require = 1)
    private static <T extends BlockEntity> void beforeRender(BlockEntityRenderer<T> renderer, T entity, float tickDelta, PoseStack matrices, MultiBufferSource consumers, CallbackInfo ci) {
        var pos = entity.getBlockPos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        
        ColoredLightReader.INSTANCE.read(x, y, z, ColoredLightEntityRenderContext::set);
    }
    
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At("RETURN"), require = 1)
    private static <T extends BlockEntity> void afterRender(BlockEntityRenderer<T> renderer, T entity, float tickDelta, PoseStack matrices, MultiBufferSource consumers, CallbackInfo ci) {
        ColoredLightEntityRenderContext.end();
    }
}
