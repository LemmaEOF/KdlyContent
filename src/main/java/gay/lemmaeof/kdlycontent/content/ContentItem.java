package gay.lemmaeof.kdlycontent.content;

import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

//Modified subset of StaticData (https://github.com/CottonMC/StaticData) for better ergonomics in this context!
public class ContentItem {
	private final Identifier id;
	private final Path path;

	public ContentItem(Identifier id, Path path) {
		this.id = id;
		this.path = path;
	}

	public InputStream createInputStream() throws IOException {
		if (!Files.exists(path)) throw new FileNotFoundException(); //Should never happen
		return Files.newInputStream(path, StandardOpenOption.READ);
	}

	public Identifier getIdentifier() {
		return id;
	}
}
