package org.polyfrost.waveycapes.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.polyfrost.waveycapes.CapeHolder;
import org.polyfrost.waveycapes.CapePhysics;
import org.polyfrost.waveycapes.sim.CapeSimulation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements CapeHolder {
    @Unique
    private CapeSimulation waveyCapes$simulation;

    @Override
    public CapeSimulation getCapeSimulation() {
        return this.waveyCapes$simulation;
    }

    @Override
    public void setCapeSimulation(CapeSimulation simulation) {
        this.waveyCapes$simulation = simulation;
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void waveyCapes$tickCape(CallbackInfo ci) {
        if ((Object) this instanceof AbstractClientPlayerEntity) {
            CapePhysics.tick((AbstractClientPlayerEntity) (Object) this);
        }
    }
}
