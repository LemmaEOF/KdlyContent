package gay.lemmaeof.kdlycontent.mixin;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import gay.lemmaeof.kdlycontent.util.BlockSettingsCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class MixinAbstractBlock {
	//clearly inject head unconditional cancel
	@Inject(method = "createSettingsCodec", at = @At("HEAD"), cancellable = true)
	private static <B extends Block> void replaceDefaultSettingsCodec(CallbackInfoReturnable<RecordCodecBuilder<B, AbstractBlock.Settings>> info) {
		info.setReturnValue(BlockSettingsCodec.CODEC.fieldOf("properties").forGetter(AbstractBlock::getSettings));
	}
}
