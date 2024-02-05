package gay.lemmaeof.kdlycontent.content.custom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.NamedProperties;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.*;

public class CustomItemGenerator implements ItemGenerator {
	@Override
	public Item generateItem(Identifier id, QuiltItemSettings settings, List<KDLNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		return new CustomItem(settings, props);
	}

	protected KdlyItemProperties parseProperties(Identifier id, List<KDLNode> customConfig) {
		KdlyItemProperties.BarProperties bar = null;
		KdlyItemProperties.ChargeProperties charge = null;
		boolean hasGlint = false;
		boolean selfRemainder = false;
		List<Text> lore = new ArrayList<>();
		Map<KdlyItemProperties.ItemFunctionPoint, Identifier> functions = new HashMap<>();

		Map<String, KDLNode> nodes = KdlHelper.mapNodes(customConfig);

		if (nodes.containsKey("bar")) {
			Map<String, KDLNode> barNodes = KdlHelper.mapNodes(nodes.get("bar").getChild().orElse(new KDLDocument.Builder().build()).getNodes());
			int color = KdlHelper.getArg(barNodes.get("color"), 1, 0xFFFFFF);
			String tag = KdlHelper.getArg(nodes.get("tag"), 1, "");
			int max = KdlHelper.getArg(nodes.get("max"), 1, 0);
			boolean showWhenFull = KdlHelper.getArg(nodes.get("showWhenFull"), 1, false);
			bar = new KdlyItemProperties.BarProperties(color, tag, max, showWhenFull);
		}

		if (nodes.containsKey("charge")) {
			Map<String, KDLNode> chargeNodes = KdlHelper.mapNodes(nodes.get("charge").getChild().orElse(new KDLDocument.Builder().build()).getNodes());
			int minDuration = KdlHelper.getArg(chargeNodes.get("minDuration"), 1, 0);
			int maxDuration = KdlHelper.getArg(chargeNodes.get("maxDuration"), 1, 0);
			UseAction action = NamedProperties.USE_ACTIONS.get(KdlHelper.getArg(chargeNodes.get("action"), 1, "none"));
			charge = new KdlyItemProperties.ChargeProperties(minDuration, maxDuration, action);
		}

		if (nodes.containsKey("glint")) hasGlint = KdlHelper.getArg(nodes.get("glint"), 1, true);

		if (nodes.containsKey("selfRemainder")) selfRemainder = KdlHelper.getArg(nodes.get("selfRemainder"), 1, true);

		if (nodes.containsKey("lore")) {
			List<KDLNode> loreLines = nodes.get("lore").getChild().orElse(new KDLDocument.Builder().build()).getNodes();
			JsonArray loreArray = KdlHelper.parseJsonArray(loreLines);
			for (JsonElement elem : loreArray) {
				lore.add(Text.Serializer.fromJson(elem));
			}
		}

		if (nodes.containsKey("functions")) {
			Map<String, KDLNode> funcNodes = KdlHelper.mapNodes(nodes.get("functions").getChild().orElse(new KDLDocument.Builder().build()).getNodes());
			for (String str : funcNodes.keySet()) {
				KDLNode node = funcNodes.get(str);
				try {
					KdlyItemProperties.ItemFunctionPoint point = KdlyItemProperties.ItemFunctionPoint.forName(str);
					functions.put(point, new Identifier(KdlHelper.getArg(node, 0, "")));
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		if (charge == null &&
				(functions.containsKey(KdlyItemProperties.ItemFunctionPoint.CHARGE_FINISH)
						|| functions.containsKey(KdlyItemProperties.ItemFunctionPoint.CHARGE_RELEASE))) {
			throw new ParseException(id, "Custom item defines charge finish or release functions without setting charge time");
		}

		return new KdlyItemProperties(Optional.ofNullable(bar), Optional.ofNullable(charge), hasGlint, selfRemainder, lore, functions);
	}
}
