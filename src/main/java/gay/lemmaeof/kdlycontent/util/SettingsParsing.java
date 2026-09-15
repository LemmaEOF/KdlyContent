package gay.lemmaeof.kdlycontent.util;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.*;

public class SettingsParsing {

	public static AbstractBlock.Settings parseBlockSettings(Identifier id, KdlNode parent) {
		//TODO: custom sound groups(?)
		AbstractBlock.Settings settings;
		if (parent.properties().hasProperty("copy")) {
			String copyId = KdlHelper.getProp(parent, "copy", "");
			Block copyBlock = Registries.BLOCK.get(Identifier.of(copyId));
			//if the block is missing and not like that on purpose, assume grabbing from same namespce
			if (copyBlock == Blocks.AIR && !copyId.contains(":") && !copyId.equals("air")) {
				copyBlock = Registries.BLOCK.get(Identifier.of(id.getNamespace(), copyId));
			}
			settings = AbstractBlock.Settings.copyShallow(copyBlock);
		} else if (parent.properties().hasProperty("copy_deep")) {
			String copyId = KdlHelper.getProp(parent, "copy", "");
			Block copyBlock = Registries.BLOCK.get(Identifier.of(copyId));
			//if the block is missing and not like that on purpose, assume grabbing from same namespce
			if (copyBlock == Blocks.AIR && !copyId.contains(":") && !copyId.equals("air")) {
				copyBlock = Registries.BLOCK.get(Identifier.of(id.getNamespace(), copyId));
			}
			settings = AbstractBlock.Settings.copy(copyBlock);
		} else {
			settings = AbstractBlock.Settings.create();
		}
		for (KdlNode node : parent.children()) {
			switch (node.name()) {
				case "no_collision" -> settings.noCollision();
				case "non_opaque" -> settings.nonOpaque();
				case "slipperiness" -> settings.slipperiness(KdlHelper.getArg(node, 0, 0f));
				case "velocity_multiplier" -> settings.velocityMultiplier(KdlHelper.getArg(node, 0, 0f));
				case "jump_velocity_multiplier" -> settings.jumpVelocityMultiplier(KdlHelper.getArg(node, 0, 0f));
				case "sounds" -> settings.sounds(NamedProperties.SOUND_GROUPS.get(KdlHelper.getArg(node, 0, "wood")));
				case "luminance" -> settings.luminance(state -> KdlHelper.getArg(node, 0, 0));
				case "strength" -> {
					if (node.arguments().size() == 1) {
						settings.strength(KdlHelper.getArg(node, 0, 0f));
					} else {
						settings.strength(KdlHelper.getArg(node, 0, 0f), KdlHelper.getArg(node, 1, 0f));
					}
				}
				case "break_instantly" -> settings.breakInstantly();
				case "ticks_randomly" -> settings.ticksRandomly();
				case "dynamic_bounds" -> settings.dynamicBounds();
				case "drops_nothing" -> settings.dropsNothing();
				case "drops_like" -> settings.dropsLike(Registries.BLOCK.get(Identifier.of(KdlHelper.getArg(node, 0, "minecraft:air"))));
//				case "drops" -> settings.dropsLike(Registries.BLOCK.get(Identifier.of(KdlHelper.getArg(node, 0, "minecraft:blocks/air"))));
				case "burnable" -> settings.burnable();
				case "liquid" -> settings.liquid();
				case "solid" -> settings.solid();
				case "not_solid" -> settings.notSolid();
				case "air" -> settings.air();
				//dynamic luminance, allow spawning, solid block, suffocates, blocks vision, post process, and emmissive lighting too complex to model with kdl for now
				case "requires_tool" -> settings.requiresTool();
				case "piston_behavior" -> settings.pistonBehavior(NamedProperties.PISTON_BEHAVIORS.get(KdlHelper.getArg(node, 0, "push")));
				case "offset_type" -> settings.offset(NamedProperties.OFFSET_TYPES.get(KdlHelper.getArg(node, 0, "none")));
				case "no_block_break_particles" -> settings.noBlockBreakParticles();
				//feature flags are hardcoded
				case "instrument" -> settings.instrument(NamedProperties.INSTRUMENTS.get(KdlHelper.getArg(node, 0, "harp")));
				case "replaceable" -> settings.replaceable();
				case "map_color" -> settings.mapColor(NamedProperties.MAP_COLORS.get(KdlHelper.getArg(node, 0, "none")));
				case "hardness" -> settings.hardness(KdlHelper.getArg(node, 0, 0f));
				case "resistance" -> settings.resistance(KdlHelper.getArg(node, 0, 0f));
				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for block {}", node.name(), id);
			}
		}
		return settings;
	}

