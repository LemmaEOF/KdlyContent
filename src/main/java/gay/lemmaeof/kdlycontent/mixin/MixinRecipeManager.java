package gay.lemmaeof.kdlycontent.mixin;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import gay.lemmaeof.kdlycontent.hooks.DynamicRecipeCallback;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
	@Shadow
	@Final
	private RegistryWrapper.WrapperLookup registryLookup;

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMultimap$Builder;build()Lcom/google/common/collect/ImmutableMultimap;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void runEvent(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci, ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> typeBuilder, com.google.common.collect.ImmutableMap.Builder<Identifier, RecipeEntry<?>> idBuilder) {
		List<RecipeEntry<?>> eventEntries = new ArrayList<>();
		DynamicRecipeCallback.EVENT.invoker().onRecipeLoad(eventEntries::add, registryLookup.getOps(JsonOps.INSTANCE));
		for (RecipeEntry<?> entry : eventEntries) {
			typeBuilder.put(entry.value().getType(), entry);
			idBuilder.put(entry.id(), entry);
		}
	}
}
