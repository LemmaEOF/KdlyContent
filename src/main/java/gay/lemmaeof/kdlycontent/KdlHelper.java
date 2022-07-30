package gay.lemmaeof.kdlycontent;

import dev.hbeck.kdl.objects.KDLNode;

import javax.annotation.Nullable;
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

	@Nullable
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
