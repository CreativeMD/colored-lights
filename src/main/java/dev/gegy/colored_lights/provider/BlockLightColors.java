package dev.gegy.colored_lights.provider;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockLightColors {
    public static final Vector3f WHITE = new Vector3f(1.0F, 1.0F, 1.0F);
    
    private static final List<BlockLightColorProvider> providers = new ArrayList<>();
    private static Lookup lookup = (world, pos, state) -> WHITE;
    
    public static void registerProvider(BlockLightColorProvider provider) {
        BlockLightColors.providers.add(provider);
        BlockLightColors.rebuildLookup();
    }
    
    private static void rebuildLookup() {
        var providers = BlockLightColors.providers.toArray(BlockLightColorProvider[]::new);
        BlockLightColors.lookup = Lookup.build(providers);
    }
    
    public static Vector3f lookup(LevelAccessor world, BlockPos pos, BlockState state) {
        return BlockLightColors.lookup.get(world, pos, state);
    }
    
    private interface Lookup {
        static Lookup build(BlockLightColorProvider... providers) {
            if (providers.length == 1) {
                return new SingleProviderLookup(providers[0]);
            } else {
                return new CompositeProviderLookup(providers);
            }
        }
        
        Vector3f get(LevelAccessor world, BlockPos pos, BlockState state);
    }
    
    private record SingleProviderLookup(BlockLightColorProvider provider) implements Lookup {
        @Override
        public Vector3f get(LevelAccessor world, BlockPos pos, BlockState state) {
            var color = this.provider.get(world, pos, state);
            return color != null ? color : WHITE;
        }
    }
    
    private record CompositeProviderLookup(BlockLightColorProvider[] providers) implements Lookup {
        @Override
        public Vector3f get(LevelAccessor world, BlockPos pos, BlockState state) {
            for (var provider : this.providers) {
                var color = provider.get(world, pos, state);
                if (color != null) {
                    return color;
                }
            }
            return WHITE;
        }
    }
}
