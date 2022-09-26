package gay.lemmaeof.kdlycontent.api;

import dev.hbeck.kdl.objects.KDLNode;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.List;

public interface BlockGenerator {

	Block generateBlock(Identifier id, QuiltBlockSettings settings, List<KDLNode> customConfig) throws ParseException;
}
