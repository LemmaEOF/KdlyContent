package gay.lemmaeof.kdlycontent.init;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.content.type.*;
import gay.lemmaeof.kdlycontent.hooks.DynamicRecipeCallback;
import gay.lemmaeof.kdlycontent.hooks.DynamicRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;

public class KdlyContentTypes {

	public static final ContentType BLOCK = register("block", new BlockContentType());
	public static final ContentType ITEM = register("item", new ItemContentType());
	public static final ContentType TOOL_MATERIAL = register("tool_material", new ToolMaterialContentType());
	public static final ContentType ARMOR_MATERIAL = register("armor_material", new ArmorMaterialContentType());
	public static final ContentType RECIPE = register("recipe", new RecipeContentType());
	public static final ContentType GROUP = register("group", new GroupContentType());
	public static final ContentType REQUIRE = register("require", new ConditionalContentType());

	private static ContentType register(String name, ContentType type) {
		return Registry.register(KdlyRegistries.CONTENT_TYPES, Identifier.of(KdlyContent.MODID, name), type);
	}

	public static void init() {
		for (RegistryLoader.Entry<?> entry : DynamicRegistries.getDynamicRegistries()) {
			registerDynamicType(entry);
		}
		DynamicRecipeCallback.EVENT.register(((recipeConsumer, ops) -> {
			for (Identifier id : RecipeContentType.KDLY_RECIPES.keySet()) {
				DataResult<Pair<Recipe<?>, JsonElement>> result = Recipe.CODEC.decode(ops, RecipeContentType.KDLY_RECIPES.get(id));
				if (result.isError()) {
					KdlyContent.LOGGER.error("Error decoding recipe {}: {}", id, result.error().get().message());
				} else {
					recipeConsumer.accept(new RecipeEntry<>(id, result.result().get().getFirst()));
				}
			}
		}));
	}

	private static <T> void registerDynamicType(RegistryLoader.Entry<T> entry) {
		Identifier id = entry.key().getValue();
		DynamicContentType<T> type = new DynamicContentType<>(entry);
		//redirect vanilla dynreg types to our namespace
		//also replace `/` with `.` for identifier legality without quotes
		Identifier sanitizedId;
		if (id.getNamespace().equals("minecraft")) sanitizedId = Identifier.of(KdlyContent.MODID, id.getPath().replace('/', '.'));
		else sanitizedId = Identifier.of(id.getNamespace(), id.getPath().replace('/', '.'));
		Registry.register(KdlyRegistries.CONTENT_TYPES, sanitizedId, type);
		DynamicRegistrationCallback.event((RegistryKey<Registry<T>>) entry.key()).register((registry, infoGetter, decoder) -> {
			RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
			for (Identifier entryId : type.entries.keySet()) {
				DataResult<Pair<T, JsonElement>> result = decoder.decode(ops, type.entries.get(entryId));
				if (result.isError()) {
					KdlyContent.LOGGER.error("Error decoding dynamic entry {} in {}: {}", entryId, entry.key().getValue(), result.error().get().message());
				} else {
					Registry.register(registry, entryId, result.result().get().getFirst());
				}
			}
		});
	}
}
