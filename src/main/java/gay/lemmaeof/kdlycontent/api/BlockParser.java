package gay.lemmaeof.kdlycontent.api;

import com.google.gson.JsonObject;
import dev.kdl.KdlNode;
import net.minecraft.util.Identifier;

import java.util.List;

public interface BlockParser {

	JsonObject parseData(Identifier id, List<KdlNode> customConfig) throws ParseException;
}
