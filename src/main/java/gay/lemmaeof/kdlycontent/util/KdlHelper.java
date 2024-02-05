package gay.lemmaeof.kdlycontent.util;

import com.google.gson.*;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KdlHelper {
	public static float getProp(KDLNode node, String property, float defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsNumberOrElse(defaultValue).floatValue();
		}
		return defaultValue;
	}

	public static boolean getProp(KDLNode node, String property, boolean defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsBooleanOrElse(defaultValue);
		}
		return defaultValue;
	}

	public static int getProp(KDLNode node, String property, int defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsNumberOrElse(defaultValue).intValue();
		}
		return defaultValue;
	}

	public static String getProp(KDLNode node, String property, String defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsString().getValue();
		}
		return defaultValue;
	}

	public static float getArg(KDLNode node, int index, float defaultValue) {
		if (node.getArgs().size() > index) {
			return node.getArgs().get(index).getAsNumberOrElse(defaultValue).floatValue();
		}
		return defaultValue;
	}

	public static boolean getArg(KDLNode node, int index, boolean defaultValue) {
		if (node.getArgs().size() > index) {
			return node.getArgs().get(index).getAsBooleanOrElse(defaultValue);
		}
		return defaultValue;
	}

	public static int getArg(KDLNode node, int index, int defaultValue) {
		if (node.getArgs().size() > index) {
			return node.getArgs().get(index).getAsNumberOrElse(defaultValue).intValue();
		}
		return defaultValue;
	}

	public static String getArg(KDLNode node, int index, String defaultValue) {
		if (node.getArgs().size() > index) {
			return node.getArgs().get(index).getAsString().getValue();
		}
		return defaultValue;
	}

	public static KDLNode getChild(List<KDLNode> nodes, String name) {
		KDLNode ret = null;
		for (KDLNode node : nodes) {
			if (node.getIdentifier().equals(name)) {
				ret = node;
			}
		}
		return ret;
	}

	public static Map<String, KDLNode> mapNodes(List<KDLNode> nodes) {
		Map<String, KDLNode> ret = new HashMap<>();
		for (KDLNode node : nodes) {
			ret.put(node.getIdentifier(), node);
		}
		return ret;
	}

	public static JsonElement parseKdlyJson(KDLDocument doc) {
		List<KDLNode> nodes = doc.getNodes();
		return switch(parseJsonType(nodes)) {
			case LITERAL -> throw new IllegalStateException("unreachable");
			case OBJECT -> parseJsonObject(nodes);
			case ARRAY -> parseJsonArray(nodes);
		};
	}

	public static JsonObject parseJsonObject(KDLNode node) {
		JsonObject ret = new JsonObject();
		for (String key : node.getProps().keySet()) {
			ret.add(key, parseJsonLiteral(node.getProps().get(key)));
		}
		if (node.getChild().isPresent()) {
			JsonObject listObj = parseJsonObject(node.getChild().get().getNodes());
			for (String key : listObj.keySet()) {
				ret.add(key, listObj.get(key));
			}
		}
		return ret;
	}

	public static JsonObject parseJsonObject(List<KDLNode> nodes) {
		JsonObject ret = new JsonObject();
		for (KDLNode node : nodes) {
			String key = node.getIdentifier();
			switch (parseJsonType(node)) {
				case LITERAL -> ret.add(key, parseJsonLiteral(node.getArgs().get(0)));
				case OBJECT -> ret.add(key, parseJsonObject(node));
				case ARRAY -> ret.add(key, parseJsonArray(node));
			}
		}
		return ret;
	}

	public static JsonArray parseJsonArray(KDLNode node) {
		JsonArray ret = new JsonArray();
		for (KDLValue<?> val : node.getArgs()) {
			ret.add(parseJsonLiteral(val));
		}
		if (node.getChild().isPresent()) {
			JsonArray arrayObj = parseJsonArray(node.getChild().get().getNodes());
			ret.addAll(arrayObj);
		}
		return ret;
	}

	public static JsonArray parseJsonArray(List<KDLNode> nodes) {
		JsonArray ret = new JsonArray();
		for (KDLNode node : nodes) {
			switch(parseJsonType(node)) {
				case LITERAL -> ret.add(parseJsonLiteral(node.getArgs().get(0)));
				case OBJECT -> ret.add(parseJsonObject(node));
				case ARRAY -> ret.add(parseJsonArray(node));
			}
		}
		return ret;
	}

	public static JsonElement parseJsonLiteral(KDLValue<?> value) {
		if (value.isNull()) return JsonNull.INSTANCE;
		if (value.isBoolean()) return new JsonPrimitive(value.getAsBooleanOrElse(false));
		if (value.isString()) return new JsonPrimitive(value.getAsString().getValue());
		if (value.isNumber()) return new JsonPrimitive(value.getAsNumberOrElse(0));
		throw new IllegalArgumentException("Unrecognized state for KDL value: " + value);
	}

	public static KdlyJsonType parseJsonType(KDLNode node) {
		//implementation of Bram Gotink's node type heuristic: https://github.com/kdl-org/kdl/issues/281#issuecomment-1215058690
		if (node.getType().isPresent()) {
			if (node.getType().get().equals("array")) return KdlyJsonType.ARRAY;
			if (node.getType().get().equals("object")) return KdlyJsonType.OBJECT;
			throw new IllegalArgumentException("Illegal node type hint `" + node.getType().get() + "` found: must be `array` or `object`");
		}
		if (!node.getProps().isEmpty()) return KdlyJsonType.OBJECT;
		if (node.getChild().isPresent()) {
			return parseJsonType(node.getChild().get().getNodes());
		}
		if (node.getArgs().size() > 1) return KdlyJsonType.ARRAY;
		return KdlyJsonType.LITERAL;
	}

	public static KdlyJsonType parseJsonType(List<KDLNode> nodes) {
		for (KDLNode node : nodes) {
			if (!node.getIdentifier().equals("-")) return KdlyJsonType.OBJECT;
		}
		return KdlyJsonType.ARRAY;
	}

	public enum KdlyJsonType {
		OBJECT,
		ARRAY,
		LITERAL
	}
}
