package gay.lemmaeof.kdlycontent.content.custom;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.NamedProperties;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

import java.util.*;

public class CustomItemGenerator implements ItemGenerator {
	@Override
	public Item generateItem(Identifier id, Item.Settings settings, List<KdlNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		return new CustomItem(settings, props);
	}

	protected KdlyItemProperties parseProperties(Identifier id, List<KdlNode> customConfig) {
		KdlyItemProperties.BarProperties bar = null;
		KdlyItemProperties.ChargeProperties charge = null;
		boolean selfRemainder = false;
		Map<KdlyItemProperties.ItemFunctionPoint, Identifier> functions = new HashMap<>();

		Map<String, KdlNode> nodes = KdlHelper.mapNodes(customConfig);

		if (nodes.containsKey("bar")) {
			Map<String, KdlNode> barNodes = KdlHelper.mapNodes(nodes.get("bar").children());
			int color = KdlHelper.getArg(barNodes.get("color"), 0, 0xFFFFFF);
			String tag = KdlHelper.getArg(barNodes.get("tag"), 0, "");
			int max = KdlHelper.getArg(barNodes.get("max"), 0, 0);
			boolean showWhenFull = KdlHelper.getArg(barNodes.get("show_when_full"), 0, false);
			bar = new KdlyItemProperties.BarProperties(color, tag, max, showWhenFull);
		}

		if (nodes.containsKey("charge")) {
			Map<String, KdlNode> chargeNodes = KdlHelper.mapNodes(nodes.get("charge").children());
			int minDuration = KdlHelper.getArg(chargeNodes.get("min_duration"), 0, 0);
			int maxDuration = KdlHelper.getArg(chargeNodes.get("max_duration"), 0, 0);
			UseAction action = NamedProperties.USE_ACTIONS.get(KdlHelper.getArg(chargeNodes.get("action"), 0, "none"));
			charge = new KdlyItemProperties.ChargeProperties(minDuration, maxDuration, action);
		}

		if (nodes.containsKey("self_remainder")) selfRemainder = KdlHelper.getArg(nodes.get("self_remainder"), 0, true);

		if (nodes.containsKey("functions")) {
			Map<String, KdlNode> funcNodes = KdlHelper.mapNodes(nodes.get("functions").children());
			for (String str : funcNodes.keySet()) {
				KdlNode node = funcNodes.get(str);
				try {
					KdlyItemProperties.ItemFunctionPoint point = KdlyItemProperties.ItemFunctionPoint.forName(str);
					functions.put(point, Identifier.of(KdlHelper.getArg(node, 0, "")));
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		//TODO: allow charge finish for foods?
		if (charge == null &&
				(functions.containsKey(KdlyItemProperties.ItemFunctionPoint.CHARGE_FINISH)
						|| functions.containsKey(KdlyItemProperties.ItemFunctionPoint.CHARGE_RELEASE))) {
			throw new ParseException(id, "Custom item defines charge finish or release functions without setting charge time");
		}

		return new KdlyItemProperties(Optional.ofNullable(bar), Optional.ofNullable(charge), selfRemainder, functions);
	}
}