	public static Item.Settings parseItemSettings(Identifier id, KdlNode parent) {
		Item.Settings settings = new Item.Settings();
		for (KdlNode node : parent.children()) {
			switch (node.name()) {
				case "max_count" -> settings.maxCount(KdlHelper.getArg(node, 0, 0));
				case "max_damage" -> settings.maxDamage(KdlHelper.getArg(node, 0, 0));
				case "recipe_remainder" ->
						settings.recipeRemainder(Registries.ITEM.get(Identifier.of(KdlHelper.getArg(node, 0, "air"))));
				case "rarity" -> {
					String rarity = KdlHelper.getArg(node, 0, "common");
					settings.rarity(switch(rarity) {
						case "common" -> Rarity.COMMON;
						case "uncommon" -> Rarity.UNCOMMON;
						case "rare" -> Rarity.RARE;
						case "epic" -> Rarity.EPIC;
						default -> throw new ParseException(id, "Unknown rarity " + rarity);
					});
				}
				case "fireproof" -> settings.fireproof();
				case "equipment_slot" -> {
					String slot = KdlHelper.getArg(node, 0, "");
					settings.equipmentSlot((entity, stack) -> switch(slot) {
						case "head" -> EquipmentSlot.HEAD;
						case "chest" -> EquipmentSlot.CHEST;
						case "legs" -> EquipmentSlot.LEGS;
						case "feet" -> EquipmentSlot.FEET;
						default -> throw new ParseException(id, "Unknown equipment slot " + slot);
					});
				}
				//TODO: I think components might want a RegistryOps actually? How do I get one of those here?
				case "food" -> {
					JsonObject json = KdlHelper.parseJsonObject(node.children());
					DataResult<Pair<FoodComponent, JsonElement>> result = FoodComponent.CODEC.decode(JsonOps.INSTANCE, json);
					if (result.isError()) {
						throw new ParseException(id, "Decode error on item food component: " + result.error().get().message());
					}
					settings.food(result.result().get().getFirst());
				}
				case "jukebox_playable" -> {
					RegistryKey<JukeboxSong> key = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(KdlHelper.getArg(node, 0, "")));
					settings.jukeboxPlayable(key);
				}
				//TODO: I think components might want a RegistryOps actually? How do I get one of those here?
				case "attribute_modifiers" -> {
					JsonObject json = KdlHelper.parseJsonObject(node.children());
					DataResult<Pair<AttributeModifiersComponent, JsonElement>> result = AttributeModifiersComponent.CODEC.decode(JsonOps.INSTANCE, json);
					if (result.isError()) {
						throw new ParseException(id, "Decode error on item attribute modifiers component: " + result.error().get().message());
					}
					settings.attributeModifiers(result.result().get().getFirst());
				}
				//TODO: I think components might want a RegistryOps actually? How do I get one of those here?
				case "component" -> {
					Identifier compId = Identifier.of(KdlHelper.getArg(node, 0, ""));
					ComponentType<?> type = Registries.DATA_COMPONENT_TYPE.get(compId);
					if (type != null && type.getCodec() != null) {
						JsonElement elem = parseComponentElement(node);
						DataResult<? extends Pair<?, JsonElement>> result = type.getCodec().decode(JsonOps.INSTANCE, elem);
						if (result.isError()) {
							throw new ParseException(id, "Decode error on item component: " + result.error().get().message());
						}
						settings.component((ComponentType) type, (Object) result.result().get().getFirst());
					} else {
						KdlyContent.LOGGER.info("Unknown component type {} in kdl for item {}", KdlHelper.getArg(node, 0, ""), id);
					}
				}

				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for item {}", node.name(), id);
			}
		}
		return settings;
	}

	private static JsonElement parseComponentElement(KdlNode node) {
		if (node.arguments().size() > 1) { //has another argument, literal json
			return KdlHelper.parseJsonLiteral(node.arguments().get(1));
		} else if (!node.children().isEmpty()) {
			return KdlHelper.parseKdlyJson(node.children());
		}
		return JsonNull.INSTANCE;
	}
}
