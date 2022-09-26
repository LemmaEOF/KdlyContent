package gay.lemmaeof.kdlycontent.init;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.util.KdlyTools;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.content.custom.CustomBlockGenerator;
import gay.lemmaeof.kdlycontent.content.type.ArmorMaterialContentType;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class KdlyGenerators {
	public static final BlockGenerator STANDARD_BLOCK = registerBlockGen("standard", (id, settings, customConfig) -> new Block(settings));
	public static final BlockGenerator SLAB = registerBlockGen("slab", (id, settings, customConfig) -> new SlabBlock(settings));
	public static final BlockGenerator STAIRS = registerBlockGen("stair", (id, settings, customConfig) -> {
		KDLNode node = KdlHelper.getChild(customConfig, "parent");
		if (node != null) {
			Block parent = Registry.BLOCK.get(new Identifier(node.getArgs().get(0).getAsString().getValue()));
			return new StairsBlock(parent.getDefaultState(), settings);
		}
		throw new ParseException(id, "No parent block found for stairs");
	});
	public static final BlockGenerator WALL = registerBlockGen("wall", (id, settings, customConfig) -> new WallBlock(settings));
	//TODO: other block presets
	public static final BlockGenerator CUSTOM_BLOCK = registerBlockGen("custom", new CustomBlockGenerator());

	public static final ItemGenerator STANDARD_ITEM = registerItemGen("standard", (id, settings, customConfig) -> new Item(settings));
	public static final ItemGenerator PICKAXE = registerItemGen("pickaxe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyPickaxe::new));
	public static final ItemGenerator AXE = registerItemGen("axe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyAxe::new));
	public static final ItemGenerator SHOVEL = registerItemGen("shovel", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, ShovelItem::new));
	public static final ItemGenerator HOE = registerItemGen("hoe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyHoe::new));
	public static final ItemGenerator SWORD = registerItemGen("sword", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlySword::new));
	public static final ItemGenerator ARMOR = registerItemGen("armor", (id, settings, customConfig) -> {
		KDLNode materialNode = KdlHelper.getChild(customConfig, "material");
		if (materialNode == null) throw new ParseException(id, "No armor material specified");
		KDLNode slotNode = KdlHelper.getChild(customConfig, "equipmentSlot");
		if (slotNode == null) throw new ParseException(id, "No equipmentSlot specified");
		ArmorMaterial armor = ArmorMaterialContentType.getMaterial(materialNode.getArgs().get(0).getAsString().getValue(), id);
		String slotName = slotNode.getArgs().get(0).getAsString().getValue();
		EquipmentSlot slot = switch (slotName) {
			case "head" -> EquipmentSlot.HEAD;
			case "chest" -> EquipmentSlot.CHEST;
			case "legs" -> EquipmentSlot.LEGS;
			case "feet" -> EquipmentSlot.FEET;
			default -> throw new ParseException(id, "Equipment slot not found");
		};
		return new ArmorItem(armor, slot, settings);
	});
	//TODO: custom item

	private static BlockGenerator registerBlockGen(String name, BlockGenerator generator) {
		return Registry.register(KdlyRegistries.BLOCK_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	private static ItemGenerator registerItemGen(String name, ItemGenerator generator) {
		return Registry.register(KdlyRegistries.ITEM_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	public static void init() {}
}
