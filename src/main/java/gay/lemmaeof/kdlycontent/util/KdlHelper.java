package gay.lemmaeof.kdlycontent.util;

import dev.hbeck.kdl.objects.KDLNode;

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
}
