package gay.lemmaeof.kdlycontent.content;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

//Modified subset of StaticData (https://github.com/CottonMC/StaticData) for better ergonomics in this context!
public class ContentLoading {
	public static final String GLOBAL_DATA_NAMESPACE = "kdlycontent";

	public static ImmutableSet<ContentItem> getAll(String name) {
		ImmutableSet.Builder<ContentItem> builder = ImmutableSet.builder();
		for(ModContainer container : QuiltLoader.getAllMods()) {
			Path staticDataPath = container.rootPath().resolve("content");
			if (Files.isDirectory(staticDataPath)) {
				Path data = staticDataPath.resolve(name);
				if (Files.exists(data) && !Files.isDirectory(data)) {
					builder.add(new ContentItem(toIdentifier(container,data), data));
				}
			}
		}

		Path globalStaticDataFolder = new File(QuiltLoader.getGameDir().toFile(), "content").toPath();
		if (Files.isDirectory(globalStaticDataFolder)) {
			Path data = globalStaticDataFolder.resolve(name);
			if (Files.exists(data) && !Files.isDirectory(data)) {
				builder.add(new ContentItem(toGlobalIdentifier(globalStaticDataFolder, data), data));
			}
		}

		return builder.build();
	}

	private static String getRelative(Path parent, Path child) {
		return parent.toAbsolutePath().relativize(child)
				.toString()
				.replace(File.separatorChar, '/')
				.toLowerCase(Locale.ROOT)
				.replace(' ', '_')
				;
	}

	private static Identifier toIdentifier(ModContainer container, Path path) {
		String rel = getRelative(container.rootPath(), path);
		if (rel.startsWith("static_data/")) { //Should always be true
			rel = rel.substring("static_data/".length());
		}
		return new Identifier(container.metadata().id(), rel);
	}

	private static Identifier toGlobalIdentifier(Path root, Path path) {
		return new Identifier(GLOBAL_DATA_NAMESPACE, getRelative(root, path));
	}
}
