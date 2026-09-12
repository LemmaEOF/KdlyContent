package gay.lemmaeof.kdlycontent.content.custom;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class CustomToolItemGenerator extends CustomItemGenerator {
	@Override
	public Item generateItem(Identifier id, Item.Settings settings, List<KdlNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(customConfig);
		Identifier tagId = Identifier.of(KdlHelper.getArg(nodes.get("tag"), 0, ""));
		TagKey<Block> tag = TagKey.of(Registries.BLOCK.getKey(), tagId);
		return KdlyTools.construct(id, settings, customConfig,
				(material, s) -> new CustomToolItem(material, tag, settings, props));
	}
}
