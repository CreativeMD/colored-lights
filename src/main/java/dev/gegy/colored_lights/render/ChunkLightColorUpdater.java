package dev.gegy.colored_lights.render;

import org.jetbrains.annotations.Nullable;

import dev.gegy.colored_lights.ColoredLightCorner;
import dev.gegy.colored_lights.ColoredLightValue;
import dev.gegy.colored_lights.chunk.ColoredLightChunkSection;
import dev.gegy.colored_lights.mixin.render.chunk.BuiltChunkStorageAccess;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;

public final class ChunkLightColorUpdater {
    private final BlockPos.Mutable chunkAccessPos = new BlockPos.Mutable();
    
    public void rerenderChunk(WorldView world, BuiltChunkStorage chunks, int x, int y, int z) {
        var chunkAccess = (BuiltChunkStorageAccess) chunks;
        if (this.isChunkLightOutdated(world, chunkAccess, x, y, z)) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int cx = x + dx;
                        int cy = y + dy;
                        int cz = z + dz;
                        var chunk = this.getBuiltChunk(chunkAccess, cx, cy, cz);
                        if (chunk != null) {
                            this.updateChunk(world, chunk, cx, cy, cz);
                        }
                    }
                }
            }
        }
    }
    
    private boolean isChunkLightOutdated(WorldView world, BuiltChunkStorageAccess chunks, int x, int y, int z) {
        var chunk = world.getChunk(x, z);
        var section = getChunkSection(chunk, y);
        if (section == null) {
            return false;
        }
        
        var builtChunk = this.getBuiltChunk(chunks, x, y, z);
        if (builtChunk == null) {
            return false;
        }
        
        return ((ColoredLightBuiltChunk) builtChunk).isLightOutdated(section);
    }
    
    @Nullable
    private ChunkBuilder.BuiltChunk getBuiltChunk(BuiltChunkStorageAccess chunkAccess, int x, int y, int z) {
        var pos = this.chunkAccessPos;
        pos.set(x << 4, y << 4, z << 4);
        return chunkAccess.getBuiltChunk(pos);
    }
    
    public void updateChunk(WorldView world, ChunkBuilder.BuiltChunk builtChunk) {
        var origin = builtChunk.getOrigin();
        this.updateChunk(world, builtChunk, origin.getX() >> 4, origin.getY() >> 4, origin.getZ() >> 4);
    }
    
    private void updateChunk(WorldView world, ChunkBuilder.BuiltChunk builtChunk, int x, int y, int z) {
        var corners = new ColoredLightCorner[] { this.getLightColorAt(world, x, y, z), this.getLightColorAt(world, x, y, z + 1), this.getLightColorAt(world, x, y + 1, z), this
                .getLightColorAt(world, x, y + 1, z + 1), this.getLightColorAt(world, x + 1, y, z), this
                        .getLightColorAt(world, x + 1, y, z + 1), this.getLightColorAt(world, x + 1, y + 1, z), this.getLightColorAt(world, x + 1, y + 1, z + 1) };
        
        var section = getChunkSection(world, x, y, z);
        int generation = section != null ? section.getColoredLightGeneration() : Integer.MIN_VALUE;
        
        ((ColoredLightBuiltChunk) builtChunk).updateChunkLight(generation, isLightingColored(corners) ? corners : null);
    }
    
    private static boolean isLightingColored(ColoredLightCorner[] corners) {
        for (var corner : corners) {
            if (!corner.isDefault()) {
                return true;
            }
        }
        return false;
    }
    
    private ColoredLightCorner getLightColorAt(WorldView world, int cx, int cy, int cz) {
        var color = new ColoredLightValue();
        
        for (int dz = 0; dz <= 1; dz++) {
            for (int dx = 0; dx <= 1; dx++) {
                var chunk = world.getChunk(cx - dx, cz - dz);
                var chunkPos = chunk.getPos();
                for (int dy = 0; dy <= 1; dy++) {
                    int sy = cy - dy;
                    var section = getChunkSection(chunk, sy);
                    if (section != null) {
                        var sectionPos = ChunkSectionPos.from(chunkPos, sy);
                        color.add(section.getColoredLightPoint(world, sectionPos, dx, dy, dz));
                    }
                }
            }
        }
        
        return color.asCorner();
    }
    
    @Nullable
    private static ColoredLightChunkSection getChunkSection(WorldView world, int x, int y, int z) {
        var chunk = world.getChunk(x, z);
        return getChunkSection(chunk, y);
    }
    
    @Nullable
    private static ColoredLightChunkSection getChunkSection(Chunk chunk, int y) {
        var sections = chunk.getSectionArray();
        int index = chunk.sectionCoordToIndex(y);
        if (index >= 0 && index < sections.length) {
            return (ColoredLightChunkSection) sections[index];
        } else {
            return null;
        }
    }
}
