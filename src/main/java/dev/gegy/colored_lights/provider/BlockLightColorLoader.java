package dev.gegy.colored_lights.provider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.util.JsonUtils;

import dev.gegy.colored_lights.ColoredLights;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public final class BlockLightColorLoader implements SimpleResourceReloadListener<BlockLightColorMap> {
    private final Consumer<BlockLightColorMap> colorConsumer;
    
    public BlockLightColorLoader(Consumer<BlockLightColorMap> colorConsumer) {
        this.colorConsumer = colorConsumer;
    }
    
    @Override
    public CompletableFuture<BlockLightColorMap> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadColors(manager);
            } catch (IOException e) {
                ColoredLights.LOGGER.error("Failed to load colored light mappings", e);
                return new BlockLightColorMap();
            }
        }, executor);
    }
    
    @Override
    public CompletableFuture<Void> apply(BlockLightColorMap colors, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        this.colorConsumer.accept(colors);
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(ColoredLights.ID, "light_colors");
    }
    
    private static BlockLightColorMap loadColors(ResourceManager manager) throws IOException {
        var baseColors = new BlockLightColorMap();
        var overrideColors = new BlockLightColorMap();
        
        for (var resource : manager.getResources(new ResourceLocation(ColoredLights.ID, "light_colors.json"))) {
            try (var input = resource.getInputStream()) {
                var root = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
                
                boolean replace = JsonUtils.getBooleanOr("replace", root, false);
                JsonObject mappings = root.getAsJsonObject("colors");
                
                if (replace) {
                    baseColors = new BlockLightColorMap();
                    parseColorMappings(mappings, baseColors);
                } else {
                    parseColorMappings(mappings, overrideColors);
                }
            } catch (JsonSyntaxException e) {
                ColoredLights.LOGGER.error("Failed to parse colored light mappings at {}", resource.getSourceName(), e);
            }
        }
        
        baseColors.putAll(overrideColors);
        
        return baseColors;
    }
    
    private static void parseColorMappings(JsonObject mappings, BlockLightColorMap colors) throws JsonSyntaxException {
        for (var entry : mappings.entrySet()) {
            var color = parseColor(entry.getValue().getAsString());
            var result = BlockReferenceParser.parse(entry.getKey());
            if (result != null) {
                result.ifLeft(block -> colors.put(block, color));
                result.ifRight(state -> colors.put(state, color));
            }
        }
    }
    
    private static Vector3f parseColor(String string) {
        if (!string.startsWith("#")) {
            throw new JsonSyntaxException("Invalid color! Expected hex string in format #ffffff");
        }
        
        try {
            int color = Integer.parseInt(string.substring(1), 16);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            return new Vector3f(red / 255.0F, green / 255.0F, blue / 255.0F);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Malformed hex string", e);
        }
    }
}
