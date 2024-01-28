package gay.lemmaeof.kdlycontent.util;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.ArrayList;
import java.util.List;

public class SettingsParsing {

	public static QuiltBlockSettings parseBlockSettings(Identifier id, KDLNode parent) {
		//TODO: custom sound groups(?)
		QuiltBlockSettings settings;
		if (parent.getProps().containsKey("copy")) {
			settings = QuiltBlockSettings.copyOf(Registries.BLOCK.get(new Identifier(KdlHelper.getProp(parent, "copy", ""))));
		} else {
			settings = QuiltBlockSettings.create();
		}
		for (KDLNode node : parent.getChild().orElse(new KDLDocument.Builder().build()).getNodes()) {
			switch (node.getIdentifier()) {
				case "noCollision" -> settings.noCollision();
				case "nonOpaque" -> settings.nonOpaque();
				case "slipperiness" -> settings.slipperiness(KdlHelper.getArg(node, 0, 0f));
				case "velocityMultiplier" -> settings.velocityMultiplier(KdlHelper.getArg(node, 0, 0f));
				case "jumpVelocityMultiplier" -> settings.jumpVelocityMultiplier(KdlHelper.getArg(node, 0, 0f));
				case "sounds" -> settings.sounds(NamedProperties.SOUND_GROUPS.get(KdlHelper.getArg(node, 0, "wood")));
				case "luminance" -> settings.luminance(KdlHelper.getArg(node, 0, 0));
				case "strength" -> {
					if (node.getArgs().size() == 1) {
						settings.strength(KdlHelper.getArg(node, 0, 0f));
					} else {
						settings.strength(KdlHelper.getArg(node, 0, 0f), KdlHelper.getArg(node, 1, 0f));
					}
				}
				case "breakInstantly" -> settings.breakInstantly();
				case "ticksRandomly" -> settings.ticksRandomly(KdlHelper.getArg(node, 0, true));
				case "dynamicBounds" -> settings.dynamicBounds(KdlHelper.getArg(node, 0, true));
				case "dropsNothing" -> settings.dropsNothing();
				case "dropsLike" -> settings.dropsLike(Registries.BLOCK.get(new Identifier(KdlHelper.getArg(node, 0, "minecraft:air"))));
				case "drops" -> settings.drops(new Identifier(KdlHelper.getArg(node, 0, "minecraft:blocks/air")));
				case "lavaIgnitable" -> settings.lavaIgnitable(KdlHelper.getArg(node, 0, true));
				case "liquid" -> settings.liquid(KdlHelper.getArg(node, 0, true));
				case "solid" -> settings.solid(KdlHelper.getArg(node, 0, true));
				case "nonSolid" -> settings.nonSolid(KdlHelper.getArg(node, 0, true));
				case "air" -> settings.air(KdlHelper.getArg(node, 0, true));
				//dynamic luminance, allow spawning, solid block, suffocates, blocks vision, post process, and emmissive lighting too complex to model with kdl for now
				case "requiresTool" -> settings.requiresTool(KdlHelper.getArg(node, 0, true));
				case "pistonBehavior" -> settings.pistonBehavior(NamedProperties.PISTON_BEHAVIORS.get(KdlHelper.getArg(node, 0, "push")));
				case "offsetType" -> settings.offsetType(NamedProperties.OFFSET_TYPES.get(KdlHelper.getArg(node, 0, "none")));
				case "disableParticlesOnBreak" -> settings.disableParticlesOnBreak();
				//feature flags are hardcoded
				case "instrument" -> settings.instrument(NamedProperties.INSTRUMENTS.get(KdlHelper.getArg(node, 0, "harp")));
				case "replaceable" -> settings.replaceable(KdlHelper.getArg(node, 0, true));
				case "opaque" -> settings.opaque(KdlHelper.getArg(node, 0, true));
				case "mapColor" -> settings.mapColor(NamedProperties.MAP_COLORS.get(KdlHelper.getArg(node, 0, "none")));
				case "hardness" -> settings.hardness(KdlHelper.getArg(node, 0, 0f));
				case "resistance" -> settings.resistance(KdlHelper.getArg(node, 0, 0f));
				case "collidable" -> settings.collidable(KdlHelper.getArg(node, 0, true));
				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for block {}", node.getIdentifier(), id);
			}
		}
		return settings;
	}

	public static QuiltItemSettings parseItemSettings(Identifier id, KDLNode parent) {
		QuiltItemSettings settings = new QuiltItemSettings();
		for (KDLNode node : parent.getChild().orElse(new KDLDocument.Builder().build()).getNodes()) {
			switch (node.getIdentifier()) {
				case "maxCount" -> settings.maxCount(KdlHelper.getArg(node, 0, 0));
				case "maxDamage" -> settings.maxDamage(KdlHelper.getArg(node, 0, 0));
				case "recipeRemainder" ->
						settings.recipeRemainder(Registries.ITEM.get(new Identifier(KdlHelper.getArg(node, 0, "air"))));
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
				case "food" ->
						settings.food(getFoodComponent(id, node.getChild().orElse(new KDLDocument(new ArrayList<>())).getNodes()));
				case "equipmentSlot" -> {
					String slot = KdlHelper.getArg(node, 0, "");
					settings.equipmentSlot(switch(slot) {
						case "head" -> EquipmentSlot.HEAD;
						case "chest" -> EquipmentSlot.CHEST;
						case "legs" -> EquipmentSlot.LEGS;
						case "feet" -> EquipmentSlot.FEET;
						default -> throw new ParseException(id, "Unknown equipment slot " + slot);
					});
				}
				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for item {}", node.getIdentifier(), id);
			}
		}
		return settings;
	}

	private static FoodComponent getFoodComponent(Identifier id, List<KDLNode> config) {
		FoodComponent.Builder builder = new FoodComponent.Builder();
		for (KDLNode node : config) {
			switch (node.getIdentifier()) {
				case "hunger" -> builder.hunger(KdlHelper.getArg(node, 0, 0));
				case "saturation" -> builder.saturationModifier(KdlHelper.getArg(node, 0, 0f));
				case "meat" -> builder.meat();
				case "alwaysEdible" -> builder.alwaysEdible();
				case "snack" -> builder.snack();
				case "statusEffect" -> {
					Identifier effId = new Identifier(KdlHelper.getArg(node, 0, ""));
					StatusEffect eff = Registries.STATUS_EFFECT.get(effId);
					if (eff == null) throw new ParseException(id, "Unknown status effect " + effId);
					float chance = KdlHelper.getProp(node, "chance", 1f);
					int duration = KdlHelper.getProp(node, "duration", 600);
					int amplifier = KdlHelper.getProp(node, "amplifier", 0);
					boolean ambient = KdlHelper.getProp(node, "ambient", false);
					boolean showParticles = KdlHelper.getProp(node, "showParticles", true);
					boolean showIcon = KdlHelper.getProp(node, "showIcon", true);
					builder.statusEffect(new StatusEffectInstance(eff, duration, amplifier, ambient, showParticles, showIcon), chance);
				}
			}
		}
		return builder.build();
	}
}
