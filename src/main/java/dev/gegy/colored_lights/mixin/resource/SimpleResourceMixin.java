package dev.gegy.colored_lights.mixin.resource;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.gegy.colored_lights.resource.ResourcePatchManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleResource;

@Mixin(SimpleResource.class)
public class SimpleResourceMixin {
    @Shadow
    @Final
    private ResourceLocation id;
    @Shadow
    @Final
    @Mutable
    private InputStream inputStream;
    
    @Unique
    private boolean colored_lights$patchedResource;
    
    @Inject(method = "getInputStream", at = @At("HEAD"), require = 1)
    private void getInputStream(CallbackInfoReturnable<InputStream> ci) {
        if (!this.colored_lights$patchedResource) {
            this.colored_lights$patchedResource = true;
            this.inputStream = ResourcePatchManager.INSTANCE.patch(this.id, this.inputStream);
        }
    }
}
