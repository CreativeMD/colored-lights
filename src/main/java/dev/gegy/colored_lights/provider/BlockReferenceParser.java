package dev.gegy.colored_lights.provider;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class BlockReferenceParser {
    @Nullable
    public static Either<Block, BlockState> parse(String reference) {
        if (reference.indexOf('[') != -1) {
            var state = parseBlockState(reference);
            return state != null ? Either.right(state) : null;
        } else {
            var block = parseBlock(reference);
            return block != null ? Either.left(block) : null;
        }
    }
    
    @Nullable
    private static BlockState parseBlockState(String reference) {
        int propertiesIndex = reference.indexOf('[');
        if (propertiesIndex == -1 || !reference.endsWith("]")) {
            throw new JsonSyntaxException("Malformed block state reference: " + reference);
        }
        
        var blockReference = reference.substring(0, propertiesIndex);
        var propertiesReference = reference.substring(propertiesIndex + 1, reference.length() - 1);
        
        var block = parseBlock(blockReference);
        if (block != null) {
            return parseBlockStateProperties(blockReference, propertiesReference, block);
        } else {
            return null;
        }
    }
    
    private static BlockState parseBlockStateProperties(String blockReference, String propertiesReference, Block block) {
        var state = block.defaultBlockState();
        var states = block.getStateDefinition();
        
        var properties = parseProperties(propertiesReference);
        for (var entry : properties.entrySet()) {
            var property = states.getProperty(entry.getKey());
            if (property != null) {
                state = parseBlockStateProperty(state, property, entry.getValue());
            } else {
                throw new JsonSyntaxException("Missing block state property " + entry.getKey() + " on " + blockReference);
            }
        }
        
        return state;
    }
    
    private static <T extends Comparable<T>> BlockState parseBlockStateProperty(BlockState state, Property<T> property, String valueReference) {
        var value = property.getValue(valueReference)
                .orElseThrow(() -> new JsonSyntaxException("Invalid property value " + valueReference + " for " + property + " on " + state.getBlock()));
        
        return state.setValue(property, value);
    }
    
    private static Map<String, String> parseProperties(String reference) {
        reference = reference.replaceAll(" ", "");
        
        var properties = new Object2ObjectArrayMap<String, String>();
        for (var declaration : reference.split(",")) {
            int equalsIdx = declaration.indexOf('=');
            if (equalsIdx == -1) {
                throw new JsonSyntaxException("Malformed block state property reference: " + declaration);
            }
            
            var key = declaration.substring(0, equalsIdx);
            var value = declaration.substring(equalsIdx + 1);
            properties.put(key, value);
        }
        
        return properties;
    }
    
    @Nullable
    private static Block parseBlock(String reference) {
        if (ResourceLocation.isValidResourceLocation(reference)) {
            var blockId = new ResourceLocation(reference);
            return Registry.BLOCK.get(blockId);
        }
        throw new JsonSyntaxException("Malformed block identifier: " + reference);
    }
}
