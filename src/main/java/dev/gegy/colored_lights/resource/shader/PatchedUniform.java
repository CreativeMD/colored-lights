package dev.gegy.colored_lights.resource.shader;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;

public final class PatchedUniform {
    final String name;
    final Type type;
    final Consumer<Uniform> reset;
    
    PatchedUniform(String name, Type type, Consumer<Uniform> reset) {
        this.name = name;
        this.type = type;
        this.reset = reset;
    }
    
    public static PatchedUniform ofInt(String name, int value) {
        return new PatchedUniform(name, Type.INT, u -> u.set(value));
    }
    
    public static PatchedUniform ofFloat(String name, float value) {
        return new PatchedUniform(name, Type.FLOAT, u -> u.set(value));
    }
    
    public static PatchedUniform ofInt2(String name, int x, int y) {
        return new PatchedUniform(name, Type.INT2, u -> u.set(x, y));
    }
    
    public String getName() {
        return this.name;
    }
    
    @Nullable
    public Uniform get(Shader shader) {
        if (shader instanceof PatchedShader patchedShader) {
            return patchedShader.getPatchedUniform(this);
        }
        return null;
    }
    
    public Uniform toGlUniform(Shader shader) {
        var uniform = new Uniform(this.name, this.type.glType, this.type.count, shader);
        this.reset.accept(uniform);
        return uniform;
    }
    
    public enum Type {
        INT(Uniform.UT_INT1, "int", 1),
        FLOAT(Uniform.UT_FLOAT1, "float", 1),
        INT2(Uniform.UT_INT2, "ivec2", 2);
        
        public final int glType;
        public final String glslType;
        public final int count;
        
        Type(int glType, String glslType, int count) {
            this.glType = glType;
            this.glslType = glslType;
            this.count = count;
        }
    }
}
