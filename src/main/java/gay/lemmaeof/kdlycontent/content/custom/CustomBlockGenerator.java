package gay.lemmaeof.kdlycontent.content.custom;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import gay.lemmaeof.kdlycontent.KdlHelper;
import gay.lemmaeof.kdlycontent.NamedProperties;
import gay.lemmaeof.kdlycontent.api.BlockGenerator;
import gay.lemmaeof.kdlycontent.api.ParseException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlockGenerator implements BlockGenerator {
	@Override
	public Block generateBlock(Identifier id, QuiltBlockSettings settings, List<KDLNode> customConfig) throws ParseException {
		boolean hasWaterlogged = false;
		CustomBlock.RotationProperty rotationProp = CustomBlock.RotationProperty.NONE;
		CustomBlock.PlacementRule placementRule = CustomBlock.PlacementRule.PLAYER;
		VoxelShape defaultShape = VoxelShapes.empty();
		PistonBehavior behavior = PistonBehavior.NORMAL;
		Map<CustomBlock.FunctionPoint, Identifier> functions = new HashMap<>();

		Map<String, KDLNode> nodes = KdlHelper.mapNodes(customConfig);

		if (nodes.containsKey("properties")) {
			Map<String, KDLNode> propNodes = KdlHelper.mapNodes(nodes.get("properties").getChild().orElse(new KDLDocument.Builder().build()).getNodes());
			if (propNodes.containsKey("waterloggable")) hasWaterlogged = true;
			if (propNodes.containsKey("rotation")) {
				KDLNode rotNode = propNodes.get("rotation");
				try {
					rotationProp = CustomBlock.RotationProperty.forName(rotNode.getProps().get("type").getAsString().getValue());
					if (rotNode.getProps().containsKey("placement")) {
						placementRule = CustomBlock.PlacementRule.forName(rotNode.getProps().get("placement").getAsString().getValue());
					}
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		if (nodes.containsKey("shape")) {
			List<KDLNode> shapeNodes = nodes.get("shape").getChild().orElse(new KDLDocument.Builder().build()).getNodes();
			for (KDLNode shapeNode : shapeNodes) {
				//TODO: enforce node name? not really anything you can do other than cuboids without Major hacks
				defaultShape = VoxelShapes.union(defaultShape, Block.createCuboidShape(
						KdlHelper.getProp(shapeNode, "minX", 0.0F),
						KdlHelper.getProp(shapeNode, "minY", 0.0F),
						KdlHelper.getProp(shapeNode, "minZ", 0.0F),
						KdlHelper.getProp(shapeNode, "maxX", 16.0F),
						KdlHelper.getProp(shapeNode, "maxY", 16.0F),
						KdlHelper.getProp(shapeNode, "maxZ", 16.0F)
				));
			}
		}

		if (nodes.containsKey("pistonBehavior")) {
			KDLNode pistonNode = nodes.get("pistonBehavior");
			String key = pistonNode.getArgs().get(0).getAsString().getValue();
			if (NamedProperties.PISTON_BEHAVIORS.containsKey(key)) {
				behavior = NamedProperties.PISTON_BEHAVIORS.get(key);
			} else {
				throw new ParseException(id, "Unknown piston behavior " + key);
			}
		}

		if (nodes.containsKey("functions")) {
			Map<String, KDLNode> funcNodes = KdlHelper.mapNodes(nodes.get("functions").getChild().orElse(new KDLDocument.Builder().build()).getNodes());
			for (String str : funcNodes.keySet()) {
				KDLNode node = funcNodes.get(str);
				try {
					CustomBlock.FunctionPoint point = CustomBlock.FunctionPoint.forName(str);
					functions.put(point, new Identifier(node.getArgs().get(0).getAsString().getValue()));
				} catch (IllegalArgumentException e) {
					throw new ParseException(id, e.getMessage());
				}
			}
		}

		CustomBlock.KdlyBlockProperties props = new CustomBlock.KdlyBlockProperties(hasWaterlogged, rotationProp, placementRule, defaultShape, behavior, functions);

		return new CustomBlock(settings, props) {
			@Override
			protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
				super.appendProperties(builder);
				if (props.hasWaterlogged()) builder.add(Properties.WATERLOGGED);
				if (props.rotProp() != RotationProperty.NONE) builder.add(props.rotProp().getProp());
			}
		};
	}
}
