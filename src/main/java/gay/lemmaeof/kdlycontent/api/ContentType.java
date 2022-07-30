package gay.lemmaeof.kdlycontent.api;

import dev.hbeck.kdl.objects.KDLNode;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface ContentType {
	static ContentType of(BiConsumer<Identifier, KDLNode> generateFrom, Supplier<Optional<String>> getApplyMessage) {
		return new ContentType() {

			@Override
			public void generateFrom(Identifier id, KDLNode parent) {
				generateFrom.accept(id, parent);
			}

			@Override
			public Optional<String> getApplyMessage() {
				return getApplyMessage.get();
			}
		};
	}

	void generateFrom(Identifier id, KDLNode parent) throws ParseException;
	Optional<String> getApplyMessage();
}
