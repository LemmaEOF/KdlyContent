package gay.lemmaeof.kdlycontent.content.type;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ConditionalContentType extends KdlyContent implements ContentType {
	@Override
	public void generateFrom(Identifier id, KdlNode parent) throws ParseException {
		//TODO: other conditions?
		String mod = KdlHelper.getProp(parent, "mod", "");
		if (mod.equals("") || FabricLoader.getInstance().isModLoaded(mod)) {
			KdlDocument kdl = new KdlDocument(parent.children());
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
