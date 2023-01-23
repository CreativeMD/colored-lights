package dev.gegy.colored_lights.resource.shader;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;

import dev.gegy.colored_lights.resource.ResourcePatchManager;
import net.minecraft.resources.ResourceLocation;

public final class ShaderPatchManager {
    public static final ShaderPatchManager INSTANCE = new ShaderPatchManager();
    
    private final Multimap<String, ShaderPatch> patches = HashMultimap.create();
    
    private final ThreadLocal<Collection<ShaderPatch>> activePatches = new ThreadLocal<>();
    
    private ShaderPatchManager() {}
    
    public void add(String shader, ShaderPatch patch) {
        this.patches.put(shader, patch);
        
        this.addResourcePatch(shader, patch, Program.Type.VERTEX);
        this.addResourcePatch(shader, patch, Program.Type.FRAGMENT);
    }
    
    private void addResourcePatch(String shader, ShaderPatch patch, Program.Type type) {
        var location = new ResourceLocation("shaders/core/" + shader + type.getExtension());
        
        ResourcePatchManager.INSTANCE.add(location, bytes -> {
            String source = new String(bytes, StandardCharsets.UTF_8);
            source = patch.applyToSource(source, type);
            
            return source.getBytes(StandardCharsets.UTF_8);
        });
    }
    
    public static void startPatching(String shader) {
        INSTANCE.activePatches.set(INSTANCE.patches.get(shader));
    }
    
    public static void stopPatching() {
        INSTANCE.activePatches.remove();
    }
    
    public static void applyUniformPatches(Shader shader, BiConsumer<PatchedUniform, Uniform> consumer) {
        var activePatches = getActivePatches();
        if (activePatches != null) {
            for (ShaderPatch patch : activePatches) {
                patch.addUniforms(shader, consumer);
            }
        }
    }
    
    @Nullable
    private static Collection<ShaderPatch> getActivePatches() {
        return INSTANCE.activePatches.get();
    }
}
