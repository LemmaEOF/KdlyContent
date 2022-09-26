package gay.lemmaeof.kdlycontent.api;

import dev.hbeck.kdl.objects.KDLNode;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.List;

public interface ItemGenerator {

	Item generateItem(Identifier id, QuiltItemSettings settings, List<KDLNode> customConfig) throws ParseException;
}
