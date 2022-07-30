package gay.lemmaeof.kdlycontent.init;

import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.KdlHelper;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.KdlyTools;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
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

	//TODO: /armor materials
	public static final ItemGenerator STANDARD_ITEM = registerItemGen("standard", (id, settings, customConfig) -> new Item(settings));
	public static final ItemGenerator PICKAXE = registerItemGen("pickaxe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyPickaxe::new));
	public static final ItemGenerator AXE = registerItemGen("axe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyAxe::new));
	public static final ItemGenerator SHOVEL = registerItemGen("shovel", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, ShovelItem::new));
	public static final ItemGenerator HOE = registerItemGen("hoe", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlyHoe::new));
	public static final ItemGenerator SWORD = registerItemGen("sword", (id, settings, customConfig) -> KdlyTools.construct(id, settings, customConfig, KdlyTools.KdlySword::new));

	private static BlockGenerator registerBlockGen(String name, BlockGenerator generator) {
		return Registry.register(KdlyRegistries.BLOCK_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	private static ItemGenerator registerItemGen(String name, ItemGenerator generator) {
		return Registry.register(KdlyRegistries.ITEM_GENERATORS, new Identifier(KdlyContent.MODID, name), generator);
	}

	public static void init() {}
}
