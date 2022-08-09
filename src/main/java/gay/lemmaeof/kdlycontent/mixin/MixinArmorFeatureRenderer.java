package gay.lemmaeof.kdlycontent.mixin;

import java.util.Map;

import gay.lemmaeof.kdlycontent.content.type.ArmorMaterialContentType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public class MixinArmorFeatureRenderer {
	@Shadow
	@Final
	private static Map<String, Identifier> ARMOR_TEXTURE_CACHE;

	@Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
	private void injectCustomArmorTextures(ArmorItem item, boolean legs, @Nullable String overlay, CallbackInfoReturnable<Identifier> info) {
		ArmorMaterial mat = item.getMaterial();
		if (mat instanceof ArmorMaterialContentType.CustomArmorMaterial custom) {
			String string = custom.getNamespace() + ":textures/models/armor/" + custom.getName() + "_layer_" + (legs ? 2 : 1) + (overlay == null ? "" : "_" + overlay) + ".png";
			info.setReturnValue(ARMOR_TEXTURE_CACHE.computeIfAbsent(string, Identifier::new));
		}
	}
}
