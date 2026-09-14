package gay.lemmaeof.kdlycontent.mixin.client;

import gay.lemmaeof.kdlycontent.hooks.LateClientModInitializer;
import gay.lemmaeof.kdlycontent.hooks.LateModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	//the same injection point as fabric registry sync's refreeze but one order earlier - the last possible moment to register new things
	//TODO: does this assumption hold under ffapi?
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"), order = 999)
	private void runLateClientInit(CallbackInfo info) {
		FabricLoader.getInstance().getEntrypoints("kdlycontent:late_main", LateModInitializer.class).forEach(LateModInitializer::onLateInitialize);
		FabricLoader.getInstance().getEntrypoints("kdlycontent:late_client", LateClientModInitializer.class).forEach(LateClientModInitializer::onLateInitializeClient);
	}
}
