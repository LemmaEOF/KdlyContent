package gay.lemmaeof.kdlycontent.content.type;

import com.google.gson.JsonObject;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeContentType implements ContentType {
	public static final Map<Identifier, JsonObject> KDLY_RECIPES = new HashMap<>();

	@Override
	public void generateFrom(Identifier id, KdlNode parent) throws ParseException {
		JsonObject data = KdlHelper.parseJsonObject(parent.children());
		KDLY_RECIPES.put(id, data);
	}

	@Override
	public Optional<String> getApplyMessage() {
		if (!KDLY_RECIPES.isEmpty())
			return Optional.of(MessageFormat.format("{0} recipe{1}", KDLY_RECIPES.size(), KDLY_RECIPES.size() == 1? "" : "s"));
		return Optional.empty();
	}
}
