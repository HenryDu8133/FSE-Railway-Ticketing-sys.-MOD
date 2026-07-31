package net.fsefmgftc.fseticket.init;

import net.fsefmgftc.fseticket.FseticketMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FseticketModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, FseticketMod.MODID);

    public static final Supplier<SoundEvent> HQ_SPEAKER = REGISTRY.register("hq_speaker", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(FseticketMod.MODID, "hq_speaker")));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}