package gay.lemmaeof.kdlycontent.util;

import com.google.gson.*;
import dev.kdl.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KdlHelper {
	public static float getProp(KdlNode node, String property, float defaultValue) {
		if (node.properties().hasProperty(property)) {
			//this sucks!
			Optional<KdlValue<?>> opt = (Optional<KdlValue<?>>) (Object) node.properties().getValue(property);
			if (opt.isPresent() && opt.get() instanceof KdlNumber<?> num) {
				return (float) num.asDouble();
			}
		}
		return defaultValue;
	}

	public static boolean getProp(KdlNode node, String property, boolean defaultValue) {
		if (node.properties().hasProperty(property)) {
			//this sucks!
			Optional<KdlValue<?>> opt = (Optional<KdlValue<?>>) (Object) node.properties().getValue(property);
			if (opt.isPresent() && opt.get() instanceof KdlBoolean bool) {
				return bool.booleanValue();
			}
		}
		return defaultValue;
	}

	public static int getProp(KdlNode node, String property, int defaultValue) {
		if (node.properties().hasProperty(property)) {
			//this sucks!
			Optional<KdlValue<?>> opt = (Optional<KdlValue<?>>) (Object) node.properties().getValue(property);
			if (opt.isPresent() && opt.get() instanceof KdlNumber<?> num) {
				return num.asInt();
			}
		}
		return defaultValue;
	}

	public static String getProp(KdlNode node, String property, String defaultValue) {
		if (node.properties().hasProperty(property)) {
			return String.valueOf(node.properties().getValue(property).orElse((KdlValue) new KdlString(defaultValue)).value());
		}
		return defaultValue;
	}

	public static float getArg(KdlNode node, int index, float defaultValue) {
		if (node.arguments().size() > index) {
			if (node.arguments().get(index) instanceof KdlNumber<?> num) {
				return (float) num.asDouble();
			}
		}
		return defaultValue;
	}

	public static boolean getArg(KdlNode node, int index, boolean defaultValue) {
		if (node.arguments().size() > index) {
			if (node.arguments().get(index) instanceof KdlBoolean bool) {
				return bool.booleanValue();
			}
		}
		return defaultValue;
	}

	public static int getArg(KdlNode node, int index, int defaultValue) {
		if (node.arguments().size() > index) {
			if (node.arguments().get(index) instanceof KdlNumber<?> num) {
				return num.asInt();
			}
		}
		return defaultValue;
	}

	public static String getArg(KdlNode node, int index, String defaultValue) {
		if (node.arguments().size() > index) {
			return String.valueOf(node.arguments().get(index).value());
		}
		return defaultValue;
	}

	public static KdlNode getChild(List<KdlNode> nodes, String name) {
		KdlNode ret = null;
		for (KdlNode node : nodes) {
			if (node.name().equals(name)) {
				ret = node;
			}
		}
		return ret;
	}

	public static Map<String, KdlNode> mapNodes(List<KdlNode> nodes) {
		Map<String, KdlNode> ret = new HashMap<>();
		for (KdlNode node : nodes) {
			ret.put(node.name(), node);
		}
		return ret;
	}

	public static JsonElement parseKdlyJson(KdlDocument doc) {
		return parseKdlyJson(doc.nodes());
	}

	public static JsonElement parseKdlyJson(List<KdlNode> nodes) {
		return switch(parseJsonType(nodes)) {
			case LITERAL -> throw new IllegalStateException("unreachable");
			case OBJECT -> parseJsonObject(nodes);
			case ARRAY -> parseJsonArray(nodes);
		};
	}

	public static JsonObject parseJsonObject(KdlNode node) {
		JsonObject ret = new JsonObject();
		for (String key : node.properties().propertyNames()) {
			ret.add(key, parseJsonLiteral(node.properties().getValue(key).get()));
		}
		if (!node.children().isEmpty()) {
			JsonObject listObj = parseJsonObject(node.children());
			for (String key : listObj.keySet()) {
				ret.add(key, listObj.get(key));
			}
		}
		return ret;
	}

	public static JsonObject parseJsonObject(List<KdlNode> nodes) {
		JsonObject ret = new JsonObject();
		for (KdlNode node : nodes) {
			String key = node.name();
			switch (parseJsonType(node)) {
				case LITERAL -> ret.add(key, parseJsonLiteral(node.arguments().get(0)));
				case OBJECT -> ret.add(key, parseJsonObject(node));
				case ARRAY -> ret.add(key, parseJsonArray(node));
			}
		}
		return ret;
	}

	public static JsonArray parseJsonArray(KdlNode node) {
		JsonArray ret = new JsonArray();
		for (KdlValue<?> val : node.arguments()) {
			ret.add(parseJsonLiteral(val));
		}
		if (!node.children().isEmpty()) {
			JsonArray arrayObj = parseJsonArray(node.children());
			ret.addAll(arrayObj);
		}
		return ret;
	}

	public static JsonArray parseJsonArray(List<KdlNode> nodes) {
		JsonArray ret = new JsonArray();
		for (KdlNode node : nodes) {
			switch(parseJsonType(node)) {
				case LITERAL -> ret.add(parseJsonLiteral(node.arguments().get(0)));
				case OBJECT -> ret.add(parseJsonObject(node));
				case ARRAY -> ret.add(parseJsonArray(node));
			}
		}
		return ret;
	}

	public static JsonElement parseJsonLiteral(KdlValue<?> value) {
		if (value.isNull()) return JsonNull.INSTANCE;
		if (value.isBoolean()) return new JsonPrimitive(((KdlBoolean)value).value());
		if (value.isString()) return new JsonPrimitive(((KdlString)value).value());
		if (value.isNumber()) return new JsonPrimitive(((KdlNumber<Number>)value).value());
		throw new IllegalArgumentException("Unrecognized state for KDL value: " + value);
	}

	public static KdlyJsonType parseJsonType(KdlNode node) {
		//implementation of Bram Gotink's node type heuristic: https://github.com/kdl-org/kdl/issues/281#issuecomment-1215058690
		if (node.type() != null) {
			if (node.type().equals("array")) return KdlyJsonType.ARRAY;
			if (node.type().equals("object")) return KdlyJsonType.OBJECT;
			throw new IllegalArgumentException("Illegal node type hint `" + node.type() + "` found: must be `array` or `object`");
		}
		if (!node.properties().propertyNames().isEmpty()) return KdlyJsonType.OBJECT;
		if (!node.children().isEmpty()) {
			return parseJsonType(node.children());
		}
		if (node.arguments().size() > 1) return KdlyJsonType.ARRAY;
		return KdlyJsonType.LITERAL;
	}

	public static KdlyJsonType parseJsonType(List<KdlNode> nodes) {
		for (KdlNode node : nodes) {
			if (!node.name().equals("-")) return KdlyJsonType.OBJECT;
		}
		return KdlyJsonType.ARRAY;
	}

	public enum KdlyJsonType {
		OBJECT,
		ARRAY,
		LITERAL
	}
}
