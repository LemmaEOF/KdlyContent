package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.SettingsParsing;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ItemGenerator;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.text.MessageFormat;
import java.util.*;

public class ItemContentType implements ContentType {
	public static final Map<Identifier, Item> KDLY_ITEMS = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KDLNode parent) {
		Map<String, KDLNode> nodes = KdlHelper.mapNodes(parent.getChild().orElse(KDLDocument.builder().build()).getNodes());
		KDLNode settingsNode = nodes.get("settings");
		if (settingsNode == null) {
			throw new ParseException(id, "No item settings node provided");
		}
		QuiltItemSettings settings = SettingsParsing.parseItemSettings(id, settingsNode);
		KDLNode generatorNode = nodes.get("type");
		String typeName = generatorNode == null? "kdlycontent:standard" : generatorNode.getArgs().get(0).getAsString().getValue();
		if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
		List<KDLNode> customConfig = generatorNode == null? Collections.emptyList() : generatorNode.getChild().orElse(KDLDocument.builder().build()).getNodes();
		ItemGenerator gen = KdlyRegistries.ITEM_GENERATORS.get(new Identifier(typeName));
		KDLY_ITEMS.put(id, Registry.register(Registry.ITEM, id, gen.generateItem(id, settings, customConfig)));
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (KDLY_ITEMS.size() > 0)
			return Optional.of(MessageFormat.format("{0} item{1}", KDLY_ITEMS.size(), KDLY_ITEMS.size() == 1? "" : "s"));
		return Optional.empty();
	}
}
