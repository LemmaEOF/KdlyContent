package gay.lemmaeof.kdlycontent;

import com.google.common.collect.ImmutableSet;
import dev.hbeck.kdl.objects.*;
import dev.hbeck.kdl.parse.KDLParser;
import io.github.cottonmc.staticdata.StaticData;
import io.github.cottonmc.staticdata.StaticDataItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.Material;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KdlyContent implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("KdlyContent");
	public static final Map<Identifier, Block> KDLY_BLOCKS = new HashMap<>();
	public static final Map<Identifier, Item> KDLY_ITEMS = new HashMap<>();
	private static final KDLParser parser = new KDLParser();

	private static final Map<String, Material> MATERIALS = new HashMap<>();

	public static final ItemGroup GROUP = QuiltItemGroup.createWithIcon(new Identifier("kdlycontent", "generated"), () -> new ItemStack(Items.CRAFTING_TABLE));

	@Override
	public void onInitialize(ModContainer mod) {
		ImmutableSet<StaticDataItem> data = StaticData.getAll("kdlycontent.kdl");
		for (StaticDataItem item : data) {
			if (item.getIdentifier().getPath().endsWith(".kdl")) {
				String namespace = item.getIdentifier().getNamespace();
				if (namespace.equals(StaticData.GLOBAL_DATA_NAMESPACE)) namespace = "kdlycontent";
				try {
					KDLDocument kdl = parser.parse(item.createInputStream());
					for (KDLNode node : kdl.getNodes()) {
						String name = node.getArgs().get(0).getAsString().getValue();
						Identifier id = new Identifier(namespace, name);
						switch (node.getIdentifier()) {
							case "item" -> parseItem(id, node);
							case "block" -> parseBlock(id, node);
							//TODO: other types?
						}
					}
				} catch (IOException | ParseException e) {
					throw new RuntimeException("Could not load KDL for file " + item.getIdentifier(), e);
				}
			}
		}
		LOGGER.info("Registered {} block{} and {} item{}", KDLY_BLOCKS.size(), KDLY_BLOCKS.size() == 1? "" : "s", KDLY_ITEMS.size(), KDLY_ITEMS.size() == 1? "" : "s");
	}

	private static void parseBlock(Identifier id, KDLNode parent) {
		QuiltBlockSettings settings;
		if (parent.getProps().containsKey("material")) {
			settings = QuiltBlockSettings.of(MATERIALS.get(parent.getProps().get("material").getAsString().getValue()));
		} else if (parent.getProps().containsKey("copy")) {
			settings = QuiltBlockSettings.copyOf(Registry.BLOCK.get(new Identifier(parent.getProps().get("copy").getAsString().getValue())));
		} else {
			throw new ParseException(id, "No material or block to copy from");
		}
		for (KDLNode node : parent.getChild().orElse(new KDLDocument.Builder().build()).getNodes()) {
			switch (node.getIdentifier()) {
				case "noCollision" -> settings.noCollision();
				case "nonOpaque" -> settings.nonOpaque();
				case "slipperiness" -> settings.slipperiness(getFloat(node.getArgs().get(0)));
				case "velocityMultiplier" -> settings.velocityMultiplier(getFloat(node.getArgs().get(0)));
				case "jumpVelocityMultiplier" -> settings.jumpVelocityMultiplier(getFloat(node.getArgs().get(0)));
				//TODO: sounds
				case "luminance" -> settings.luminance(getInt(node.getArgs().get(0)));
				case "strength" -> {
					if (node.getArgs().size() == 1) {
						settings.strength(getFloat(node.getArgs().get(0)));
					} else {
						settings.strength(getFloat(node.getArgs().get(0)), getFloat(node.getArgs().get(1)));
					}
				}
				case "breakInstantly" -> settings.breakInstantly();
				//ticks randomly doesn't affect standard `Block`
				//dynamic bounds doesn't affect standard `Block`
				case "dropsNothing" -> settings.dropsNothing();
				case "dropsLike" -> settings.dropsLike(Registry.BLOCK.get(new Identifier(node.getArgs().get(0).getAsString().getValue())));
				case "drops" -> settings.drops(new Identifier(node.getArgs().get(0).getAsString().getValue()));
				case "air" -> settings.air();
				//luminance, allow spawning, solid block, suffocates, blocks vision, post process, and emmissive lighting too complex to model with kdl for now
				case "requiresTool" -> settings.requiresTool();
				//TODO: map color, dye color
				case "hardness" -> settings.hardness(getFloat(node.getArgs().get(0)));
				case "resistance" -> settings.resistance(getFloat(node.getArgs().get(0)));
				case "collidable" -> settings.collidable(getBoolean(node.getArgs().get(0)));
				default -> LOGGER.info("Unknown node type {} in kdl for block {}", node.getIdentifier(), id);
			}
		}
		KDLY_BLOCKS.put(id, Registry.register(Registry.BLOCK, id, new Block(settings)));
	}

	private static void parseItem(Identifier id, KDLNode parent) {
		//TODO: change item group?
		//TODO: food
		//TODO: better solution for block items, this is just a temporary monkeypatch
		Item.Settings settings = new Item.Settings();
		for (KDLNode node : parent.getChild().orElse(new KDLDocument.Builder().build()).getNodes()) {
			switch (node.getIdentifier()) {
				case "maxCount" -> settings.maxCount(getInt(node.getArgs().get(0)));
				case "maxDamage" -> settings.maxDamage(getInt(node.getArgs().get(0)));
				case "recipeRemainder" ->
						settings.recipeRemainder(Registry.ITEM.get(new Identifier(node.getArgs().get(0).getAsString().getValue())));
				case "rarity" -> {
					String rarity = node.getArgs().get(0).getAsString().getValue();
					settings.rarity(switch(rarity) {
						case "common" -> Rarity.COMMON;
						case "uncommon" -> Rarity.UNCOMMON;
						case "rare" -> Rarity.RARE;
						case "epic" -> Rarity.EPIC;
						default -> throw new ParseException(id, "Unknown rarity " + rarity);
					});
				}
				case "fireproof" -> settings.fireproof();
				default -> LOGGER.info("Unknown node type {} in kdl for item {}", node.getIdentifier(), id);
			}
		}
		settings.group(GROUP);
		if (parent.getProps().containsKey("block")) {
			Block block = Registry.BLOCK.get(new Identifier(parent.getProps().get("block").getAsString().getValue()));
			if (block == Blocks.AIR) throw new ParseException(id, "Attempted to register block item before its corresponding block");
			KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, new BlockItem(block, settings)));
		} else {
			KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, new Item(settings)));
		}
	}

	private static int getInt(KDLValue<?> value) {
		return value.getAsNumber().orElse(KDLNumber.from(0)).getValue().intValue();
	}

	private static float getFloat(KDLValue<?> value) {
		return value.getAsNumber().orElse(KDLNumber.from(0)).getValue().floatValue();
	}

	private static boolean getBoolean(KDLValue<?> value) {
		return value.getAsBoolean().orElse(new KDLBoolean(false)).getValue();
	}

	public static class ParseException extends RuntimeException {
		public ParseException(Identifier id, String message) {
			super("Error parsing KDL for " + id.toString() + ": " + message);
		}
	}

	static {
		//if you appreciate the work I've put in transcribing all of this *by hand*,
		//please support me on ko-fi! https://ko-fi.com/LemmaEOF
		MATERIALS.put("air", Material.AIR);
		MATERIALS.put("structure_void", Material.STRUCTURE_VOID);
		MATERIALS.put("portal", Material.PORTAL);
		MATERIALS.put("carpet", Material.CARPET);
		MATERIALS.put("plant", Material.PLANT);
		MATERIALS.put("underwater_plant", Material.UNDERWATER_PLANT);
		MATERIALS.put("replaceable_plant", Material.REPLACEABLE_PLANT);
		MATERIALS.put("nether_shoots", Material.NETHER_SHOOTS);
		MATERIALS.put("replaceable_underwater_plant", Material.REPLACEABLE_UNDERWATER_PLANT);
		MATERIALS.put("water", Material.WATER);
		MATERIALS.put("bubble_column", Material.BUBBLE_COLUMN);
		MATERIALS.put("lava", Material.LAVA);
		MATERIALS.put("snow_layer", Material.SNOW_LAYER);
		MATERIALS.put("fire", Material.FIRE);
		MATERIALS.put("decoration", Material.DECORATION);
		MATERIALS.put("cobweb", Material.COBWEB);
		MATERIALS.put("sculk", Material.SCULK);
		MATERIALS.put("redstone_lamp", Material.REDSTONE_LAMP);
		MATERIALS.put("organic_product", Material.ORGANIC_PRODUCT);
		MATERIALS.put("soil", Material.SOIL);
		MATERIALS.put("solid_organic", Material.SOLID_ORGANIC);
		MATERIALS.put("dense_ice", Material.DENSE_ICE);
		MATERIALS.put("aggregate", Material.AGGREGATE);
		MATERIALS.put("sponge", Material.SPONGE);
		MATERIALS.put("shulker_box", Material.SHULKER_BOX);
		MATERIALS.put("wood", Material.WOOD);
		MATERIALS.put("nether_wood", Material.NETHER_WOOD);
		MATERIALS.put("bamboo_sapling", Material.BAMBOO_SAPLING);
		MATERIALS.put("bamboo", Material.BAMBOO);
		MATERIALS.put("wool", Material.WOOL);
		MATERIALS.put("tnt", Material.TNT);
		MATERIALS.put("leaves", Material.LEAVES);
		MATERIALS.put("glass", Material.GLASS);
		MATERIALS.put("ice", Material.ICE);
		MATERIALS.put("cactus", Material.CACTUS);
		MATERIALS.put("stone", Material.STONE);
		MATERIALS.put("metal", Material.METAL);
		MATERIALS.put("snow_block", Material.SNOW_BLOCK);
		MATERIALS.put("repair_station", Material.REPAIR_STATION);
		MATERIALS.put("barrier", Material.BARRIER);
		MATERIALS.put("piston", Material.PISTON);
		MATERIALS.put("moss_block", Material.MOSS_BLOCK);
		MATERIALS.put("gourd", Material.GOURD);
		MATERIALS.put("egg", Material.EGG);
		MATERIALS.put("cake", Material.CAKE);
		MATERIALS.put("amethyst", Material.AMETHYST);
		MATERIALS.put("powder_snow", Material.POWDER_SNOW);
		MATERIALS.put("frog_spawn", Material.FROG_SPAWN);
		MATERIALS.put("froglight", Material.FROGLIGHT);
	}
}
