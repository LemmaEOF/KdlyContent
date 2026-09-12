package gay.lemmaeof.kdlycontent.api;

import dev.kdl.KdlNode;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

import java.util.List;

public interface BlockGenerator {

	Block generateBlock(Identifier id, AbstractBlock.Settings settings, List<KdlNode> customConfig) throws ParseException;
}
