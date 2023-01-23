package dev.gegy.colored_lights.mixin.chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.gegy.colored_lights.ColoredLightValue;
import dev.gegy.colored_lights.chunk.ChunkColoredLightSampler;
import dev.gegy.colored_lights.chunk.ColoredLightChunkSection;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements ColoredLightChunkSection {
    @Shadow
    public abstract boolean isEmpty();
    
    private ColoredLightValue[] coloredLightPoints;
    private int coloredLightGeneration;
    
    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getFluidState()Lnet/minecraft/fluid/FluidState;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void setBlockState(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> ci, BlockState lastState) {
        if (lastState.getLuminance() != 0 || state.getLuminance() != 0) {
            this.invalidateColoredLight();
        }
    }
    
    @Inject(method = "calculateCounts", at = @At("HEAD"))
    private void calculateCounts(CallbackInfo ci) {
        this.invalidateColoredLight();
    }
    
    private void invalidateColoredLight() {
        this.coloredLightPoints = null;
        this.coloredLightGeneration++;
    }
    
    @Override
    public ColoredLightValue getColoredLightPoint(WorldView world, ChunkSectionPos sectionPos, int x, int y, int z) {
        if (this.isEmpty()) {
            return ColoredLightValue.NO;
        }
        
        var points = this.coloredLightPoints;
        if (points == null) {
            this.coloredLightPoints = points = ChunkColoredLightSampler.sampleCorners(world, sectionPos, (ChunkSection) (Object) this);
        }
        
        return points[ChunkColoredLightSampler.octantIndex(x, y, z)];
    }
    
    @Override
    public int getColoredLightGeneration() {
        return this.coloredLightGeneration;
    }
}
