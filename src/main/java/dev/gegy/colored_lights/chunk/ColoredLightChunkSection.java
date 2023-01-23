package dev.gegy.colored_lights.chunk;

import dev.gegy.colored_lights.ColoredLightValue;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;

public interface ColoredLightChunkSection {
    ColoredLightValue getColoredLightPoint(LevelAccessor world, SectionPos sectionPos, int x, int y, int z);
    
    int getColoredLightGeneration();
}
