package dev.gegy.colored_lights.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.LightTexture;

public final class ColoredLightEntityRenderContext {
    private static final ColoredLightEntityRenderContext INSTANCE = new ColoredLightEntityRenderContext();
    
    private boolean active;
    
    public float red;
    public float green;
    public float blue;
    public float skyBrightness = 1.0F;
    
    @Nullable
    public static ColoredLightEntityRenderContext get() {
        var context = INSTANCE;
        return context.active ? context : null;
    }
    
    public float getLightColorFactor(int light) {
        float blockLight = LightTexture.block(light) / 15.0F;
        float skyLight = LightTexture.sky(light) / 15.0F;
        skyLight *= this.skyBrightness;
        
        return blockLight * (1.0F - skyLight);
    }
    
    public static void setGlobal(float skyBrightness) {
        INSTANCE.skyBrightness = skyBrightness;
    }
    
    public static void set(float red, float green, float blue) {
        var context = INSTANCE;
        context.red = red;
        context.green = green;
        context.blue = blue;
        context.active = true;
    }
    
    public static void end() {
        INSTANCE.active = false;
    }
}
