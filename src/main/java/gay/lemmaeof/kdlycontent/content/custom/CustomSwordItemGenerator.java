package gay.lemmaeof.kdlycontent.content.custom;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;

public class CustomSwordItemGenerator extends CustomItemGenerator {
	@Override
	public Item generateItem(Identifier id, Item.Settings settings, List<KdlNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		return KdlyTools.construct(id, settings, customConfig,
				(material, s) -> new CustomSwordItem(material, settings, props));
	}
}
