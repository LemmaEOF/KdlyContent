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
		float attackDamage = KdlHelper.getArg(attackDamageNode, 0, 1.0f);
		KDLNode attackSpeedNode = nodes.get("attackSpeed");
		if (attackSpeedNode == null) throw new ParseException(id, "No attackSpeed specified");
		float attackSpeed = KdlHelper.getArg(attackSpeedNode, 0, 1.0f);
		return creator.create(mat, attackDamage, attackSpeed, settings);
	}

	public interface ToolCreator {
		Item create(ToolMaterial material, float attackDamage, float attackSpeed, QuiltItemSettings settings);
	}

	//have to exist because these technically take ints for attack damage :<

	public static Item newPick(ToolMaterial material, float attackDamage, float attackSpeed, QuiltItemSettings settings) {
		return new PickaxeItem(material, (int) attackDamage, attackSpeed, settings);
	}

	public static Item newHoe(ToolMaterial material, float attackDamage, float attackSpeed, QuiltItemSettings settings) {
		return new HoeItem(material, (int) attackDamage, attackSpeed, settings);
	}

	public static Item newSword(ToolMaterial material, float attackDamage, float attackSpeed, QuiltItemSettings settings) {
		return new SwordItem(material, (int) attackDamage, attackSpeed, settings);
	}
}
