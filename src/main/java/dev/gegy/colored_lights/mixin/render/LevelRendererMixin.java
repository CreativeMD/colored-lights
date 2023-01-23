package dev.gegy.colored_lights.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import dev.gegy.colored_lights.ColoredLightCorner;
import dev.gegy.colored_lights.ColoredLights;
import dev.gegy.colored_lights.mixin.render.chunk.ViewAreaAccess;
import dev.gegy.colored_lights.render.ChunkLightColorUpdater;
import dev.gegy.colored_lights.render.ColorConsumer;
import dev.gegy.colored_lights.render.ColoredLightBuiltChunk;
import dev.gegy.colored_lights.render.ColoredLightEntityRenderContext;
import dev.gegy.colored_lights.render.ColoredLightLevelRenderer;
import dev.gegy.colored_lights.render.ColoredLightReader;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin implements ColoredLightLevelRenderer, ColoredLightReader {
    @Shadow
    private ClientLevel world;
    @Shadow
    private ViewArea viewArea;
    
    private final ChunkLightColorUpdater chunkLightColorUpdater = new ChunkLightColorUpdater();
    
    private final MutableBlockPos readBlockPos = new MutableBlockPos();
    private Uniform chunkLightColors;
    
    private long lastChunkLightColors;
    
    @Inject(method = "scheduleChunkRender", at = @At("HEAD"), require = 1)
    private void scheduleChunkRender(int x, int y, int z, boolean important, CallbackInfo ci) {
        this.chunkLightColorUpdater.rerenderChunk(this.world, this.viewArea, x, y, z);
    }
    
    @Inject(method = "renderChunkLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;setupRenderState()V", shift = At.Shift.AFTER), require = 1)
    private void prepareRenderLayer(RenderLayer layer, PoseStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci) {
        var shader = RenderSystem.getShader();
        this.chunkLightColors = ColoredLights.CHUNK_LIGHT_COLORS.get(shader);
        this.lastChunkLightColors = 0;
    }
    
    @Redirect(method = "renderChunkLayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getOrigin()Lnet/minecraft/core/BlockPos;"), require = 1)
    private BlockPos prepareRenderChunk(RenderChunk chunk) {
        var chunkLightColors = this.chunkLightColors;
        if (chunkLightColors != null) {
            long colors = ((ColoredLightBuiltChunk) chunk).getPackedChunkLightColors();
            if (this.lastChunkLightColors != colors) {
                this.lastChunkLightColors = colors;
                
                int colorsHigh = (int) (colors >>> 32);
                int colorsLow = (int) colors;
                chunkLightColors.set(colorsHigh, colorsLow);
                chunkLightColors.upload();
            }
        }
        return chunk.getOrigin();
    }
    
    @Inject(method = "renderChunkLayer", at = @At("RETURN"), require = 1)
    private void finishRenderLayer(RenderLayer layer, PoseStack transform, double cameraX, double cameraY, double cameraZ, Matrix4f projection, CallbackInfo ci) {
        this.lastChunkLightColors = 0;
        
        var chunkLightColors = this.chunkLightColors;
        if (chunkLightColors != null) {
            chunkLightColors.set(0, 0);
        }
    }
    
    @Inject(method = "render", at = @At("INVOKE"), require = 1)
    private void beforeRender(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        float skyBrightness = this.world.getStarBrightness(tickDelta);
        ColoredLightEntityRenderContext.setGlobal(skyBrightness);
    }
    
    @Inject(method = "renderEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, require = 1)
    private void beforeRenderEntity(Entity entity, double x, double y, double z, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci, double entityX, double entityY, double entityZ, float entityYaw) {
        this.read(entityX, entityY, entityZ, ColoredLightEntityRenderContext::set);
    }
    
    @Inject(method = "renderEntity", at = @At("RETURN"), require = 1)
    private void afterRenderEntity(Entity entity, double x, double y, double z, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        ColoredLightEntityRenderContext.end();
    }
    
    @Override
    public void read(double x, double y, double z, ColorConsumer consumer) {
        var readBlockPos = this.readBlockPos.set(x, y, z);
        var chunk = ((ViewAreaAccess) this.viewArea).getBuiltChunk(readBlockPos);
        if (chunk == null) {
            return;
        }
        
        var corners = ((ColoredLightBuiltChunk) chunk).getChunkLightColors();
        if (corners != null) {
            BlockPos origin = chunk.getOrigin();
            float localX = (float) (x - origin.getX()) / 16.0F;
            float localY = (float) (y - origin.getY()) / 16.0F;
            float localZ = (float) (z - origin.getZ()) / 16.0F;
            ColoredLightCorner.mix(corners, localX, localY, localZ, consumer);
        }
    }
    
    @Override
    public ChunkLightColorUpdater getChunkLightColorUpdater() {
        return this.chunkLightColorUpdater;
    }
}
