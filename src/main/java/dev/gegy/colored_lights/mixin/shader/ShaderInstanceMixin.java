package dev.gegy.colored_lights.mixin.shader;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.gegy.colored_lights.resource.shader.PatchedShader;
import dev.gegy.colored_lights.resource.shader.PatchedUniform;
import dev.gegy.colored_lights.resource.shader.ShaderPatchManager;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin implements PatchedShader {
    @Shadow
    @Final
    private List<Uniform> uniforms;
    
    private final Map<PatchedUniform, Uniform> patchedUniforms = new Reference2ObjectOpenHashMap<>();
    
    @Inject(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceFactory;getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;"),
            require = 1)
    private void initEarly(ResourceProvider factory, String name, VertexFormat format, CallbackInfo ci) {
        ShaderPatchManager.startPatching(name);
    }
    
    @Inject(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Shader;readBlendState(Lcom/google/gson/JsonObject;)Lnet/minecraft/client/gl/GlBlendState;"),
            require = 1)
    private void initUniforms(ResourceProvider factory, String name, VertexFormat format, CallbackInfo ci) {
        ShaderPatchManager.applyUniformPatches((Shader) this, (patchedUniform, glUniform) -> {
            this.uniforms.add(glUniform);
            this.patchedUniforms.put(patchedUniform, glUniform);
        });
    }
    
    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void initLate(ResourceProvider factory, String name, VertexFormat format, CallbackInfo ci) {
        ShaderPatchManager.stopPatching();
    }
    
    @Override
    public @Nullable Uniform getPatchedUniform(PatchedUniform uniform) {
        return this.patchedUniforms.get(uniform);
    }
}
