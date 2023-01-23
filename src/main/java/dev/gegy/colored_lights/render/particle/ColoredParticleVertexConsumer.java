package dev.gegy.colored_lights.render.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.util.Mth;

public final class ColoredParticleVertexConsumer implements VertexConsumer, AutoCloseable {
    private VertexConsumer parent;
    private float redLight;
    private float greenLight;
    private float blueLight;
    private float skyLight;
    
    private int red;
    private int green;
    private int blue;
    private int alpha;
    private boolean hasColor;
    
    public void setup(VertexConsumer parent, float skyLight) {
        this.parent = parent;
        this.skyLight = skyLight;
    }
    
    public void setLightColor(float redLight, float greenLight, float blueLight) {
        this.redLight = redLight;
        this.greenLight = greenLight;
        this.blueLight = blueLight;
    }
    
    @Override
    public void close() {
        this.parent = null;
    }
    
    private void flushColor() {
        if (this.hasColor) {
            this.parent.color(this.red, this.green, this.blue, this.alpha);
            this.hasColor = false;
        }
    }
    
    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.flushColor();
        
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.hasColor = true;
        
        return this;
    }
    
    @Override
    public VertexConsumer uv2(int block, int sky) {
        if (block > 0 && sky < 255 && this.skyLight < 1.0F) {
            float blockFactor = block / 255.0F;
            float skyFactor = (sky / 255.0F) * this.skyLight;
            float lightFactor = blockFactor * (1.0F - skyFactor);
            if (lightFactor > 1e-3f) {
                this.red *= Mth.lerp(lightFactor, 1.0F, this.redLight);
                this.green *= Mth.lerp(lightFactor, 1.0F, this.greenLight);
                this.blue *= Mth.lerp(lightFactor, 1.0F, this.blueLight);
            }
        }
        
        this.flushColor();
        this.parent.uv2(block, sky);
        
        return this;
    }
    
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.flushColor();
        this.parent.vertex(x, y, z);
        return this;
    }
    
    @Override
    public VertexConsumer uv(float u, float v) {
        this.flushColor();
        this.parent.uv(u, v);
        return this;
    }
    
    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        this.flushColor();
        this.parent.overlayCoords(u, v);
        return this;
    }
    
    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.flushColor();
        this.parent.normal(x, y, z);
        return this;
    }
    
    @Override
    public void endVertex() {
        this.flushColor();
        this.parent.endVertex();
    }
    
    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        this.parent.defaultColor(red, green, blue, alpha);
    }
    
    @Override
    public void unsetDefaultColor() {
        this.parent.unsetDefaultColor();
    }
}
