package gay.lemmaeof.kdlycontent.init;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.custom.CustomBlockGenerator;
import gay.lemmaeof.kdlycontent.content.custom.CustomItemGenerator;
import gay.lemmaeof.kdlycontent.content.custom.CustomSwordItemGenerator;
import gay.lemmaeof.kdlycontent.content.custom.CustomToolItemGenerator;
import gay.lemmaeof.kdlycontent.content.type.ArmorMaterialContentType;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class KdlyGenerators {
	public static final BlockGenerator STANDARD_BLOCK = registerBlockGen("standard", (id, settings, customConfig) -> new Block(settings));
	public static final BlockGenerator SLAB = registerBlockGen("slab", (id, settings, customConfig) -> new SlabBlock(settings));
	public static final BlockGenerator STAIRS = registerBlockGen("stair", (id, settings, customConfig) -> {
		KDLNode node = KdlHelper.getChild(customConfig, "parent");
		if (node != null) {
			Block parent = Registries.BLOCK.get(new Identifier(KdlHelper.getArg(node, 0, "air")));
			return new StairsBlock(parent.getDefaultState(), settings);
		}
		throw new ParseException(id, "No parent block found for stairs");
	});
	public static final BlockGenerator WALL = registerBlockGen("wall", (id, settings, customConfig) -> new WallBlock(settings));
	//TODO: other block presets
	public static final BlockGenerator CUSTOM_BLOCK = registerBlockGen("custom", new CustomBlockGenerator());

	public static final ItemGenerator STANDARD_ITEM = registerItemGen("standard", (id, settings, customConfig) -> new Item(settings));
	public static final ItemGenerator PICKAXE = registerItemGen("pickaxe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newPick));
	public static final ItemGenerator AXE = registerItemGen("axe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, AxeItem::new));
	public static final ItemGenerator SHOVEL = registerItemGen("shovel", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, ShovelItem::new));
	public static final ItemGenerator HOE = registerItemGen("hoe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newHoe));
	public static final ItemGenerator SWORD = registerItemGen("sword", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools::newSword));
	public static final ItemGenerator ARMOR = registerItemGen("armor", (id, settings, customConfig) -> {
		KDLNode materialNode = KdlHelper.getChild(customConfig, "material");
		if (materialNode == null) throw new ParseException(id, "No armor material specified");
		KDLNode slotNode = KdlHelper.getChild(customConfig, "equipmentSlot");
		if (slotNode == null) throw new ParseException(id, "No equipmentSlot specified");
		ArmorMaterial armor = ArmorMaterialContentType.getMaterial(KdlHelper.getArg(materialNode, 0, "diamond"), id);
		String slotName = KdlHelper.getArg(slotNode, 0, "head");
		ArmorItem.ArmorSlot slot = switch (slotName) {
			case "head" -> ArmorItem.ArmorSlot.HELMET;
			case "chest" -> ArmorItem.ArmorSlot.CHESTPLATE;
			case "legs" -> ArmorItem.ArmorSlot.LEGGINGS;
			case "feet" -> ArmorItem.ArmorSlot.BOOTS;
			default -> throw new ParseException(id, "Equipment slot not found");
		};
		return new ArmorItem(armor, slot, settings);
	});
	public static final ItemGenerator CUSTOM_ITEM = registerItemGen("custom", new CustomItemGenerator());
	public static final ItemGenerator CUSTOM_TOOL = registerItemGen("custom_tool", new CustomToolItemGenerator());
	public static final ItemGenerator CUSTOM_SWORD = registerItemGen("custom_sword", new CustomSwordItemGenerator());

	private static BlockGenerator registerBlockGen(String name, BlockGenerator generator) {
		return Registry.register(KdlyRegistries.BLOCK_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	private static ItemGenerator registerItemGen(String name, ItemGenerator generator) {
		return Registry.register(KdlyRegistries.ITEM_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	public static void init() {}
}
