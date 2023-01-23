package dev.gegy.colored_lights.render;

import net.minecraft.client.Minecraft;

public interface ColoredLightReader {
    
    ColoredLightReader INSTANCE = (ColoredLightReader) Minecraft.getInstance().levelRenderer;
    
    void read(double x, double y, double z, ColorConsumer consumer);
}
