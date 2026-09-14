package gay.lemmaeof.kdlycontent.content.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.BlockParser;
import gay.lemmaeof.kdlycontent.util.Cuboid;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlockParser implements BlockParser {
	@Override
	public JsonObject parseData(Identifier id, List<KdlNode> customConfig) throws ParseException {
		CustomBlock.KdlyBlockBehaviors behaviors = parseBehaviors(id, customConfig);
		DataResult<JsonElement> result = CustomBlock.KdlyBlockBehaviors.CODEC.encodeStart(JsonOps.INSTANCE, behaviors);
		if (result.isError()) throw new ParseException(id, "Error encoding custom block behaviors: " + result.error().get().message());
		JsonObject ret = new JsonObject();
		ret.add("behaviors", result.result().get());
		return ret;
	}

	protected CustomBlock.KdlyBlockBehaviors parseBehaviors(Identifier id, List<KdlNode> customConfig) {
		boolean hasWaterlogged = false;
		CustomBlock.RotationProperty rotationProp = CustomBlock.RotationProperty.NONE;
		CustomBlock.PlacementRule placementRule = CustomBlock.PlacementRule.PLAYER;
		List<Cuboid> defaultShape = new ArrayList<>();
		Map<CustomBlock.BlockFunctionPoint, Identifier> functions = new HashMap<>();

		Map<String, KdlNode> nodes = KdlHelper.mapNodes(customConfig);

		if (nodes.containsKey("properties")) {
			Map<String, KdlNode> propNodes = KdlHelper.mapNodes(nodes.get("properties").children());
			if (propNodes.containsKey("waterloggable")) hasWaterlogged = true;
			if (propNodes.containsKey("rotation")) {
				KdlNode rotNode = propNodes.get("rotation");
				try {
					rotationProp = CustomBlock.RotationProperty.forName(KdlHelper.getProp(rotNode, "type", "facing"));
					if (rotNode.properties().hasProperty("placement")) {
						placementRule = CustomBlock.PlacementRule.forName(KdlHelper.getProp(rotNode, "placement", "side"));
					}
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		if (nodes.containsKey("shape")) {
			List<KdlNode> shapeNodes = nodes.get("shape").children();
			for (KdlNode shapeNode : shapeNodes) {
				defaultShape.add(new Cuboid(
						KdlHelper.getProp(shapeNode, "min_x", 0.0F),
						KdlHelper.getProp(shapeNode, "min_y", 0.0F),
						KdlHelper.getProp(shapeNode, "min_z", 0.0F),
						KdlHelper.getProp(shapeNode, "max_x", 16.0F),
						KdlHelper.getProp(shapeNode, "max_y", 16.0F),
						KdlHelper.getProp(shapeNode, "max_z", 16.0F)
				));
			}
		}

		if (nodes.containsKey("functions")) {
			Map<String, KdlNode> funcNodes = KdlHelper.mapNodes(nodes.get("functions").children());
			for (String str : funcNodes.keySet()) {
				KdlNode node = funcNodes.get(str);
				try {
					CustomBlock.BlockFunctionPoint point = CustomBlock.BlockFunctionPoint.forName(str);
					functions.put(point, Identifier.of(KdlHelper.getArg(node, 0, "")));
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		return new CustomBlock.KdlyBlockBehaviors(hasWaterlogged, rotationProp, placementRule, defaultShape, functions);
	}

}
