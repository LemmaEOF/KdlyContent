package gay.lemmaeof.kdlycontent.api;

import dev.kdl.KdlNode;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;

public interface ItemGenerator {

	Item generateItem(Identifier id, Item.Settings settings, List<KdlNode> customConfig) throws ParseException;
}
