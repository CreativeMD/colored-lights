package dev.gegy.colored_lights.resource.shader;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.shaders.Uniform;

public interface PatchedShader {
    @Nullable
    Uniform getPatchedUniform(PatchedUniform uniform);
}
