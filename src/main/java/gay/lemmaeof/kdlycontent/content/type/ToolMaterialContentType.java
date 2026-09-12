package gay.lemmaeof.kdlycontent.content.type;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.api.ContentType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ToolMaterialContentType implements ContentType {
	public static final Map<Identifier, ToolMaterial> KDLY_TOOL_MATERIALS = new HashMap<>();
	public static final Map<Identifier, ToolMaterial> ALL_TOOL_MATERIALS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KdlNode parent) throws ParseException {
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(parent.children());
		KdlNode durabilityNode = nodes.get("maxDamage");
		if (durabilityNode == null) throw new ParseException(id, "No maxDamage specified");
		int durability = KdlHelper.getArg(durabilityNode, 0, 59);
		KdlNode speedNode = nodes.get("miningSpeed");
		if (speedNode == null) throw new ParseException(id, "No miningSpeed specified");
		float miningSpeedMultiplier = KdlHelper.getArg(speedNode, 0, 1f);
		KdlNode attackNode = nodes.get("baseAttackDamage");
		if (attackNode == null) throw new ParseException(id, "No baseAttackDamage specified");
		float attackDamage = KdlHelper.getArg(attackNode, 0, 1f);
		KdlNode inverseTagNode = nodes.get("inverseTag");
		if (inverseTagNode == null) throw new ParseException(id, "No inverseTag specified");
		TagKey<Block> inverseTag = TagKey.of(RegistryKeys.BLOCK, Identifier.of(KdlHelper.getArg(inverseTagNode, 0, "")));
		KdlNode enchantabilityNode = nodes.get("enchantability");
		if (enchantabilityNode == null) throw new ParseException(id, "No enchantability specified");
		int enchantability = KdlHelper.getArg(enchantabilityNode, 0, 1);

		//fun stuff for ingredients, whee
		KdlNode repairNode = nodes.get("repairIngredient");
		if (repairNode == null) throw new ParseException(id, "No repairIngredient specified");
		Supplier<Ingredient> repairIng;
		if (repairNode.properties().hasProperty("tag")) {
			repairIng = () -> Ingredient.fromTag(TagKey.of(Registries.ITEM.getKey(), Identifier.of(String.valueOf(repairNode.properties().getValue("tag").get().value()))));
		} else {
			repairIng = () -> Ingredient.ofItems(repairNode.arguments().stream().map(val -> Registries.ITEM.get(Identifier.of(String.valueOf(val.value())))).toArray(Item[]::new));
		}

		ToolMaterial mat = new CustomToolMaterial(durability, miningSpeedMultiplier, attackDamage, inverseTag, enchantability, repairIng);
		KDLY_TOOL_MATERIALS.put(id, mat);
		ALL_TOOL_MATERIALS.put(id, mat);
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (KDLY_TOOL_MATERIALS.size() > 0)
			return Optional.of(MessageFormat.format("{0} tool material{1}", KDLY_TOOL_MATERIALS.size(), KDLY_TOOL_MATERIALS.size() == 1? "" : "s"));
		return Optional.empty();
	}

	public static ToolMaterial getMaterial(String rawId, Identifier dataId) {
		if (!rawId.contains(":")) {
			Identifier minecraftTest = Identifier.of(rawId);
			if (ALL_TOOL_MATERIALS.containsKey(minecraftTest)) {
				return ALL_TOOL_MATERIALS.get(minecraftTest);
			} else {
				Identifier localTest = Identifier.of(dataId.getNamespace(), rawId);
				if (ALL_TOOL_MATERIALS.containsKey(localTest)) {
					return ALL_TOOL_MATERIALS.get(localTest);
				} else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
			}
		}
		Identifier directTest = Identifier.of(rawId);
		if (ALL_TOOL_MATERIALS.containsKey(directTest)) return ALL_TOOL_MATERIALS.get(directTest);
		else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
	}

	static {
		ALL_TOOL_MATERIALS.put(Identifier.of("wood"), ToolMaterials.WOOD);
		ALL_TOOL_MATERIALS.put(Identifier.of("stone"), ToolMaterials.STONE);
		ALL_TOOL_MATERIALS.put(Identifier.of("iron"), ToolMaterials.IRON);
		ALL_TOOL_MATERIALS.put(Identifier.of("diamond"), ToolMaterials.DIAMOND);
		ALL_TOOL_MATERIALS.put(Identifier.of("gold"), ToolMaterials.GOLD);
		ALL_TOOL_MATERIALS.put(Identifier.of("netherite"), ToolMaterials.NETHERITE);
	}

	private static class CustomToolMaterial implements ToolMaterial {
		private final int durability;
		private final float miningSpeedMultiplier;
		private final float attackDamage;
		private final TagKey<Block> inverseTag;
		private final int enchantability;
		private final Supplier<Ingredient> repairIngredient;

		private CustomToolMaterial(int durability, float miningSpeedMultiplier, float attackDamage, TagKey<Block> inverseTag, int enchantability, Supplier<Ingredient> repairIngredient) {
			this.durability = durability;
			this.miningSpeedMultiplier = miningSpeedMultiplier;
			this.attackDamage = attackDamage;
			this.inverseTag = inverseTag;
			this.enchantability = enchantability;
			this.repairIngredient = repairIngredient;
		}

		@Override
		public int getDurability() {
			return durability;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return miningSpeedMultiplier;
		}

		@Override
		public float getAttackDamage() {
			return attackDamage;
		}

		@Override
		public TagKey<Block> getInverseTag() {
			return inverseTag;
		}

		@Override
		public int getEnchantability() {
			return enchantability;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return repairIngredient.get();
		}
	}
}
