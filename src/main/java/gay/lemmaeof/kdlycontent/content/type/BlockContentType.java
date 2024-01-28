package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.SettingsParsing;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.text.MessageFormat;
import java.util.*;

public class BlockContentType implements ContentType {
	public static final Map<Identifier, Block> KDLY_BLOCKS = new HashMap<>();
	public static final Map<Block, String> KDLY_RENDER_LAYERS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KDLNode parent) {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		KDLNode settingsNode = nodes.get("settings");
		if (settingsNode == null) {
			throw new ParseException(id, "No block settings node provided");
		}
		QuiltBlockSettings settings = SettingsParsing.parseBlockSettings(id, settingsNode);
		KDLNode generatorNode = nodes.get("type");
		String typeName = generatorNode == null? "kdlycontent:standard" : KdlHelper.getArg(generatorNode, 0, "kdlycontent:standard");
		if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
		List<KDLNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.getChild().orElse(KDLDocument.builder().build()).getNodes();
		BlockGenerator gen = KdlyRegistries.BLOCK_GENERATORS.get(new Identifier(typeName));
		Block block = gen.generateBlock(id, settings, customConfig);
		KDLY_BLOCKS.put(id, Registry.register(Registries.BLOCK, id, block));

		//item settings time!
		KDLNode itemNode = nodes.get("item");
		if (itemNode != null) {
			Identifier groupId = new Identifier(KdlHelper.getProp(itemNode, "group", "kdlycontent:generated"));
			ItemGroup group = Registries.ITEM_GROUP.get(groupId);
			QuiltItemSettings itemSettings = SettingsParsing.parseItemSettings(id, itemNode);
			BlockItem item = new BlockItem(block, itemSettings);
			ItemContentType.KDLY_ITEMS.put(id, Registry.register(Registries.ITEM, id, item));
			ItemContentType.KDLY_ITEM_GROUPS.computeIfAbsent(group, g -> new ArrayList<>()).add(item);
		}

		//render layers!
		KDLNode renderLayerNode = nodes.get("renderLayer");
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
