package gay.lemmaeof.kdlycontent;

import com.google.common.collect.ImmutableSet;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.parse.KDLParser;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.ContentItem;
import gay.lemmaeof.kdlycontent.content.ContentLoading;
import gay.lemmaeof.kdlycontent.init.KdlyContentTypes;
import gay.lemmaeof.kdlycontent.init.KdlyGenerators;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class KdlyContent implements ModInitializer {
	public static final String MODID = "kdlycontent";
	public static final Logger LOGGER = LoggerFactory.getLogger("KdlyContent");

	private static final KDLParser parser = new KDLParser();

	public static final ItemGroup GROUP = QuiltItemGroup.createWithIcon(new Identifier("kdlycontent", "generated"), () -> new ItemStack(Items.CRAFTING_TABLE));

	@Override
	public void onInitialize(ModContainer mod) {
		KdlyContentTypes.init();
		KdlyGenerators.init();
		//add an entrypoint for ensuring that people reg their content types and generators before we do!
		QuiltLoader.getEntrypoints("kdlycontent", Runnable.class).forEach(Runnable::run);
		ImmutableSet<ContentItem> data = ContentLoading.getAll("kdlycontent.kdl");
		for (ContentItem item : data) {
			String namespace = item.getIdentifier().getNamespace();
			try {
				KDLDocument kdl = parser.parse(item.createInputStream());
				for (KDLNode node : kdl.getNodes()) {
					String name = node.getArgs().get(0).getAsString().getValue();
					Identifier id = new Identifier(namespace, name);
					String type = toSnakeCase(node.getIdentifier());
					if (!type.contains(":")) type = "kdlycontent:" + type;
					Identifier typeId = new Identifier(type);
					if (KdlyRegistries.CONTENT_TYPES.containsId(typeId)) KdlyRegistries.CONTENT_TYPES.get(typeId).generateFrom(id, node);
					else throw new ParseException(id, "Content type `" + node.getIdentifier() + "` not found (converted to `" + typeId + "`)");
				}
			} catch (IOException | ParseException e) {
				throw new RuntimeException("Could not load KDL for file " + item.getIdentifier(), e);
			}

		}
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
	}

	private String toSnakeCase(String original) {
		//this may be considered sliiiightly evil, my condolences
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		original = original.replaceAll(regex, replacement).toLowerCase();
		return original;
	}

}
