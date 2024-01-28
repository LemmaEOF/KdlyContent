package gay.lemmaeof.kdlycontent.content.type;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLString;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.Optional;

public class ConditionalContentType extends KdlyContent implements ContentType {
	@Override
	public void generateFrom(Identifier id, KDLNode parent) throws ParseException {
		//TODO: other conditions?
		String mod = KdlHelper.getProp(parent, "mod", "");
		if (mod.equals("") || QuiltLoader.isModLoaded(mod)) {
			KDLDocument kdl = parent.getChild().orElse(new KDLDocument.Builder().build());
			parseKdl(id.getNamespace(), kdl);
		}
	}

	@Override
	public Optional<String> getApplyMessage() {
		return Optional.empty();
	}

	@Override
	public boolean needsIdentifier() {
		return false;
	}
}
