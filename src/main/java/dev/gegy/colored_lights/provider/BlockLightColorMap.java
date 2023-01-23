package dev.gegy.colored_lights.provider;

import org.jetbrains.annotations.Nullable;

import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockLightColorMap implements BlockLightColorProvider {
    private final Reference2ObjectOpenHashMap<Block, Vector3f> blockToColor = new Reference2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<BlockState, Vector3f> stateToColor = new Reference2ObjectOpenHashMap<>();
    
    public void clear() {
        this.blockToColor.clear();
        this.stateToColor.clear();
    }
    
    public void set(BlockLightColorMap map) {
        this.clear();
        this.putAll(map);
    }
    
    public void put(Block block, Vector3f color) {
        this.blockToColor.put(block, color);
    }
    
    public void put(BlockState state, Vector3f color) {
        this.stateToColor.put(state, color);
    }
    
    public void putAll(BlockLightColorMap colors) {
        this.blockToColor.putAll(colors.blockToColor);
        this.stateToColor.putAll(colors.stateToColor);
    }
    
    @Override
    @Nullable
    public Vector3f get(LevelAccessor world, BlockPos pos, BlockState state) {
        var stateColor = this.stateToColor.get(state);
        if (stateColor != null) {
            return stateColor;
        }
        
        return this.blockToColor.get(state.getBlock());
    }
}
