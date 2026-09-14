package gay.lemmaeof.kdlycontent.mixin;

import com.mojang.serialization.Decoder;
import gay.lemmaeof.kdlycontent.hooks.DynamicRegistrationCallback;
import net.minecraft.registry.*;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings({"unchecked"})
@Mixin(RegistryLoader.class)
public class MixinRegistryLoader {
	@Inject(method = "loadFromResource(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V", at = @At("TAIL"))
	private static <E> void hookRegistration(ResourceManager manager, RegistryOps.RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> decoder, Map<RegistryKey<?>, Exception> errors, CallbackInfo info) {
		DynamicRegistrationCallback.event((RegistryKey<Registry<E>>) registry.getKey()).invoker().onRegistration(registry, infoGetter, decoder);
	}
}
