package gay.lemmaeof.kdlycontent.content.type;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;

public class ArmorMaterialContentType implements ContentType {
	public static final Map<Identifier, ArmorMaterial> KDLY_ARMOR_MATERIALS = new HashMap<>();

	private static final int[] BASE_DURABILITY = new int[]{11, 16, 15, 13};

	@Override
	public void generateFrom(Identifier id, KdlNode parent) throws ParseException {
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(parent.children());
		KdlNode protectionNode = nodes.get("defense");
		if (protectionNode == null) throw new ParseException(id, "No defense specified");
		EnumMap<ArmorItem.Type, Integer> protection = parseSlots(id, protectionNode);
		KdlNode toughnessNode = nodes.get("toughness");
		if (toughnessNode == null) throw new ParseException(id, "No toughness specified");
		float toughness = KdlHelper.getArg(toughnessNode, 0, 0f);
		KdlNode resistanceNode = nodes.get("knockback_resistance");
		if (resistanceNode == null) throw new ParseException(id, "No knockback_resistance specified");
		float knockbackResistance = KdlHelper.getArg(toughnessNode, 0, 0f);
		KdlNode enchantabilityNode = nodes.get("enchantability");
		if (enchantabilityNode == null) throw new ParseException(id, "No enchantability specified");
		int enchantability = KdlHelper.getArg(enchantabilityNode, 0, 0);
		KdlNode equipSoundNode = nodes.get("equip_sound");
		if (equipSoundNode == null) throw new ParseException(id, "No equip_sound specified");
		RegistryEntry<SoundEvent> equipSound = Registries.SOUND_EVENT.getEntry(Identifier.of(KdlHelper.getArg(equipSoundNode, 0, ""))).get();

		//fun stuff for ingredients, whee
		KdlNode repairNode = nodes.get("repair_ingredient");
		if (repairNode == null) throw new ParseException(id, "No repair_ingredient specified");
		Supplier<Ingredient> repairIng;
		if (repairNode.properties().hasProperty("tag")) {
			repairIng = () -> Ingredient.fromTag(TagKey.of(Registries.ITEM.getKey(), Identifier.of(String.valueOf(repairNode.properties().getValue("tag").get()))));
		} else {
			repairIng = () -> Ingredient.ofItems(repairNode.arguments().stream().map(val -> Registries.ITEM.get(Identifier.of(String.valueOf(val.value())))).toArray(Item[]::new));
		}

		List<ArmorMaterial.Layer> layers = new ArrayList<>();
		KdlNode layersNode = nodes.get("layers");
		if (layersNode == null) throw new ParseException(id, "No layers specified");
		for (KdlNode layerNode : layersNode.children()) {
			Identifier layerId = Identifier.of(KdlHelper.getArg(layerNode, 0, ""));
			String suffix = KdlHelper.getProp(layerNode, "suffix", "");
			boolean dyeable = KdlHelper.getProp(layerNode, "dyeable", false);
			layers.add(new ArmorMaterial.Layer(id, suffix, dyeable));
		}

		ArmorMaterial mat = new ArmorMaterial(protection, enchantability, equipSound, repairIng, layers, toughness, knockbackResistance);
		KDLY_ARMOR_MATERIALS.put(id, mat);
		Registry.register(Registries.ARMOR_MATERIAL, id, mat);
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (!KDLY_ARMOR_MATERIALS.isEmpty())
			return Optional.of(MessageFormat.format("{0} armor material{1}", KDLY_ARMOR_MATERIALS.size(), KDLY_ARMOR_MATERIALS.size() == 1? "" : "s"));
		return Optional.empty();
	}

	private EnumMap<ArmorItem.Type, Integer> parseSlots(Identifier id, KdlNode node) throws ParseException {
		EnumMap<ArmorItem.Type, Integer> ret = new EnumMap<>(ArmorItem.Type.class);
		List<KdlNode> children = node.children();
		if (!children.isEmpty()) {
			Map<String, KdlNode> nodes = KdlHelper.mapNodes(children);
			KdlNode head = nodes.get("helmet");
			if (head == null) throw new ParseException(id, "No helmet value specified");
			KdlNode chest = nodes.get("chestplate");
			if (chest == null) throw new ParseException(id, "No chestplate value specified");
			KdlNode legs = nodes.get("leggings");
			if (legs == null) throw new ParseException(id, "No leggings value specified");
			KdlNode feet = nodes.get("boots");
			if (feet == null) throw new ParseException(id, "No boots value specified");
			ret.put(ArmorItem.Type.HELMET, KdlHelper.getArg(head, 0, 0));
			ret.put(ArmorItem.Type.CHESTPLATE, KdlHelper.getArg(chest, 0, 0));
			ret.put(ArmorItem.Type.LEGGINGS, KdlHelper.getArg(legs, 0, 0));
			ret.put(ArmorItem.Type.BOOTS, KdlHelper.getArg(feet, 0, 0));
		} else {
			ret.put(ArmorItem.Type.HELMET, KdlHelper.getProp(node, "helmet", 0));
			ret.put(ArmorItem.Type.CHESTPLATE, KdlHelper.getProp(node, "chestplate", 0));
			ret.put(ArmorItem.Type.LEGGINGS, KdlHelper.getProp(node, "leggings", 0));
			ret.put(ArmorItem.Type.BOOTS, KdlHelper.getProp(node, "boots", 0));
		}
		return ret;
	}

	public static RegistryEntry<ArmorMaterial> getMaterial(String rawId, Identifier dataId) {
		Identifier id = Identifier.of(rawId);
		return Registries.ARMOR_MATERIAL.getEntry(id).orElseThrow(() -> new ParseException(dataId, "No armor material with name `" + rawId + "` found"));
	}
}
