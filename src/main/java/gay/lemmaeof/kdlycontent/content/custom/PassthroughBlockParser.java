package gay.lemmaeof.kdlycontent.content.custom;

import com.google.gson.JsonObject;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.BlockParser;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.util.Identifier;

import java.util.List;

public class PassthroughBlockParser implements BlockParser {
	@Override
	public JsonObject parseData(Identifier id, List<KdlNode> customConfig) throws ParseException {
		return KdlHelper.parseJsonObject(customConfig);
	}
}
