package gay.lemmaeof.kdlycontent.util;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.type.ToolMaterialContentType;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class KdlyTools {

	public static Item construct(Identifier id, Item.Settings settings, List<KdlNode> customConfig, ToolCreator creator) {
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(customConfig);
		//TODO: ability to define material in-line
		KdlNode materialNode = nodes.get("material");
		if (materialNode == null) throw new ParseException(id, "No material specified");
		String matId = String.valueOf(materialNode.arguments().getFirst().value());
		ToolMaterial mat = ToolMaterialContentType.getMaterial(matId, id);
		return creator.create(mat, settings);
	}

	public interface ToolCreator {
		Item create(ToolMaterial material, Item.Settings settings);
	}

	//have to exist because these technically take ints for attack damage :<

	public static Item newPick(ToolMaterial material, Item.Settings settings) {
		return new PickaxeItem(material, settings);
	}

	public static Item newHoe(ToolMaterial material, Item.Settings settings) {
		return new HoeItem(material, settings);
	}

	public static Item newSword(ToolMaterial material, Item.Settings settings) {
		return new SwordItem(material, settings);
	}
}
