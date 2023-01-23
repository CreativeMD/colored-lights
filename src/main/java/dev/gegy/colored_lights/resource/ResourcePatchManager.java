package dev.gegy.colored_lights.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.resources.ResourceLocation;

public final class ResourcePatchManager {
    public static final ResourcePatchManager INSTANCE = new ResourcePatchManager();
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final Multimap<ResourceLocation, ResourcePatch> patches = HashMultimap.create();
    
    private ResourcePatchManager() {}
    
    public void add(ResourceLocation id, ResourcePatch patch) {
        this.patches.put(id, patch);
    }
    
    @NotNull
    public InputStream patch(ResourceLocation id, InputStream input) {
        var patches = this.patches.get(id);
        if (patches.isEmpty()) {
            return input;
        }
        
        try {
            var bytes = IOUtils.toByteArray(input);
            for (ResourcePatch patch : patches) {
                bytes = patch.apply(bytes);
            }
            
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            LOGGER.error("Failed to load bytes for patching for resource: '{}'", id, e);
            return propagateException(e);
        }
    }
    
    private static InputStream propagateException(IOException exception) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw exception;
            }
        };
    }
}
