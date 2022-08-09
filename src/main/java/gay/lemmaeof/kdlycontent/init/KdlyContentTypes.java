package gay.lemmaeof.kdlycontent.init;

import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.content.type.ArmorMaterialContentType;
import gay.lemmaeof.kdlycontent.content.type.BlockContentType;
import gay.lemmaeof.kdlycontent.content.type.ItemContentType;
import gay.lemmaeof.kdlycontent.content.type.ToolMaterialContentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class KdlyContentTypes {

	public static final ContentType BLOCK = register("block", new BlockContentType());
	public static final ContentType ITEM = register("item", new ItemContentType());
	public static final ContentType TOOL_MATERIAL = register("tool_material", new ToolMaterialContentType());
	public static final ContentType ARMOR_MATERIAL = register("armor_material", new ArmorMaterialContentType());

	private static ContentType register(String name, ContentType type) {
		return Registry.register(KdlyRegistries.CONTENT_TYPES, new Identifier(KdlyContent.MODID, name), type);
	}

	public static void init() {}
}
