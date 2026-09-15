package gay.lemmaeof.kdlycontent.hooks;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryOps;

import java.util.function.Consumer;

public interface DynamicRecipeCallback {
	void onRecipeLoad(Consumer<RecipeEntry<?>> recipeConsumer, RegistryOps<JsonElement> ops);

	Event<DynamicRecipeCallback> EVENT = EventFactory.createArrayBacked(DynamicRecipeCallback.class,
		events -> ((recipeConsumer, ops) -> {
			for (DynamicRecipeCallback event : events) {
				event.onRecipeLoad(recipeConsumer, ops);
			}
		})
	);
}
