package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.SettingsParsing;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.text.MessageFormat;
import java.util.*;

public class BlockContentType implements ContentType {
	public static final Map<Identifier, Block> KDLY_BLOCKS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KDLNode parent) {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		KDLNode settingsNode = nodes.get("settings");
		if (settingsNode == null) {
			throw new ParseException(id, "No block settings node provided");
		}
		QuiltBlockSettings settings = SettingsParsing.parseBlockSettings(id, settingsNode);
		KDLNode generatorNode = nodes.get("type");
		String typeName = generatorNode == null? "kdlycontent:standard" : generatorNode.getArgs().get(0).getAsString().getValue();
		if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
		List<KDLNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.getChild().orElse(KDLDocument.builder().build()).getNodes();
		BlockGenerator gen = KdlyRegistries.BLOCK_GENERATORS.get(new Identifier(typeName));
		Block block = gen.generateBlock(id, settings, customConfig);
		KDLY_BLOCKS.put(id, Registry.register(Registry.BLOCK, id, block));

		//item settings time!
		KDLNode itemNode = nodes.get("item");
		if (itemNode != null) {
			QuiltItemSettings itemSettings = SettingsParsing.parseItemSettings(id, itemNode);
			ItemContentType.KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, new BlockItem(block, itemSettings)));
		}
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (KDLY_BLOCKS.size() > 0)
			return Optional.of(MessageFormat.format("{0} block{1}", KDLY_BLOCKS.size(), KDLY_BLOCKS.size() == 1 ? "" : "s"));
		return Optional.empty();
	}
}
