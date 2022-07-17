package gay.lemmaeof.kdlycontent;

import com.google.common.collect.ImmutableSet;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.parse.KDLParser;
import io.github.cottonmc.staticdata.StaticData;
import io.github.cottonmc.staticdata.StaticDataItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
	private static final Map<String, BlockSoundGroup> SOUND_GROUPS = new HashMap<>();
	private static final Map<String, MapColor> MAP_COLORS = new HashMap<>();

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
		//TODO: custom sound groups and materials
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
				case "slipperiness" -> settings.slipperiness(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "velocityMultiplier" -> settings.velocityMultiplier(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "jumpVelocityMultiplier" -> settings.jumpVelocityMultiplier(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "sounds" -> settings.sounds(SOUND_GROUPS.get(node.getArgs().get(0).getAsString().getValue()));
				case "luminance" -> settings.luminance(node.getArgs().get(0).getAsNumberOrElse(0).intValue());
				case "strength" -> {
					if (node.getArgs().size() == 1) {
						settings.strength(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
					} else {
						settings.strength(node.getArgs().get(0).getAsNumberOrElse(0).floatValue(), node.getArgs().get(1).getAsNumberOrElse(0).floatValue());
					}
				}
				case "breakInstantly" -> settings.breakInstantly();
				//ticks randomly doesn't affect standard `Block`
				//dynamic bounds doesn't affect standard `Block`
				case "dropsNothing" -> settings.dropsNothing();
				case "dropsLike" -> settings.dropsLike(Registry.BLOCK.get(new Identifier(node.getArgs().get(0).getAsString().getValue())));
				case "drops" -> settings.drops(new Identifier(node.getArgs().get(0).getAsString().getValue()));
				case "air" -> settings.air();
				//dynamic luminance, allow spawning, solid block, suffocates, blocks vision, post process, and emmissive lighting too complex to model with kdl for now
				case "requiresTool" -> settings.requiresTool();
				case "mapColor" -> settings.mapColor(MAP_COLORS.get(node.getArgs().get(0).getAsString().getValue()));
				case "hardness" -> settings.hardness(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "resistance" -> settings.resistance(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "collidable" -> settings.collidable(node.getArgs().get(0).getAsBooleanOrElse(true));
				default -> LOGGER.info("Unknown node type {} in kdl for block {}", node.getIdentifier(), id);
			}
		}
		KDLY_BLOCKS.put(id, Registry.register(Registry.BLOCK, id, new Block(settings)));
	}

	private static void parseItem(Identifier id, KDLNode parent) {
		QuiltItemSettings settings = new QuiltItemSettings();
		ItemGroup group = GROUP;
		for (KDLNode node : parent.getChild().orElse(new KDLDocument.Builder().build()).getNodes()) {
			switch (node.getIdentifier()) {
				case "maxCount" -> settings.maxCount(node.getArgs().get(0).getAsNumberOrElse(0).intValue());
				case "maxDamage" -> settings.maxDamage(node.getArgs().get(0).getAsNumberOrElse(0).intValue());
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
				case "group" -> {
					String groupName = node.getArgs().get(0).getAsString().getValue();
					boolean found = false;
					for (ItemGroup g : ItemGroup.GROUPS) {
						if (g.getName().equals(groupName)) {
							group = g;
							found = true;
						}
					}
					if (!found) {
						throw new ParseException(id, "Unknown item group " + groupName);
					}
				}
				case "food" ->
						settings.food(getFoodComponent(id, node.getChild().orElse(new KDLDocument(new ArrayList<>())).getNodes()));
				case "equipmentSlot" -> {
					String slot = node.getArgs().get(0).getAsString().getValue();
					settings.equipmentSlot(switch(slot) {
						case "head" -> EquipmentSlot.HEAD;
						case "chest" -> EquipmentSlot.CHEST;
						case "legs" -> EquipmentSlot.LEGS;
						case "feet" -> EquipmentSlot.FEET;
						default -> throw new ParseException(id, "Unknown equipment slot " + slot);
					});
				}
				default -> LOGGER.info("Unknown node type {} in kdl for item {}", node.getIdentifier(), id);
			}
		}
		settings.group(group);
		//TODO: better solution for block items, this is just a temporary monkeypatch
		if (parent.getProps().containsKey("block")) {
			Block block = Registry.BLOCK.get(new Identifier(parent.getProps().get("block").getAsString().getValue()));
			if (block == Blocks.AIR) throw new ParseException(id, "Attempted to register block item before its corresponding block");
			KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, new BlockItem(block, settings)));
		} else {
			KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, new Item(settings)));
		}
	}

	private static FoodComponent getFoodComponent(Identifier id, List<KDLNode> config) {
		FoodComponent.Builder builder = new FoodComponent.Builder();
		for (KDLNode node : config) {
			switch (node.getIdentifier()) {
				case "hunger" -> builder.hunger(node.getArgs().get(0).getAsNumberOrElse(0).intValue());
				case "saturation" -> builder.saturationModifier(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "meat" -> builder.meat();
				case "alwaysEdible" -> builder.alwaysEdible();
				case "snack" -> builder.snack();
				case "statusEffect" -> {
					Identifier effId = new Identifier(node.getArgs().get(0).getAsString().getValue());
					StatusEffect eff = Registry.STATUS_EFFECT.get(effId);
					if (eff == null) throw new ParseException(id, "Unknown status effect " + effId);
					float chance = getProp(node, "chance", 1f);
					int duration = getProp(node, "duration", 600);
					int amplifier = getProp(node, "amplifier", 0);
					boolean ambient = getProp(node, "ambient", false);
					boolean showParticles = getProp(node, "showParticles", true);
					boolean showIcon = getProp(node, "showIcon", true);
					builder.statusEffect(new StatusEffectInstance(eff, duration, amplifier, ambient, showParticles, showIcon), chance);
				}
			}
		}
		return builder.build();
	}

	private static float getProp(KDLNode node, String property, float defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsNumberOrElse(defaultValue).floatValue();
		}
		return defaultValue;
	}

	private static boolean getProp(KDLNode node, String property, boolean defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsBooleanOrElse(defaultValue);
		}
		return defaultValue;
	}

	private static int getProp(KDLNode node, String property, int defaultValue) {
		if (node.getProps().containsKey(property)) {
			return node.getProps().get(property).getAsNumberOrElse(defaultValue).intValue();
		}
		return defaultValue;
	}

	public static class ParseException extends RuntimeException {
		public ParseException(Identifier id, String message) {
			super("Error parsing KDL for " + id.toString() + ": " + message);
		}
	}

	static {
		//if you appreciate the work I've put in transcribing all of this *by hand*,
		//please support me on ko-fi! https://ko-fi.com/LemmaEOF

		//Materials
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

		//Block sound groups
		SOUND_GROUPS.put("wood", BlockSoundGroup.WOOD);
		SOUND_GROUPS.put("gravel", BlockSoundGroup.GRAVEL);
		SOUND_GROUPS.put("grass", BlockSoundGroup.GRASS);
		SOUND_GROUPS.put("lily_pad", BlockSoundGroup.LILY_PAD);
		SOUND_GROUPS.put("stone", BlockSoundGroup.STONE);
		SOUND_GROUPS.put("metal", BlockSoundGroup.METAL);
		SOUND_GROUPS.put("glass", BlockSoundGroup.GLASS);
		SOUND_GROUPS.put("wool", BlockSoundGroup.WOOL);
		SOUND_GROUPS.put("sand", BlockSoundGroup.SAND);
		SOUND_GROUPS.put("snow", BlockSoundGroup.SNOW);
		SOUND_GROUPS.put("powder_snow", BlockSoundGroup.POWDER_SNOW);
		SOUND_GROUPS.put("ladder", BlockSoundGroup.LADDER);
		SOUND_GROUPS.put("anvil", BlockSoundGroup.ANVIL);
		SOUND_GROUPS.put("slime", BlockSoundGroup.SLIME);
		SOUND_GROUPS.put("honey", BlockSoundGroup.HONEY);
		SOUND_GROUPS.put("wet_grass", BlockSoundGroup.WET_GRASS);
		SOUND_GROUPS.put("coral", BlockSoundGroup.CORAL);
		SOUND_GROUPS.put("bamoo", BlockSoundGroup.BAMBOO);
		SOUND_GROUPS.put("bamoo_sapling", BlockSoundGroup.BAMBOO_SAPLING);
		SOUND_GROUPS.put("scaffolding", BlockSoundGroup.SCAFFOLDING);
		SOUND_GROUPS.put("sweet_berry_bush", BlockSoundGroup.SWEET_BERRY_BUSH);
		SOUND_GROUPS.put("crop", BlockSoundGroup.CROP);
		SOUND_GROUPS.put("stem", BlockSoundGroup.STEM);
		SOUND_GROUPS.put("vine", BlockSoundGroup.VINE);
		SOUND_GROUPS.put("nether_wart", BlockSoundGroup.NETHER_WART);
		SOUND_GROUPS.put("lantern", BlockSoundGroup.LANTERN);
		SOUND_GROUPS.put("nether_stem", BlockSoundGroup.NETHER_STEM);
		SOUND_GROUPS.put("nylium", BlockSoundGroup.NYLIUM);
		SOUND_GROUPS.put("fungus", BlockSoundGroup.FUNGUS);
		SOUND_GROUPS.put("roots", BlockSoundGroup.ROOTS);
		SOUND_GROUPS.put("shroomlight", BlockSoundGroup.SHROOMLIGHT);
		SOUND_GROUPS.put("weeping_vines", BlockSoundGroup.WEEPING_VINES);
		SOUND_GROUPS.put("weeping_vines_low_pitch", BlockSoundGroup.WEEPING_VINES_LOW_PITCH); //uh
		SOUND_GROUPS.put("soul_sand", BlockSoundGroup.SOUL_SAND);
		SOUND_GROUPS.put("soul_soil", BlockSoundGroup.SOUL_SOIL);
		SOUND_GROUPS.put("basalt", BlockSoundGroup.BASALT);
		SOUND_GROUPS.put("wart_block", BlockSoundGroup.WART_BLOCK);
		SOUND_GROUPS.put("netherrack", BlockSoundGroup.NETHERRACK);
		SOUND_GROUPS.put("nether_bricks", BlockSoundGroup.NETHER_BRICKS);
		SOUND_GROUPS.put("nether_sprouts", BlockSoundGroup.NETHER_SPROUTS);
		SOUND_GROUPS.put("nether_ore", BlockSoundGroup.NETHER_ORE);
		SOUND_GROUPS.put("bone", BlockSoundGroup.BONE);
		SOUND_GROUPS.put("netherite", BlockSoundGroup.NETHERITE);
		SOUND_GROUPS.put("ancient_debris", BlockSoundGroup.ANCIENT_DEBRIS);
		SOUND_GROUPS.put("lodestone", BlockSoundGroup.LODESTONE);
		SOUND_GROUPS.put("chain", BlockSoundGroup.CHAIN);
		SOUND_GROUPS.put("nether_gold_ore", BlockSoundGroup.NETHER_GOLD_ORE);
		SOUND_GROUPS.put("gilded_blackstone", BlockSoundGroup.GILDED_BLACKSTONE);
		SOUND_GROUPS.put("candle", BlockSoundGroup.CANDLE);
		SOUND_GROUPS.put("amethyst_block", BlockSoundGroup.AMETHYST_BLOCK);
		SOUND_GROUPS.put("amethyst_cluster", BlockSoundGroup.AMETHYST_CLUSTER);
		SOUND_GROUPS.put("small_amethyst_bud", BlockSoundGroup.SMALL_AMETHYST_BUD);
		SOUND_GROUPS.put("medium_amethyst_bud", BlockSoundGroup.MEDIUM_AMETHYST_BUD);
		SOUND_GROUPS.put("large_amethyst_bud", BlockSoundGroup.LARGE_AMETHYST_BUD);
		SOUND_GROUPS.put("tuff", BlockSoundGroup.TUFF);
		SOUND_GROUPS.put("calcite", BlockSoundGroup.CALCITE);
		SOUND_GROUPS.put("dripstone_block", BlockSoundGroup.DRIPSTONE_BLOCK);
		SOUND_GROUPS.put("pointed_dripstone", BlockSoundGroup.POINTED_DRIPSTONE);
		SOUND_GROUPS.put("copper", BlockSoundGroup.COPPER);
		SOUND_GROUPS.put("cave_vines", BlockSoundGroup.CAVE_VINES);
		SOUND_GROUPS.put("spore_blossom", BlockSoundGroup.SPORE_BLOSSOM);
		SOUND_GROUPS.put("azalea", BlockSoundGroup.AZALEA);
		SOUND_GROUPS.put("flowering_azalea", BlockSoundGroup.FLOWERING_AZALEA);
		SOUND_GROUPS.put("moss_carpet", BlockSoundGroup.MOSS_CARPET);
		SOUND_GROUPS.put("moss_block", BlockSoundGroup.MOSS_BLOCK);
		SOUND_GROUPS.put("big_dripleaf", BlockSoundGroup.BIG_DRIPLEAF);
		SOUND_GROUPS.put("small_dripleaf", BlockSoundGroup.SMALL_DRIPLEAF);
		SOUND_GROUPS.put("rooted_dirt", BlockSoundGroup.ROOTED_DIRT);
		SOUND_GROUPS.put("hanging_roots", BlockSoundGroup.HANGING_ROOTS);
		SOUND_GROUPS.put("azalea_leaves", BlockSoundGroup.AZALEA_LEAVES);
		SOUND_GROUPS.put("sculk_sensor", BlockSoundGroup.SCULK_SENSOR);
		SOUND_GROUPS.put("sculk_catalyst", BlockSoundGroup.SCULK_CATALYST);
		SOUND_GROUPS.put("sculk", BlockSoundGroup.SCULK);
		SOUND_GROUPS.put("sculk_vein", BlockSoundGroup.SCULK_VEIN);
		SOUND_GROUPS.put("sculk_shrieker", BlockSoundGroup.SCULK_SHRIEKER);
		SOUND_GROUPS.put("glow_lichen", BlockSoundGroup.GLOW_LICHEN);
		SOUND_GROUPS.put("deepslate", BlockSoundGroup.DEEPSLATE);
		SOUND_GROUPS.put("deepslate_bricks", BlockSoundGroup.DEEPSLATE_BRICKS);
		SOUND_GROUPS.put("deepslate_tiles", BlockSoundGroup.DEEPSLATE_TILES);
		SOUND_GROUPS.put("polished_deepslate", BlockSoundGroup.POLISHED_DEEPSLATE);
		SOUND_GROUPS.put("froglight", BlockSoundGroup.FROGLIGHT);
		SOUND_GROUPS.put("frogspawn", BlockSoundGroup.FROGSPAWN);
		SOUND_GROUPS.put("mangrove_roots", BlockSoundGroup.MANGROVE_ROOTS);
		SOUND_GROUPS.put("muddy_mangrove_roots", BlockSoundGroup.MUDDY_MANGROVE_ROOTS);
		SOUND_GROUPS.put("mud", BlockSoundGroup.MUD);
		SOUND_GROUPS.put("mud_bricks", BlockSoundGroup.MUD_BRICKS);
		SOUND_GROUPS.put("packed_mud", BlockSoundGroup.PACKED_MUD);

		//Map colors
		MAP_COLORS.put("clear", MapColor.CLEAR);
		MAP_COLORS.put("pale_green", MapColor.PALE_GREEN);
		MAP_COLORS.put("pale_yellow", MapColor.PALE_YELLOW);
		MAP_COLORS.put("white_gray", MapColor.WHITE);
		MAP_COLORS.put("bright_red", MapColor.BRIGHT_RED);
		MAP_COLORS.put("pale_purple", MapColor.PALE_PURPLE);
		MAP_COLORS.put("iron_gray", MapColor.IRON_GRAY);
		MAP_COLORS.put("dark_green", MapColor.DARK_GREEN);
		MAP_COLORS.put("white", MapColor.WHITE);
		MAP_COLORS.put("light_blue_gray", MapColor.LIGHT_BLUE_GRAY);
		MAP_COLORS.put("dirt_brown", MapColor.DIRT_BROWN);
		MAP_COLORS.put("stone_gray", MapColor.STONE_GRAY);
		MAP_COLORS.put("water_blue", MapColor.WATER_BLUE);
		MAP_COLORS.put("oak_tan", MapColor.OAK_TAN);
		MAP_COLORS.put("off_white", MapColor.OFF_WHITE);
		MAP_COLORS.put("orange", MapColor.ORANGE);
		MAP_COLORS.put("magenta", MapColor.MAGENTA);
		MAP_COLORS.put("light_blue", MapColor.LIGHT_BLUE);
		MAP_COLORS.put("yellow", MapColor.YELLOW);
		MAP_COLORS.put("lime", MapColor.LIME);
		MAP_COLORS.put("pink", MapColor.PINK);
		MAP_COLORS.put("gray", MapColor.GRAY);
		MAP_COLORS.put("light_gray", MapColor.LIGHT_GRAY);
		MAP_COLORS.put("cyan", MapColor.CYAN);
		MAP_COLORS.put("purple", MapColor.PURPLE);
		MAP_COLORS.put("blue", MapColor.BLUE);
		MAP_COLORS.put("brown", MapColor.BROWN);
		MAP_COLORS.put("green", MapColor.GREEN);
		MAP_COLORS.put("red", MapColor.RED);
		MAP_COLORS.put("black", MapColor.BLACK);
		MAP_COLORS.put("gold", MapColor.GOLD);
		MAP_COLORS.put("diamond_blue", MapColor.DIAMOND_BLUE);
		MAP_COLORS.put("lapis_blue", MapColor.LAPIS_BLUE);
		MAP_COLORS.put("emerald_green", MapColor.EMERALD_GREEN);
		MAP_COLORS.put("spruce_brown", MapColor.SPRUCE_BROWN);
		MAP_COLORS.put("dark_red", MapColor.DARK_RED);
		MAP_COLORS.put("terracotta_white", MapColor.TERRACOTTA_WHITE);
		MAP_COLORS.put("terracotta_orange", MapColor.TERRACOTTA_ORANGE);
		MAP_COLORS.put("terracotta_magenta", MapColor.TERRACOTTA_MAGENTA);
		MAP_COLORS.put("terracotta_light_blue", MapColor.TERRACOTTA_LIGHT_BLUE);
		MAP_COLORS.put("terracotta_yellow", MapColor.TERRACOTTA_YELLOW);
		MAP_COLORS.put("terracotta_lime", MapColor.TERRACOTTA_LIME);
		MAP_COLORS.put("terracotta_pink", MapColor.TERRACOTTA_PINK);
		MAP_COLORS.put("terracotta_gray", MapColor.TERRACOTTA_GRAY);
		MAP_COLORS.put("terracotta_light_gray", MapColor.TERRACOTTA_LIGHT_GRAY);
		MAP_COLORS.put("terracotta_cyan", MapColor.TERRACOTTA_CYAN);
		MAP_COLORS.put("terracotta_purple", MapColor.TERRACOTTA_PURPLE);
		MAP_COLORS.put("terracotta_blue", MapColor.TERRACOTTA_BLUE);
		MAP_COLORS.put("terracotta_brown", MapColor.TERRACOTTA_BROWN);
		MAP_COLORS.put("terracotta_green", MapColor.TERRACOTTA_GREEN);
		MAP_COLORS.put("terracotta_red", MapColor.TERRACOTTA_RED);
		MAP_COLORS.put("terracotta_black", MapColor.TERRACOTTA_BLACK);
		MAP_COLORS.put("dull_red", MapColor.DULL_RED);
		MAP_COLORS.put("dull_pink", MapColor.DULL_PINK);
		MAP_COLORS.put("dark_crimson", MapColor.DARK_CRIMSON);
		MAP_COLORS.put("teal", MapColor.TEAL);
		MAP_COLORS.put("dark_aqua", MapColor.DARK_AQUA);
		MAP_COLORS.put("dark_dull_pink", MapColor.DARK_DULL_PINK);
		MAP_COLORS.put("bright_teal", MapColor.BRIGHT_TEAL);
		MAP_COLORS.put("deepslate_gray", MapColor.DEEPSLATE_GRAY);
		MAP_COLORS.put("raw_iron_pink", MapColor.RAW_IRON_PINK);
		MAP_COLORS.put("lichen_green", MapColor.LICHEN_GREEN);
	}
}
