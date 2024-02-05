package gay.lemmaeof.kdlycontent.content.custom;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.List;
import java.util.Map;

public class CustomToolItemGenerator extends CustomItemGenerator {
	@Override
	public Item generateItem(Identifier id, QuiltItemSettings settings, List<KDLNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(customConfig);
		Identifier tagId = new Identifier(KdlHelper.getArg(nodes.get("tag"), 0, ""));
		TagKey<Block> tag = TagKey.of(Registries.BLOCK.getKey(), tagId);
		return KdlyTools.construct(id, settings, customConfig,
				(material, attackDamage, attackSpeed, s) -> new CustomToolItem(attackDamage, attackSpeed, material, tag, settings, props));
	}
}
