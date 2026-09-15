package gay.lemmaeof.kdlycontent.util;

import dev.kdl.KdlNode;
import dev.kdl.KdlValue;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ContentTemplate(Identifier id, KdlNode template) {
	private static final Pattern PATTERN = Pattern.compile("\\$\\{[^\\}]+\\}");

	public KdlNode expandFor(Identifier otherId, KdlNode other) throws ParseException {
		Map<String, KdlValue<?>> valueParams = new HashMap<>();
		Map<String, KdlNode> nodeParams = KdlHelper.mapNodes(other.children());
		for (String key : other.properties().propertyNames()) {
			if (key.equals("template")) continue;
			valueParams.put(key, other.getProperty(key).get());
		}
		valueParams.put("_namespace", KdlValue.from(otherId.getNamespace()));
		return expandNode(otherId, template, valueParams, nodeParams);
	}

	private KdlNode expandNode(Identifier otherId, KdlNode node, Map<String, KdlValue<?>> valueParams, Map<String, KdlNode> nodeParams) throws ParseException {
		//if the whole node is an arg just fill it in wholesale + rename properly
		if (node.type() != null) {
			if (node.type().equals("parameter")) {
				String argName = KdlHelper.getArg(node, 0, "");
				if (argName.isEmpty()) {
					throw new ParseException(otherId, "missing name for node parameter in template" + id);
				} else if (!nodeParams.containsKey(argName)) {
					throw new ParseException(otherId, "missing node parameter " + argName + " for template " + id);
				} else {
					return nodeParams.get(argName).mutate().name(node.name()).build();
				}
			//make sure it doesn't stub its toe on the outer template tag
			} else if (!node.type().equals("template")) {
				throw new ParseException(otherId, "unknown type tag " + node.type() + " on node " + node.name());
			}
		}
		//kinda expensive recursive deep copy with param filling
		KdlNode.Builder builder = KdlNode.builder().name(node.name());
		//fill positional args
		for (KdlValue<?> arg : node.arguments()) {
			if (arg.type() != null) {
				if (arg.type().equals("parameter")) {
					String paramName = String.valueOf(arg.value());
					if (!valueParams.containsKey(paramName)) {
						throw new ParseException(otherId, "missing value parameter " + paramName + " for template " + id);
					} else {
						KdlValue<?> paramValue = valueParams.get(paramName);
						builder.argument(paramValue);
					}
				} else if (arg.type().equals("interpolate")) {
					String valText = String.valueOf(arg.value());
					builder.argument(interpolate(otherId, valText, valueParams));
				} else {
					throw new ParseException(otherId, "unknown type tag " + arg.type() + " on argument " + arg);
				}
			} else {
				builder.argument(arg);
			}
		}
		//fill keyword properties
		for (String key : node.properties().propertyNames()) {
			KdlValue<?> prop = node.getProperty(key).get();
			if (prop.type() != null) {
				if (prop.type().equals("parameter")) {
					String paramName = String.valueOf(prop.value());
					if (!valueParams.containsKey(paramName)) {
						throw new ParseException(otherId, "missing value parameter " + paramName + " for template " + id);
					} else {
						KdlValue<?> paramValue = valueParams.get(paramName);
						builder.property(key, paramValue);
					}
				} else if (prop.type().equals("interpolate")) {
					String valText = String.valueOf(prop.value());
					builder.property(key, interpolate(otherId, valText, valueParams));
				} else {
					throw new ParseException(otherId, "unknown type tag " + prop.type() + " on property " + key);
				}
			} else {
				builder.property(key, prop);
			}
		}

		for (KdlNode child : node.children()) {
			//recursion time!
			builder.child(expandNode(otherId, child, valueParams, nodeParams));
		}

		return builder.build();
	}

	private String interpolate(Identifier otherId, String toFill, Map<String, KdlValue<?>> valueParams) throws ParseException {
		Matcher matcher = PATTERN.matcher(toFill);
		String matched = matcher.replaceAll(result -> {
			String group = result.group();
			String param = group.substring(2, group.length()-1);
			if (!valueParams.containsKey(param)) throw new ParseException(otherId, "missing value parameter " + param + " for template " + id);
			return String.valueOf(valueParams.get(param).value());
		});
		KdlyContent.LOGGER.debug("Interpolated {} to {}", toFill, matched);
		return matched;
	}
}
