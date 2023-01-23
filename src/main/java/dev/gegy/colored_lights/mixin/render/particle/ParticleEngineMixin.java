package dev.gegy.colored_lights.mixin.render.particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.gegy.colored_lights.render.ColorConsumer;
import dev.gegy.colored_lights.render.ColoredLightReader;
import dev.gegy.colored_lights.render.particle.ColoredParticleVertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Shadow
    protected ClientLevel level;
    
    private final ColoredParticleVertexConsumer coloredParticleVertexConsumer = new ColoredParticleVertexConsumer();
    private final ColorConsumer coloredLightSetter = this.coloredParticleVertexConsumer::setLightColor;
    
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/particle/ParticleRenderType;begin(Lnet/minecraft/client/render/BufferBuilder;Lnet/minecraft/client/renderer/texture/TextureManager;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, require = 1)
    private void beforeRenderParticleSheet(PoseStack matrices, BufferSource immediate, LightTexture lightmap, Camera camera, float tickDelta, CallbackInfo ci, PoseStack modelView, ParticleRenderType type, Iterable<Particle> particles, Tesselator tessellator, BufferBuilder bufferBuilder) {
        
        float skyLight = this.level.getStarBrightness(tickDelta);
        this.coloredParticleVertexConsumer.setup(bufferBuilder, skyLight);
    }
    
    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;render(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V"), require = 1)
    private void renderParticle(Particle particle, VertexConsumer consumer, Camera camera, float tickDelta) {
        var box = particle.getBoundingBox();
        ColoredLightReader.INSTANCE.read(box.minX, box.minY, box.minZ, this.coloredLightSetter);
        
        // TODO: we can entirely disable when color is normal
        particle.render(this.coloredParticleVertexConsumer, camera, tickDelta);
    }
    
    @Inject(method = "render", at = @At("RETURN"), require = 1)
    private void afterRenderParticles(PoseStack matrices, BufferSource immediate, LightTexture lightmap, Camera camera, float tickDelta, CallbackInfo ci) {
        this.coloredParticleVertexConsumer.close();
    }
}
