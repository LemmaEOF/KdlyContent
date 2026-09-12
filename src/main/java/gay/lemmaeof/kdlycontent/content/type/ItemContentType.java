package gay.lemmaeof.kdlycontent.content.type;

import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.SettingsParsing;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.*;

public class ItemContentType implements ContentType {
	public static final Map<Identifier, Item> KDLY_ITEMS = new HashMap<>();
	public static final Map<ItemGroup, List<Item>> KDLY_ITEM_GROUPS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KdlNode parent) {
		Map<String, KdlNode> nodes = KdlHelper.mapNodes(parent.children());
		KdlNode settingsNode = nodes.get("settings");
		if (settingsNode == null) {
			throw new ParseException(id, "No item settings node provided");
		}
		Item.Settings settings = SettingsParsing.parseItemSettings(id, settingsNode);
		ItemGroup group = KdlyContent.GROUP;
		if (nodes.containsKey("group")) {
			group = Registries.ITEM_GROUP.get(Identifier.of(KdlHelper.getArg(nodes.get("group"), 0, "kdlycontent:generated")));
		}
		KdlNode generatorNode = nodes.get("type");
		String typeName = generatorNode == null? "kdlycontent:standard" : KdlHelper.getArg(generatorNode, 0, "kdlycontent:standard");
		if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
		List<KdlNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.children();
		ItemGenerator gen = KdlyRegistries.ITEM_GENERATORS.get(Identifier.of(typeName));
		Item item = gen.generateItem(id, settings, customConfig);
		KDLY_ITEMS.put(id, Registry.register(Registries.ITEM, id, item));
		KDLY_ITEM_GROUPS.computeIfAbsent(group, g -> new ArrayList<>()).add(item);
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (!KDLY_ITEMS.isEmpty())
			return Optional.of(MessageFormat.format("{0} item{1}", KDLY_ITEMS.size(), KDLY_ITEMS.size() == 1? "" : "s"));
		return Optional.empty();
	}
}
