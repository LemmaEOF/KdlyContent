package gay.lemmaeof.kdlycontent.content.type;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;


import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.registry.Registry;

public class ArmorMaterialContentType implements ContentType {
	public static final Map<Identifier, ArmorMaterial> KDLY_ARMOR_MATERIALS = new HashMap<>();
	public static final Map<Identifier, ArmorMaterial> ALL_ARMOR_MATERIALS = new HashMap<>();

	private static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};

	@Override
	public void generateFrom(Identifier id, KDLNode parent) throws ParseException {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		int[] durability;
		if (nodes.containsKey("durabilityMultiplier")) {
			KDLNode node = nodes.get("durabilityMultiplier");
			int multiplier = node.getArgs().get(0).getAsNumberOrElse(0).intValue();
			durability = new int[]{
					multiplier * BASE_DURABILITY[0],
					multiplier * BASE_DURABILITY[1],
					multiplier * BASE_DURABILITY[2],
					multiplier * BASE_DURABILITY[3],
			};
		} else {
			KDLNode node = nodes.get("durability");
			if (node == null) throw new ParseException(id, "No durability or durabilityMultiplier specified");
			durability = parseSlots(id, node);
		}
		KDLNode protectionNode = nodes.get("protection");
		if (protectionNode == null) throw new ParseException(id, "No protection specified");
		int[] protection = parseSlots(id, protectionNode);
		KDLNode toughnessNode = nodes.get("toughness");
		if (toughnessNode == null) throw new ParseException(id, "No toughness specified");
		float toughness = toughnessNode.getArgs().get(0).getAsNumberOrElse(0).floatValue();
		KDLNode resistanceNode = nodes.get("knockbackResistance");
		if (resistanceNode == null) throw new ParseException(id, "No knockbackResistance specified");
		float knockbackResistance = toughnessNode.getArgs().get(0).getAsNumberOrElse(0).floatValue();
		KDLNode enchantabilityNode = nodes.get("enchantability");
		if (enchantabilityNode == null) throw new ParseException(id, "No enchantability specified");
		int enchantability = enchantabilityNode.getArgs().get(0).getAsNumberOrElse(0).intValue();
		KDLNode equipSoundNode = nodes.get("equipSound");
		if (equipSoundNode == null) throw new ParseException(id, "No equipSound specified");
		SoundEvent equipSound = Registry.SOUND_EVENT.get(new Identifier(equipSoundNode.getArgs().get(0).getAsString().getValue()));

		//fun stuff for ingredients, whee
		KDLNode repairNode = nodes.get("repairIngredient");
		if (repairNode == null) throw new ParseException(id, "No repairIngredient specified");
		Lazy<Ingredient> repairIng;
		if (repairNode.getProps().containsKey("tag")) {
			repairIng = new Lazy<>(() -> Ingredient.ofTag(TagKey.of(Registry.ITEM_KEY, new Identifier(repairNode.getProps().get("tag").getAsString().getValue()))));
		} else {
			repairIng = new Lazy<>(() -> Ingredient.ofItems(repairNode.getArgs().stream().map(val -> Registry.ITEM.get(new Identifier(val.getAsString().getValue()))).toArray(Item[]::new)));
		}

		ArmorMaterial mat = new CustomArmorMaterial(id, durability, protection, toughness, knockbackResistance, enchantability, equipSound, repairIng);
		KDLY_ARMOR_MATERIALS.put(id, mat);
		ALL_ARMOR_MATERIALS.put(id, mat);
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (KDLY_ARMOR_MATERIALS.size() > 0)
			return Optional.of(MessageFormat.format("{0} armor material{1}", KDLY_ARMOR_MATERIALS.size(), KDLY_ARMOR_MATERIALS.size() == 1? "" : "s"));
		return Optional.empty();
	}

	private int[] parseSlots(Identifier id, KDLNode node) throws ParseException {
		int[] ret = new int[4];
		Optional<KDLDocument> childOpt = node.getChild();
		if (childOpt.isPresent()) {
			KDLDocument child = childOpt.get();
			Map<String, KDLNode> nodes = KdlHelper.mapNodes(child.getNodes());
			KDLNode head = nodes.get("head");
			if (head == null) throw new ParseException(id, "No head value specified");
			KDLNode chest = nodes.get("chest");
			if (chest == null) throw new ParseException(id, "No chest value specified");
			KDLNode legs = nodes.get("legs");
			if (legs == null) throw new ParseException(id, "No legs value specified");
			KDLNode feet = nodes.get("feet");
			if (feet == null) throw new ParseException(id, "No feet value specified");
			ret[0] = head.getArgs().get(0).getAsNumberOrElse(0).intValue();
			ret[1] = chest.getArgs().get(0).getAsNumberOrElse(0).intValue();
			ret[2] = legs.getArgs().get(0).getAsNumberOrElse(0).intValue();
			ret[3] = feet.getArgs().get(0).getAsNumberOrElse(0).intValue();
		} else {
			ret[0] = KdlHelper.getProp(node, "head", 0);
			ret[1] = KdlHelper.getProp(node, "chest", 0);
			ret[2] = KdlHelper.getProp(node, "legs", 0);
			ret[3] = KdlHelper.getProp(node, "feet", 0);
		}
		return ret;
	}

	public static ArmorMaterial getMaterial(String rawId, Identifier dataId) {
		if (!rawId.contains(":")) {
			Identifier minecraftTest = new Identifier(rawId);
			if (ALL_ARMOR_MATERIALS.containsKey(minecraftTest)) {
				return ALL_ARMOR_MATERIALS.get(minecraftTest);
			} else {
				Identifier localTest = new Identifier(dataId.getNamespace(), rawId);
				if (ALL_ARMOR_MATERIALS.containsKey(localTest)) {
					return ALL_ARMOR_MATERIALS.get(localTest);
				} else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
			}
		}
		Identifier directTest = new Identifier(rawId);
		if (ALL_ARMOR_MATERIALS.containsKey(directTest)) return ALL_ARMOR_MATERIALS.get(directTest);
		else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
	}

	static {
		for (ArmorMaterial mat : ArmorMaterials.values()) {
			ALL_ARMOR_MATERIALS.put(new Identifier(mat.getName()), mat);
		}
	}

	public static class CustomArmorMaterial implements ArmorMaterial {
		private final Identifier id;
		private final int[] durability;
		private final int[] protection;
		private final float toughness;
		private final float knockbackResistance;
		private final int enchantability;
		private final SoundEvent equipSound;
		private final Lazy<Ingredient> repairIngredient;

		public CustomArmorMaterial(Identifier id, int[] durability, int[] protection, float toughness,
								   float knockbackResistance, int enchantability, SoundEvent equipSound,
								   Lazy<Ingredient> repairIngredient) {
			this.id = id;
			this.durability = durability;
			this.protection = protection;
			this.toughness = toughness;
			this.knockbackResistance = knockbackResistance;
			this.enchantability = enchantability;
			this.equipSound = equipSound;
			this.repairIngredient = repairIngredient;
		}

		@Override
		public int getDurability(EquipmentSlot slot) {
			return durability[slot.getEntitySlotId()];
		}

		@Override
		public int getProtectionAmount(EquipmentSlot slot) {
			return protection[slot.getEntitySlotId()];
		}

		@Override
		public float getToughness() {
			return toughness;
		}

		@Override
		public float getKnockbackResistance() {
			return knockbackResistance;
		}

		@Override
		public int getEnchantability() {
			return enchantability;
		}

		@Override
		public SoundEvent getEquipSound() {
			return equipSound;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return repairIngredient.get();
		}

		public String getNamespace() {
			return id.getNamespace();
		}

		@Override
		public String getName() {
			return id.getPath();
		}
	}
}
