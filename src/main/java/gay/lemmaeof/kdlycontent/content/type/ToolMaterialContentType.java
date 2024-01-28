package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.api.ContentType;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
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
	public void generateFrom(Identifier id, KDLNode parent) throws ParseException {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		KDLNode durabilityNode = nodes.get("maxDamage");
		if (durabilityNode == null) throw new ParseException(id, "No maxDamage specified");
		int durability = durabilityNode.getArgs().get(0).getAsNumberOrElse(59).intValue();
		KDLNode speedNode = nodes.get("miningSpeed");
		if (speedNode == null) throw new ParseException(id, "No miningSpeed specified");
		float miningSpeedMultiplier = speedNode.getArgs().get(0).getAsNumberOrElse(1.0).floatValue();
		KDLNode attackNode = nodes.get("baseAttackDamage");
		if (attackNode == null) throw new ParseException(id, "No baseAttackDamage specified");
		float attackDamage = attackNode.getArgs().get(0).getAsNumberOrElse(1.0).floatValue();
		KDLNode miningLevelNode = nodes.get("miningLevel");
		if (miningLevelNode == null) throw new ParseException(id, "No miningLevel specified");
		int miningLevel = miningLevelNode.getArgs().get(0).getAsNumberOrElse(1).intValue();
		KDLNode enchantabilityNode = nodes.get("enchantability");
		if (enchantabilityNode == null) throw new ParseException(id, "No enchantability specified");
		int enchantability = enchantabilityNode.getArgs().get(0).getAsNumberOrElse(1).intValue();

		//fun stuff for ingredients, whee
		KDLNode repairNode = nodes.get("repairIngredient");
		if (repairNode == null) throw new ParseException(id, "No repairIngredient specified");
		Supplier<Ingredient> repairIng;
		if (repairNode.getProps().containsKey("tag")) {
			repairIng = () -> Ingredient.ofTag(TagKey.of(Registries.ITEM.getKey(), new Identifier(repairNode.getProps().get("tag").getAsString().getValue())));
		} else {
			repairIng = () -> Ingredient.ofItems(repairNode.getArgs().stream().map(val -> Registries.ITEM.get(new Identifier(val.getAsString().getValue()))).toArray(Item[]::new));
		}

		ToolMaterial mat = new CustomToolMaterial(durability, miningSpeedMultiplier, attackDamage, miningLevel, enchantability, repairIng);
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
			Identifier minecraftTest = new Identifier(rawId);
			if (ALL_TOOL_MATERIALS.containsKey(minecraftTest)) {
				return ALL_TOOL_MATERIALS.get(minecraftTest);
			} else {
				Identifier localTest = new Identifier(dataId.getNamespace(), rawId);
				if (ALL_TOOL_MATERIALS.containsKey(localTest)) {
					return ALL_TOOL_MATERIALS.get(localTest);
				} else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
			}
		}
		Identifier directTest = new Identifier(rawId);
		if (ALL_TOOL_MATERIALS.containsKey(directTest)) return ALL_TOOL_MATERIALS.get(directTest);
		else throw new ParseException(dataId, "No tool material with name `" + rawId + "` found");
	}

	static {
		ALL_TOOL_MATERIALS.put(new Identifier("wood"), ToolMaterials.WOOD);
		ALL_TOOL_MATERIALS.put(new Identifier("stone"), ToolMaterials.STONE);
		ALL_TOOL_MATERIALS.put(new Identifier("iron"), ToolMaterials.IRON);
		ALL_TOOL_MATERIALS.put(new Identifier("diamond"), ToolMaterials.DIAMOND);
		ALL_TOOL_MATERIALS.put(new Identifier("gold"), ToolMaterials.GOLD);
		ALL_TOOL_MATERIALS.put(new Identifier("netherite"), ToolMaterials.NETHERITE);
	}

	private static class CustomToolMaterial implements ToolMaterial {
		private final int durability;
		private final float miningSpeedMultiplier;
		private final float attackDamage;
		private final int miningLevel;
		private final int enchantability;
		private final Supplier<Ingredient> repairIngredient;

		private CustomToolMaterial(int durability, float miningSpeedMultiplier, float attackDamage, int miningLevel, int enchantability, Supplier<Ingredient> repairIngredient) {
			this.durability = durability;
			this.miningSpeedMultiplier = miningSpeedMultiplier;
			this.attackDamage = attackDamage;
			this.miningLevel = miningLevel;
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
		public int getMiningLevel() {
			return miningLevel;
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
