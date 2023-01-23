package dev.gegy.colored_lights.mixin.render.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;

@Mixin(ViewArea.class)
public interface ViewAreaAccess {
    @Invoker("getRenderChunkAt")
    RenderChunk getBuiltChunk(BlockPos pos);
}
