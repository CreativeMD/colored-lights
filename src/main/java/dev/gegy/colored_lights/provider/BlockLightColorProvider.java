package dev.gegy.colored_lights.provider;

import org.jetbrains.annotations.Nullable;

import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockLightColorProvider {
    @Nullable
    Vector3f get(LevelAccessor world, BlockPos pos, BlockState state);
}
