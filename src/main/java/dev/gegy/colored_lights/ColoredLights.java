package dev.gegy.colored_lights;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.gegy.colored_lights.provider.BlockLightColorLoader;
import dev.gegy.colored_lights.provider.BlockLightColorMap;
import dev.gegy.colored_lights.provider.BlockLightColors;
import dev.gegy.colored_lights.resource.shader.PatchedUniform;
import dev.gegy.colored_lights.resource.shader.ShaderPatch;
import dev.gegy.colored_lights.resource.shader.ShaderPatchManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public final class ColoredLights implements ModInitializer {
    public static final String ID = "colored_lights";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    
    public static final PatchedUniform CHUNK_LIGHT_COLORS = PatchedUniform.ofInt2("ChunkLightColors", 0, 0);
    
    @Override
    public void onInitialize() {
        var resources = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);
        
        var colors = new BlockLightColorMap();
        BlockLightColors.registerProvider(colors);
        
        resources.registerReloadListener(new BlockLightColorLoader(colors::set));
        
        // @formatter:off
        var chunkPatch = ShaderPatch.builder().vertex().addUniform(CHUNK_LIGHT_COLORS).declare("#moj_import <colored_chunk_light.glsl>")
                .wrapCall("minecraft_sample_lightmap", "apply_color_to_light", CHUNK_LIGHT_COLORS.getName(), "UV2", "Position", "Sampler2").end().build();
        // @formatter:on
        
        var applyChunkColorTo = new String[] { "rendertype_solid", "rendertype_cutout", "rendertype_cutout_mipped", "rendertype_translucent", "rendertype_tripwire" };
        
        for (var shader : applyChunkColorTo) {
            ShaderPatchManager.INSTANCE.add(shader, chunkPatch);
        }
    }
}
