package gay.lemmaeof.kdlycontent.hooks;

import com.mojang.serialization.Decoder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;

import java.util.HashMap;
import java.util.Map;

//I promise I know what I'm doing. Trust me.
@SuppressWarnings({"unchecked", "rawtypes"})
@FunctionalInterface
public interface DynamicRegistrationCallback<T> {
	void onRegistration(MutableRegistry<T> registry, RegistryOps.RegistryInfoGetter infoGetter, Decoder<T> decoder);

	static Map<RegistryKey<Registry>, Event<DynamicRegistrationCallback>> EVENTS = new HashMap<>();

	static <T> Event<DynamicRegistrationCallback<T>> event(RegistryKey<Registry<T>> registry) {
		return EVENTS.computeIfAbsent((RegistryKey) registry, r -> EventFactory.createArrayBacked(
			DynamicRegistrationCallback.class,
			callbacks -> (reg, getter, decoder) -> {
				for (DynamicRegistrationCallback callback : callbacks) {
					callback.onRegistration(reg, getter, decoder);
				}
			})
		);
	}
}
