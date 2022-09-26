package gay.lemmaeof.kdlycontent.util;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.type.ToolMaterialContentType;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.List;
import java.util.Map;

public class KdlyTools {

	public static Item construct(Identifier id, QuiltItemSettings settings, List<KDLNode> customConfig, ToolCreator creator) {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(customConfig);
		//TODO: ability to define material in-line
		KDLNode materialNode = nodes.get("material");
		if (materialNode == null) throw new ParseException(id, "No material specified");
		String matId = materialNode.getArgs().get(0).getAsString().getValue();
		ToolMaterial mat = ToolMaterialContentType.getMaterial(matId, id);
		KDLNode attackDamageNode = nodes.get("attackDamage");
		if (attackDamageNode == null) throw new ParseException(id, "No attackDamage specified");
		float attackDamage = attackDamageNode.getArgs().get(0).getAsNumberOrElse(1.0).floatValue();
		KDLNode attackSpeedNode = nodes.get("attackSpeed");
		if (attackSpeedNode == null) throw new ParseException(id, "No attackSpeed specified");
		float attackSpeed = attackSpeedNode.getArgs().get(0).getAsNumberOrElse(1.0).floatValue();
		return creator.create(mat, attackDamage, attackSpeed, settings);
	}

	public interface ToolCreator {
		Item create(ToolMaterial material, float attackDamage, float attackSpeed, QuiltItemSettings settings);
	}

	public static class KdlyPickaxe extends PickaxeItem {
		public KdlyPickaxe(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
			super(toolMaterial, (int) attackDamage, attackSpeed, settings);
		}
	}

	public static class KdlyAxe extends AxeItem {
		public KdlyAxe(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
			super(toolMaterial, attackDamage, attackSpeed, settings);
		}
	}

	public static class KdlyHoe extends HoeItem {
		public KdlyHoe(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
			super(toolMaterial, (int)attackDamage, attackSpeed, settings);
		}
	}

	public static class KdlySword extends SwordItem {
		public KdlySword(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, Settings settings) {
			super(toolMaterial, (int)attackDamage, attackSpeed, settings);
		}
	}
}
