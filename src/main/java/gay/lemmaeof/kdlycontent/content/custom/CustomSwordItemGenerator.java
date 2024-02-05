package gay.lemmaeof.kdlycontent.content.custom;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.List;

public class CustomSwordItemGenerator extends CustomItemGenerator {
	@Override
	public Item generateItem(Identifier id, QuiltItemSettings settings, List<KDLNode> customConfig) throws ParseException {
		KdlyItemProperties props = parseProperties(id, customConfig);
		return KdlyTools.construct(id, settings, customConfig,
				(material, attackDamage, attackSpeed, s) -> new CustomSwordItem(material, (int)attackDamage, attackSpeed, settings, props));
	}
}
