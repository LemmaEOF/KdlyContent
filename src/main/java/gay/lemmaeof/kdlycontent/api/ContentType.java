package gay.lemmaeof.kdlycontent.api;

import dev.kdl.KdlNode;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface ContentType {
	static ContentType of(BiConsumer<Identifier, KdlNode> generateFrom, Supplier<Optional<String>> getApplyMessage) {
		return new ContentType() {
			@Override
			public void generateFrom(Identifier id, KdlNode parent) {
				generateFrom.accept(id, parent);
			}

			@Override
			public Optional<String> getApplyMessage() {
				return getApplyMessage.get();
			}
		};
	}

	void generateFrom(Identifier id, KdlNode parent) throws ParseException;
	Optional<String> getApplyMessage();
	default boolean needsIdentifier() { return true; }
}
