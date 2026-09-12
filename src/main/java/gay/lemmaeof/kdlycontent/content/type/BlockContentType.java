package gay.lemmaeof.kdlycontent.content.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.PassedSettingsOps;
import gay.lemmaeof.kdlycontent.util.SettingsParsing;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.*;

public class BlockContentType implements ContentType {
	public static final Map<Identifier, Block> KDLY_BLOCKS = new HashMap<>();
	public static final Map<Block, String> KDLY_RENDER_LAYERS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KdlNode parent) {
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(parent.children());
		KdlNode settingsNode = nodes.get("settings");
		if (settingsNode == null) {
			throw new ParseException(id, "No block settings node provided");
		}
		AbstractBlock.Settings settings = SettingsParsing.parseBlockSettings(id, settingsNode);
		KdlNode generatorNode = nodes.get("type");
		//TODO: a few toe-stubs to maybe fix with custom before putting this in fully
		/*String typeName = generatorNode == null? "block" : KdlHelper.getArg(generatorNode, 0, "block");
		if (typeName.equals("custom")) typeName = "kdlycontent:custom";
		List<KdlNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.children();
		JsonObject codecValue = KdlHelper.parseJsonObject(customConfig);
		codecValue.addProperty("type", typeName);
		codecValue.addProperty("properties", "<Injected properties placeholder");
		DataResult<Pair<Block, JsonElement>> res = BlockTypes.CODEC.codec().decode(new PassedSettingsOps<>(settings, JsonOps.INSTANCE), codecValue);
		if (res.isError()) throw new ParseException(id, "Decode error on codec block: " + res.error().get().message());
		Block block = res.result().get().getFirst();*/
		String typeName = generatorNode == null? "kdlycontent:standard" : KdlHelper.getArg(generatorNode, 0, "kdlycontent:standard");
		if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
		List<KdlNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.children();
		BlockGenerator gen = KdlyRegistries.BLOCK_GENERATORS.get(Identifier.of(typeName));
		Block block = gen.generateBlock(id, settings, customConfig);
		KDLY_BLOCKS.put(id, Registry.register(Registries.BLOCK, id, block));

		//item settings time!
		KdlNode itemNode = nodes.get("item");
		if (itemNode != null) {
			Identifier groupId = Identifier.of(KdlHelper.getProp(itemNode, "group", "kdlycontent:generated"));
			ItemGroup group = Registries.ITEM_GROUP.get(groupId);
			Item.Settings itemSettings = SettingsParsing.parseItemSettings(id, itemNode);
			BlockItem item = new BlockItem(block, itemSettings);
			ItemContentType.KDLY_ITEMS.put(id, Registry.register(Registries.ITEM, id, item));
			ItemContentType.KDLY_ITEM_GROUPS.computeIfAbsent(group, g -> new ArrayList<>()).add(item);
		}

		//render layers!
		KdlNode renderLayerNode = nodes.get("renderLayer");
		if (renderLayerNode != null) {
			KDLY_RENDER_LAYERS.put(block, KdlHelper.getArg(renderLayerNode, 0, "solid"));
		}
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (KDLY_BLOCKS.size() > 0)
			return Optional.of(MessageFormat.format("{0} block{1}", KDLY_BLOCKS.size(), KDLY_BLOCKS.size() == 1 ? "" : "s"));
		return Optional.empty();
	}
}
