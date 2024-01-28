package gay.lemmaeof.kdlycontent.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.sound.BlockSoundGroup;

import java.util.HashMap;
import java.util.Map;

public class NamedProperties {

	public static final Map<String, BlockSoundGroup> SOUND_GROUPS = new HashMap<>();
	public static final Map<String, MapColor> MAP_COLORS = new HashMap<>();
	public static final Map<String, PistonBehavior> PISTON_BEHAVIORS = new HashMap<>();
	public static final Map<String, AbstractBlock.OffsetType> OFFSET_TYPES = new HashMap<>();
	public static final Map<String, NoteBlockInstrument> INSTRUMENTS = new HashMap<>();

	static {
		//if you appreciate the work I've put in transcribing all of this *by hand*,
		//please support me on ko-fi! https://ko-fi.com/LemmaEOF

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
		SOUND_GROUPS.put("bamboo", BlockSoundGroup.BAMBOO);
		SOUND_GROUPS.put("bamboo_sapling", BlockSoundGroup.BAMBOO_SAPLING);
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
		SOUND_GROUPS.put("low_pitch_weeping_vines", BlockSoundGroup.LOW_PITCH_WEEPING_VINES); //uh
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
		SOUND_GROUPS.put("hanging_sign", BlockSoundGroup.HANGING_SIGN);
		SOUND_GROUPS.put("nether_hanging_sign", BlockSoundGroup.NETHER_HANGING_SIGN);
		SOUND_GROUPS.put("bamboo_hanging_sign", BlockSoundGroup.BAMBOO_HANGING_SIGN);
		SOUND_GROUPS.put("bamboo_wood", BlockSoundGroup.BAMBOO_WOOD);
		SOUND_GROUPS.put("nether_wood", BlockSoundGroup.NETHER_WOOD);
		SOUND_GROUPS.put("cherry_wood", BlockSoundGroup.CHERRY_WOOD);
		SOUND_GROUPS.put("cherry_sapling", BlockSoundGroup.CHERRY_SAPLING);
		SOUND_GROUPS.put("cherry_leaves", BlockSoundGroup.CHERRY_LEAVES);
		SOUND_GROUPS.put("cherry_hanging_sign", BlockSoundGroup.CHERRY_HANGING_SIGN);
		SOUND_GROUPS.put("chiseled_bookshelf", BlockSoundGroup.CHISELED_BOOKSHELF);
		SOUND_GROUPS.put("suspicious_sand", BlockSoundGroup.SUSPICIOUS_SAND);
		SOUND_GROUPS.put("suspicious_gravel", BlockSoundGroup.SUSPICIOUS_GRAVEL);
		SOUND_GROUPS.put("decorated_pot", BlockSoundGroup.DECORATED_POT);
		SOUND_GROUPS.put("cracked_decorated_pot", BlockSoundGroup.CRACKED_DECORATED_POT);

		//Map colors
		MAP_COLORS.put("none", MapColor.NONE);
		MAP_COLORS.put("grass", MapColor.GRASS);
		MAP_COLORS.put("sand", MapColor.SAND);
		MAP_COLORS.put("wool", MapColor.WOOL);
		MAP_COLORS.put("fire", MapColor.FIRE);
		MAP_COLORS.put("ice", MapColor.ICE);
		MAP_COLORS.put("metal", MapColor.METAL);
		MAP_COLORS.put("plant", MapColor.PLANT);
		MAP_COLORS.put("snow", MapColor.SNOW);
		MAP_COLORS.put("clay", MapColor.CLAY);
		MAP_COLORS.put("dirt", MapColor.DIRT);
		MAP_COLORS.put("stone", MapColor.STONE);
		MAP_COLORS.put("water", MapColor.WATER);
		MAP_COLORS.put("wood", MapColor.WOOD);
		MAP_COLORS.put("quartz", MapColor.QUARTZ);
		MAP_COLORS.put("white", MapColor.WOOL); //fuck you it's white
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
		MAP_COLORS.put("diamond", MapColor.DIAMOND);
		MAP_COLORS.put("lapis", MapColor.LAPIS);
		MAP_COLORS.put("emerald", MapColor.EMERALD);
		MAP_COLORS.put("podzol", MapColor.PODZOL);
		MAP_COLORS.put("nether", MapColor.NETHER);
		MAP_COLORS.put("white_terracotta", MapColor.WHITE_TERRACOTTA);
		MAP_COLORS.put("orange_terracotta", MapColor.ORANGE_TERRACOTTA);
		MAP_COLORS.put("magenta_terracotta", MapColor.MAGENTA_TERRACOTTA);
		MAP_COLORS.put("light_blue_terracotta", MapColor.LIGHT_BLUE_TERRACOTTA);
		MAP_COLORS.put("yellow_terracotta", MapColor.YELLOW_TERRACOTTA);
		MAP_COLORS.put("lime_terracotta", MapColor.LIME_TERRACOTTA);
		MAP_COLORS.put("pink_terracotta", MapColor.PINK_TERRACOTTA);
		MAP_COLORS.put("gray_terracotta", MapColor.GRAY_TERRACOTTA);
		MAP_COLORS.put("light_gray_terracotta", MapColor.LIGHT_GRAY_TERRACOTTA);
		MAP_COLORS.put("cyan_terracotta", MapColor.CYAN_TERRACOTTA);
		MAP_COLORS.put("purple_terracotta", MapColor.PURPLE_TERRACOTTA);
		MAP_COLORS.put("blue_terracotta", MapColor.BLUE_TERRACOTTA);
		MAP_COLORS.put("brown_terracotta", MapColor.BROWN_TERRACOTTA);
		MAP_COLORS.put("green_terracotta", MapColor.GREEN_TERRACOTTA);
		MAP_COLORS.put("red_terracotta", MapColor.RED_TERRACOTTA);
		MAP_COLORS.put("black_terracotta", MapColor.BLACK_TERRACOTTA);
		MAP_COLORS.put("crimson_nylium", MapColor.CRIMSON_NYLIUM);
		MAP_COLORS.put("crimson_stem", MapColor.CRIMSON_STEM);
		MAP_COLORS.put("crimson_hyphae", MapColor.CRIMSON_HYPHAE);
		MAP_COLORS.put("warped_nylium", MapColor.WARPED_NYLIUM);
		MAP_COLORS.put("warped_stem", MapColor.WARPED_STEM);
		MAP_COLORS.put("warped_hyphae", MapColor.WARPED_HYPHAE);
		MAP_COLORS.put("warped_wart_block", MapColor.WARPED_WART_BLOCK);
		MAP_COLORS.put("deepslate", MapColor.DEEPSLATE);
		MAP_COLORS.put("raw_iron", MapColor.RAW_IRON);
		MAP_COLORS.put("glow_lichen", MapColor.GLOW_LICHEN);

		//Piston behaviors
		PISTON_BEHAVIORS.put("push", PistonBehavior.NORMAL);
		PISTON_BEHAVIORS.put("destroy", PistonBehavior.DESTROY);
		PISTON_BEHAVIORS.put("block", PistonBehavior.BLOCK);
		PISTON_BEHAVIORS.put("push_only", PistonBehavior.PUSH_ONLY);

		//Offset types
		OFFSET_TYPES.put("none", AbstractBlock.OffsetType.NONE);
		OFFSET_TYPES.put("xz", AbstractBlock.OffsetType.XZ);
		OFFSET_TYPES.put("xyz", AbstractBlock.OffsetType.XYZ);

		//Instruments
		/*
		harp, basedrum, snare, hat, bass, flute, bell, guitar, chime, xylophone, iron_xylophone, cow_bell, digeridoo,
		bit, banjo, pling, zombie, skeleton, creeper, dragon, wither_skeleton, piglin, custom_head
		 */
		for (NoteBlockInstrument inst : NoteBlockInstrument.values()) {
			INSTRUMENTS.put(inst.asString(), inst);
		}
	}
}
