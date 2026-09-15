package gay.lemmaeof.kdlycontent.util;

import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record ContentTemplate(Identifier id, KdlNode template) {

	public KdlNode expandFor(Identifier otherId, KdlNode other) throws ParseException {
		Map<String, KdlValue<?>> valueParams = new HashMap<>();
		Map<String, KdlNode> nodeParams = KdlHelper.mapNodes(other.children());
		for (String key : other.properties().propertyNames()) {
			if (key.equals("template")) continue;
			valueParams.put(key, other.getProperty(key).get());
		}
		return expandNode(otherId, template, valueParams, nodeParams);
	}

	private KdlNode expandNode(Identifier otherId, KdlNode node, Map<String, KdlValue<?>> valueParams, Map<String, KdlNode> nodeParams) throws ParseException {
		//if the whole node is an arg just fill it in wholesale + rename properly
		if (node.type() != null && node.type().equals("parameter")) {
			String argName = KdlHelper.getArg(node, 0, "");
			if (argName.isEmpty()) {
				throw new ParseException(otherId, "missing name for node parameter in template" + id);
			} else if (!nodeParams.containsKey(argName)) {
				throw new ParseException(otherId, "missing node parameter " + argName + " for template " + id);
			} else {
				return nodeParams.get(argName).mutate().name(node.name()).build();
			}
		}
		//kinda expensive recursive deep copy with param filling
		KdlNode.Builder builder = KdlNode.builder().name(node.name());
		//fill positional args
		for (KdlValue<?> arg : node.arguments()) {
			if (arg.type() != null && arg.type().equals("parameter")) {
				String argName = String.valueOf(arg.value());
				if (!valueParams.containsKey(argName)) {
					throw new ParseException(otherId, "missing value parameter " + argName + " for template " + id);
				} else {
					KdlValue<?> argValue = valueParams.get(argName);
					builder.argument(argValue);
				}
			} else {
				builder.argument(arg);
			}
		}
		//fill keyword properties
		for (String key : node.properties().propertyNames()) {
			KdlValue<?> val = node.getProperty(key).get();
			if (val.type() != null && val.type().equals("parameter")) {
				String argName = String.valueOf(val.value());
				if (!valueParams.containsKey(argName)) {
					throw new ParseException(otherId, "missing value parameter " + argName + " for template " + id);
				} else {
					KdlValue<?> argValue = valueParams.get(argName);
					builder.property(key, argValue);
				}
			} else {
				builder.property(key, val);
			}
		}

		for (KdlNode child : node.children()) {
			//recursion time!
			builder.child(expandNode(otherId, child, valueParams, nodeParams));
		}

		return builder.build();
	}
}
