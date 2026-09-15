package gay.lemmaeof.kdlycontent;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.parse.Kdl1Parser;
import dev.kdl.parse.Kdl2Parser;
import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.KdlParser;
import gay.debuggy.staticdata.api.StaticData;
import gay.debuggy.staticdata.api.StaticDataItem;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.hooks.LateModInitializer;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.custom.CustomBlock;
import gay.lemmaeof.kdlycontent.content.type.ItemContentType;
import gay.lemmaeof.kdlycontent.init.KdlyContentTypes;
import gay.lemmaeof.kdlycontent.init.KdlyGenerators;
import gay.lemmaeof.kdlycontent.util.ContentTemplate;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KdlyContent implements LateModInitializer {
	private static final Map<ContentType, Map<Identifier, ContentTemplate>> templates = new HashMap<>();

	public static final String MODID = "kdlycontent";
	public static final Logger LOGGER = LoggerFactory.getLogger("KdlyContent");

	private static final KdlParser V2_PARSER = new Kdl2Parser();
	private static final KdlParser V1_PARSER = new Kdl1Parser();

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MODID, "generated"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.kdlycontent.generated"))
					.icon(() -> new ItemStack(Items.CRAFTING_TABLE))
					.build()
	);

	@Override
	public void onLateInitialize() {
		//do all our paperwork - every other mod should have finished initializing now
		KdlyContentTypes.init(); //*needs* to be late so it doesn't miss modded dynregs
		KdlyGenerators.init();
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			for (Item item : ItemContentType.KDLY_ITEM_GROUPS.getOrDefault(group, new ArrayList<>())) {
				entries.add(item);
			}
		});
		Registry.register(Registries.BLOCK_TYPE, Identifier.of(MODID, "custom"), CustomBlock.CODEC);

		//our paperwork is done - let's get loading!
		List<StaticDataItem> data = new ArrayList<>();
		//allow both a `kdlycontent.kdl` and a `kdlycontent` subfolder for more organization
		data.addAll(StaticData.getExactData(Identifier.of("", "kdlycontent.kdl")));
		data.addAll(StaticData.getDataInDirectory(Identifier.of("", "kdlycontent"), true));
		for (StaticDataItem item : data) {
			String namespace = item.getModId();
			LOGGER.debug("Loading from file {}", item.getResourceId());
			KdlDocument kdl;
			try {
				try {
					//give kdl v2 a shot
					kdl = V2_PARSER.parse(item.getAsStream());
				} catch (KdlParseException e) {
					//parse fail - could be kdl v1?
					try {
						kdl = V1_PARSER.parse(item.getAsStream());
					} catch (KdlParseException x) {
						//nope! freak the fuck out and blow up
						throw new RuntimeException("Could not parse KDL for file" + item.getResourceId(), e);
					}
				}
				//call out to actually process the document now that it's been lexed!
				parseKdl(namespace, kdl);
			} catch (IOException | ParseException e) {
				throw new RuntimeException("Could not load KDL for file " + item.getResourceId(), e);
			}
		}

		//everything should be done, let's log what got registered!
		StringBuilder builder = new StringBuilder("Registered ");
		List<String> messages = new ArrayList<>();
		KdlyRegistries.CONTENT_TYPES.forEach(type -> type.getApplyMessage().ifPresent(messages::add));
		for (int i = 0; i < messages.size() - 1; i++) {
			builder.append(messages.get(i));
			if (messages.size() > 2) builder.append(", ");
		}
		if (messages.size() > 1) {
			builder.append("and ").append(messages.get(messages.size() - 1));
		}
		LOGGER.info(builder.toString());
		//juuuuust in case anyone wants to do anything with the stuff that got registered
		FabricLoader.getInstance().getEntrypoints("kdlycontent:after_register", Runnable.class).forEach(Runnable::run);
	}

	//TODO: oh god this method is a nightmare
	protected void parseKdl(String namespace, KdlDocument kdl) {
		for (KdlNode node : kdl.nodes()) {
			Identifier id = Identifier.of(namespace, KdlHelper.getArg(node, 0, "anonymous"));
			String typeName = node.name();
			//we add all default stuff under our namespace instead of vanilla's, so use that as default!
			if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
			Identifier typeId = Identifier.of(typeName);
			if (KdlyRegistries.CONTENT_TYPES.containsId(typeId)) {
				ContentType type = KdlyRegistries.CONTENT_TYPES.get(typeId);
				//define a template!
				if (node.type() != null && node.type().equals("template")) {
					templates.computeIfAbsent(type, t -> new HashMap<>()).put(id, new ContentTemplate(id, node));
				} else {
					//use a template!
					if (node.properties().hasProperty("template")) {
						String templateString = KdlHelper.getProp(node, "template", "");
						Identifier templateId;
						if (templateString.contains(":")) {
							templateId = Identifier.of(templateString);
						} else {
							templateId = Identifier.of(namespace, templateString);
						}
						if (templates.containsKey(type)) {
							Map<Identifier, ContentTemplate> typeTemplates = templates.get(type);
							if (typeTemplates.containsKey(templateId)) {
								ContentTemplate template = typeTemplates.get(templateId);
								type.generateFrom(id, template.expandFor(id, node));
							} else {
								throw new ParseException(id, "No template named `" + templateId + "` for content type `" + node.name() + "` (converted to `" + typeId + "`)");
							}
						} else {
							throw new ParseException(id, "No templates for content type `" + node.name() + "` found (converted to `" + typeId + "`)");
						}
					} else {
						type.generateFrom(id, node);
					}
				}
			} else {
				throw new ParseException(id, "Content type `" + node.name() + "` not found (converted to `" + typeId + "`)");
			}
		}
	}
}
