package dev.gegy.colored_lights.mixin.render.chunk;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.gegy.colored_lights.ColoredLightCorner;
import dev.gegy.colored_lights.ColoredLightPacking;
import dev.gegy.colored_lights.render.ColoredLightBuiltChunk;
import dev.gegy.colored_lights.render.ColoredLightLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;

@Mixin(RenderChunk.class)
public class RenderChunkMixin implements ColoredLightBuiltChunk {
    private int chunkLightGeneration = -1;
    private ColoredLightCorner[] chunkLightColors;
    private long packedChunkLightColors = 0;
    
    @Inject(method = "clear", at = @At("HEAD"), require = 1)
    private void clear(CallbackInfo ci) {
        this.updateChunkLight(-1, null);
    }
    
    @Inject(method = "cancelRebuild", at = @At("HEAD"), require = 1)
    private void cancelRebuild(CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var worldRenderer = (ColoredLightLevelRenderer) client.levelRenderer;
        var colorUpdater = worldRenderer.getChunkLightColorUpdater();
        
        colorUpdater.updateChunk(client.level, (RenderChunk) (Object) this);
    }
    
    @Override
    public void updateChunkLight(int generation, ColoredLightCorner[] corners) {
        this.chunkLightGeneration = generation;
        this.chunkLightColors = corners;
        
        if (corners != null) {
            this.packedChunkLightColors = ColoredLightPacking.pack(corners);
        } else {
            this.packedChunkLightColors = ColoredLightPacking.DEFAULT;
        }
    }
    
    @Nullable
    @Override
    public ColoredLightCorner[] getChunkLightColors() {
        return this.chunkLightColors;
    }
    
    @Override
    public long getPackedChunkLightColors() {
        return this.packedChunkLightColors;
    }
    
    @Override
    public int getChunkLightGeneration() {
        return this.chunkLightGeneration;
    }
}
