package com.drppp.drtech.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.drppp.drtech.personalspace.world.PWWorldProvider;

@Mixin(GameRegistry.class)
public class GameRegistryMixin {

    @Inject(method = "generateWorld", at = @At("HEAD"), cancellable = true, remap = false)
    private static void generateWorldMixin(int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                                           IChunkProvider chunkProvider, CallbackInfo ci) {
        if (world.provider instanceof PWWorldProvider) {
            ci.cancel();
        }
    }
}
