package gay.lemmaeof.kdlycontent.init;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.BlockParser;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.custom.*;
import gay.lemmaeof.kdlycontent.content.type.ArmorMaterialContentType;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.item.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class KdlyGenerators {
	public static final BlockParser PASSTHROUGH_BLOCK = registerBlockParser("passthrough", new PassthroughBlockParser());
	public static final BlockParser CUSTOM_BLOCK = registerBlockParser("custom", new CustomBlockParser());

	public static final ItemGenerator STANDARD_ITEM = registerItemGen("standard", (id, settings, customConfig) -> new Item(settings));
	public static final ItemGenerator PICKAXE = registerItemGen("pickaxe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newPick));
	public static final ItemGenerator AXE = registerItemGen("axe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, AxeItem::new));
	public static final ItemGenerator SHOVEL = registerItemGen("shovel", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, ShovelItem::new));
	public static final ItemGenerator HOE = registerItemGen("hoe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newHoe));
	public static final ItemGenerator SWORD = registerItemGen("sword", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newSword));
	public static final ItemGenerator ARMOR = registerItemGen("armor", (id, settings, customConfig) -> {
		KdlNode materialNode = KdlHelper.getChild(customConfig, "material");
		if (materialNode == null) throw new ParseException(id, "No armor material specified");
		KdlNode slotNode = KdlHelper.getChild(customConfig, "equipment_slot");
		if (slotNode == null) throw new ParseException(id, "No equipment_slot specified");
		RegistryEntry<ArmorMaterial> armor = ArmorMaterialContentType.getMaterial(KdlHelper.getArg(materialNode, 0, "diamond"), id);
		String slotName = KdlHelper.getArg(slotNode, 0, "head");
		ArmorItem.Type slot = switch (slotName) {
			case "head" -> ArmorItem.Type.HELMET;
			case "chest" -> ArmorItem.Type.CHESTPLATE;
			case "legs" -> ArmorItem.Type.LEGGINGS;
			case "feet" -> ArmorItem.Type.BOOTS;
			default -> throw new ParseException(id, "Equipment slot not found");
		};
		return new ArmorItem(armor, slot, settings);
	});
	public static final ItemGenerator CUSTOM_ITEM = registerItemGen("custom", new CustomItemGenerator());
	public static final ItemGenerator CUSTOM_TOOL = registerItemGen("custom_tool", new CustomToolItemGenerator());
	public static final ItemGenerator CUSTOM_SWORD = registerItemGen("custom_sword", new CustomSwordItemGenerator());

	private static BlockParser registerBlockParser(String name, BlockParser generator) {
		return Registry.register(KdlyRegistries.BLOCK_PARSERS, Identifier.of(KdlyContent.MODID, name), generator);
	}

	private static ItemGenerator registerItemGen(String name, ItemGenerator generator) {
		return Registry.register(KdlyRegistries.ITEM_GENERATORS, Identifier.of(KdlyContent.MODID, name), generator);
	}

	public static void init() {}
}
