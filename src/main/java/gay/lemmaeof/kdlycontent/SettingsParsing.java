package gay.lemmaeof.kdlycontent;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.ArrayList;
import java.util.List;

public class SettingsParsing {

	public static QuiltBlockSettings parseBlockSettings(Identifier id, KDLNode parent) {
		//TODO: custom sound groups and materials(?)
		QuiltBlockSettings settings;
		if (parent.getProps().containsKey("material")) {
			settings = QuiltBlockSettings.of(NamedProperties.MATERIALS.get(parent.getProps().get("material").getAsString().getValue()));
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
				case "sounds" -> settings.sounds(NamedProperties.SOUND_GROUPS.get(node.getArgs().get(0).getAsString().getValue()));
				case "luminance" -> settings.luminance(node.getArgs().get(0).getAsNumberOrElse(0).intValue());
				case "strength" -> {
					if (node.getArgs().size() == 1) {
						settings.strength(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
					} else {
						settings.strength(node.getArgs().get(0).getAsNumberOrElse(0).floatValue(), node.getArgs().get(1).getAsNumberOrElse(0).floatValue());
					}
				}
				case "breakInstantly" -> settings.breakInstantly();
				case "ticksRandomly" -> settings.ticksRandomly();
				case "dynamicBounds" -> settings.dynamicBounds();
				case "dropsNothing" -> settings.dropsNothing();
				case "dropsLike" -> settings.dropsLike(Registry.BLOCK.get(new Identifier(node.getArgs().get(0).getAsString().getValue())));
				case "drops" -> settings.drops(new Identifier(node.getArgs().get(0).getAsString().getValue()));
				case "air" -> settings.air();
				//dynamic luminance, allow spawning, solid block, suffocates, blocks vision, post process, and emmissive lighting too complex to model with kdl for now
				case "requiresTool" -> settings.requiresTool();
				case "mapColor" -> settings.mapColor(NamedProperties.MAP_COLORS.get(node.getArgs().get(0).getAsString().getValue()));
				case "hardness" -> settings.hardness(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "resistance" -> settings.resistance(node.getArgs().get(0).getAsNumberOrElse(0).floatValue());
				case "collidable" -> settings.collidable(node.getArgs().get(0).getAsBooleanOrElse(true));
				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for block {}", node.getIdentifier(), id);
			}
		}
		return settings;
	}

	public static QuiltItemSettings parseItemSettings(Identifier id, KDLNode parent) {
		QuiltItemSettings settings = new QuiltItemSettings();
		ItemGroup group = KdlyContent.GROUP;
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
				default -> KdlyContent.LOGGER.info("Unknown node type {} in kdl for item {}", node.getIdentifier(), id);
			}
		}
		settings.group(group);
		return settings;
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
