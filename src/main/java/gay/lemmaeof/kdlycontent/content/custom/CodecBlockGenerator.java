package gay.lemmaeof.kdlycontent.content.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.PassedSettingsOps;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTypes;
import net.minecraft.util.Identifier;

import java.util.List;

public class CodecBlockGenerator implements BlockGenerator {
	@Override
	public Block generateBlock(Identifier id, AbstractBlock.Settings settings, List<KdlNode> customConfig) throws ParseException {
		JsonObject codecValue = KdlHelper.parseJsonObject(customConfig);
		codecValue.addProperty("properties", "<Injected properties placeholder");
		DataResult<Pair<Block, JsonElement>> res = BlockTypes.CODEC.codec().decode(new PassedSettingsOps<>(settings, JsonOps.INSTANCE), codecValue);
		if (res.isSuccess()) return res.result().get().getFirst();
		else throw new ParseException(id, "Decode error on codec block: " + res.error().get().message());
	}
}
