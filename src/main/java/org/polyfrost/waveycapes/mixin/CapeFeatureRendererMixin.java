package org.polyfrost.waveycapes.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.polyfrost.waveycapes.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void waveyCapes$renderCape(
        AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
        float animationProgress, float headYaw, float headPitch, float scale, CallbackInfo ci
    ) {
        ci.cancel();
        CapeLayer.render(player, tickDelta);
    }
}
