package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.rendering.entity.api.client.QuiltArmorMaterialExtensions;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ArmorMaterialContentType implements ContentType {
	public static final Map<Identifier, ArmorMaterial> KDLY_ARMOR_MATERIALS = new HashMap<>();
	public static final Map<Identifier, ArmorMaterial> ALL_ARMOR_MATERIALS = new HashMap<>();

	private static final int[] BASE_DURABILITY = new int[]{11, 16, 15, 13};

	@Override
	public void generateFrom(Identifier id, KDLNode parent) throws ParseException {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		EnumMap<ArmorItem.ArmorSlot, Integer> durability = new EnumMap<>(ArmorItem.ArmorSlot.class);
		if (nodes.containsKey("durabilityMultiplier")) {
			KDLNode node = nodes.get("durabilityMultiplier");
			int multiplier = KdlHelper.getArg(node, 0, 0);
			durability.put(ArmorItem.ArmorSlot.HELMET, multiplier * BASE_DURABILITY[0]);
			durability.put(ArmorItem.ArmorSlot.CHESTPLATE, multiplier * BASE_DURABILITY[1]);
			durability.put(ArmorItem.ArmorSlot.LEGGINGS, multiplier * BASE_DURABILITY[2]);
			durability.put(ArmorItem.ArmorSlot.BOOTS, multiplier * BASE_DURABILITY[3]);
		} else {
			KDLNode node = nodes.get("durability");
			if (node == null) throw new ParseException(id, "No durability or durabilityMultiplier specified");
			durability = parseSlots(id, node);
		}
		KDLNode protectionNode = nodes.get("protection");
		if (protectionNode == null) throw new ParseException(id, "No protection specified");
		EnumMap<ArmorItem.ArmorSlot, Integer> protection = parseSlots(id, protectionNode);
		KDLNode toughnessNode = nodes.get("toughness");
		if (toughnessNode == null) throw new ParseException(id, "No toughness specified");
		float toughness = KdlHelper.getArg(toughnessNode, 0, 0f);
		KDLNode resistanceNode = nodes.get("knockbackResistance");
		if (resistanceNode == null) throw new ParseException(id, "No knockbackResistance specified");
		float knockbackResistance = KdlHelper.getArg(toughnessNode, 0, 0f);
		KDLNode enchantabilityNode = nodes.get("enchantability");
		if (enchantabilityNode == null) throw new ParseException(id, "No enchantability specified");
		int enchantability = KdlHelper.getArg(enchantabilityNode, 0, 0);
		KDLNode equipSoundNode = nodes.get("equipSound");
		if (equipSoundNode == null) throw new ParseException(id, "No equipSound specified");
		SoundEvent equipSound = Registries.SOUND_EVENT.get(new Identifier(KdlHelper.getArg(equipSoundNode, 0, "")));

		//fun stuff for ingredients, whee
		KDLNode repairNode = nodes.get("repairIngredient");
		if (repairNode == null) throw new ParseException(id, "No repairIngredient specified");
		Supplier<Ingredient> repairIng;
		if (repairNode.getProps().containsKey("tag")) {
			repairIng = () -> Ingredient.ofTag(TagKey.of(Registries.ITEM.getKey(), new Identifier(repairNode.getProps().get("tag").getAsString().getValue())));
		} else {
			repairIng = () -> Ingredient.ofItems(repairNode.getArgs().stream().map(val -> Registries.ITEM.get(new Identifier(val.getAsString().getValue()))).toArray(Item[]::new));
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

	private EnumMap<ArmorItem.ArmorSlot, Integer> parseSlots(Identifier id, KDLNode node) throws ParseException {
		EnumMap<ArmorItem.ArmorSlot, Integer> ret = new EnumMap<>(ArmorItem.ArmorSlot.class);
		Optional<KDLDocument> childOpt = node.getChild();
		if (childOpt.isPresent()) {
			KDLDocument child = childOpt.get();
			Map<String, KDLNode> nodes = KdlHelper.mapNodes(child.getNodes());
			KDLNode head = nodes.get("helmet");
			if (head == null) throw new ParseException(id, "No helmet value specified");
			KDLNode chest = nodes.get("chestplate");
			if (chest == null) throw new ParseException(id, "No chestplate value specified");
			KDLNode legs = nodes.get("leggings");
			if (legs == null) throw new ParseException(id, "No leggings value specified");
			KDLNode feet = nodes.get("boots");
			if (feet == null) throw new ParseException(id, "No boots value specified");
			ret.put(ArmorItem.ArmorSlot.HELMET, KdlHelper.getArg(head, 0, 0));
			ret.put(ArmorItem.ArmorSlot.CHESTPLATE, KdlHelper.getArg(chest, 0, 0));
			ret.put(ArmorItem.ArmorSlot.LEGGINGS, KdlHelper.getArg(legs, 0, 0));
			ret.put(ArmorItem.ArmorSlot.BOOTS, KdlHelper.getArg(feet, 0, 0));
		} else {
			ret.put(ArmorItem.ArmorSlot.HELMET, KdlHelper.getProp(node, "helmet", 0));
			ret.put(ArmorItem.ArmorSlot.CHESTPLATE, KdlHelper.getProp(node, "chestplate", 0));
			ret.put(ArmorItem.ArmorSlot.LEGGINGS, KdlHelper.getProp(node, "leggings", 0));
			ret.put(ArmorItem.ArmorSlot.BOOTS, KdlHelper.getProp(node, "boots", 0));
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

	public static class CustomArmorMaterial implements ArmorMaterial, QuiltArmorMaterialExtensions {
		private final Identifier id;
		private final EnumMap<ArmorItem.ArmorSlot, Integer> durability;
		private final EnumMap<ArmorItem.ArmorSlot, Integer> protection;
		private final float toughness;
		private final float knockbackResistance;
		private final int enchantability;
		private final SoundEvent equipSound;
		private final Supplier<Ingredient> repairIngredient;

		public CustomArmorMaterial(Identifier id, EnumMap<ArmorItem.ArmorSlot, Integer> durability, EnumMap<ArmorItem.ArmorSlot, Integer> protection, float toughness,
								   float knockbackResistance, int enchantability, SoundEvent equipSound,
								   Supplier<Ingredient> repairIngredient) {
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
		public int getDurability(ArmorItem.ArmorSlot slot) {
			return durability.get(slot);
		}

		@Override
		public int getProtection(ArmorItem.ArmorSlot slot) {
			return protection.get(slot);
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

		@Override
		public String getName() {
			return id.getPath();
		}

		@Override
		public @ClientOnly @NotNull Identifier getTexture() {
			return id;
		}
	}
}
