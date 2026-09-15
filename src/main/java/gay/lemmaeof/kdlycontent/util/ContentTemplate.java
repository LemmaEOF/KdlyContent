package gay.lemmaeof.kdlycontent.util;

import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record ContentTemplate(Identifier id, KdlNode template) {

	public KdlNode expandFor(Identifier otherId, KdlNode other) throws ParseException {
		Map<String, KdlValue<?>> valueArgs = new HashMap<>();
		Map<String, KdlNode> nodeArgs = KdlHelper.mapNodes(other.children());
		for (String key : other.properties().propertyNames()) {
			if (key.equals("template")) continue;
			valueArgs.put(key, other.getProperty(key).get());
		}
		return expandNode(otherId, template, valueArgs, nodeArgs);
	}

	private KdlNode expandNode(Identifier otherId, KdlNode node, Map<String, KdlValue<?>> valueArgs, Map<String, KdlNode> nodeArgs) throws ParseException {
		//if the whole node is an arg just fill it in wholesale + rename properly
		if (node.type() != null && node.type().equals("argument")) {
			String argName = KdlHelper.getArg(node, 0, "");
			if (argName.isEmpty()) {
				throw new ParseException(otherId, "missing name for node argument in template" + id);
			} else if (!nodeArgs.containsKey(argName)) {
				throw new ParseException(otherId, "missing node argument " + argName + " for template " + id);
			} else {
				return nodeArgs.get(argName).mutate().name(node.name()).build();
			}
		}
		//kinda expensive recursive deep copy with argument filling
		KdlNode.Builder builder = KdlNode.builder().name(node.name());
		//fill positional args
		for (KdlValue<?> arg : node.arguments()) {
			if (arg.type() != null && arg.type().equals("argument")) {
				String argName = String.valueOf(arg.value());
				if (!valueArgs.containsKey(argName)) {
					throw new ParseException(otherId, "missing value argument " + argName + " for template " + id);
				} else {
					KdlValue<?> argValue = valueArgs.get(argName);
					builder.argument(argValue);
				}
			} else {
				builder.argument(arg);
			}
		}
		//fill keyword properties
		for (String key : node.properties().propertyNames()) {
			KdlValue<?> val = node.getProperty(key).get();
			if (val.type() != null && val.type().equals("argument")) {
				String argName = String.valueOf(val.value());
				if (!valueArgs.containsKey(argName)) {
					throw new ParseException(otherId, "missing value argument " + argName + " for template " + id);
				} else {
					KdlValue<?> argValue = valueArgs.get(argName);
					builder.property(key, argValue);
				}
			} else {
				builder.property(key, val);
			}
		}

		for (KdlNode child : node.children()) {
			//recursion time!
			builder.child(expandNode(otherId, child, valueArgs, nodeArgs));
		}

		return builder.build();
	}
}
