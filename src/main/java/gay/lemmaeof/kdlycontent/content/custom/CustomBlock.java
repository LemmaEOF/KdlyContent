package gay.lemmaeof.kdlycontent.content.custom;

import gay.lemmaeof.kdlycontent.util.VoxelMath;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class CustomBlock extends Block implements MaybeWaterloggable {
	private final KdlyBlockProperties props;
	private final Map<BlockState, VoxelShape> shapes = new HashMap<>();
	public CustomBlock(Settings settings, KdlyBlockProperties props) {
		super(settings);
		this.props = props;
		for (BlockState state : this.getStateManager().getStates()) {
			switch (props.rotProp) {
				case AXIS -> shapes.put(state, switch ((Direction.Axis) state.get(props.rotProp.prop)) {
					case X -> VoxelMath.rotateZ(props.defaultShape);
					case Y -> props.defaultShape;
					case Z -> VoxelMath.rotateX(props.defaultShape);
				});
				case HORIZONTAL_AXIS -> shapes.put(state, state.get(props.rotProp.prop) == Direction.Axis.X?
						props.defaultShape : VoxelMath.rotate(90, props.defaultShape));
				case FACING, VERTICAL_DIRECTION -> shapes.put(state, switch ((Direction) state.get(props.rotProp.prop)) {
						case NORTH -> VoxelMath.rotateX(props.defaultShape);
						case SOUTH -> VoxelMath.rotate(180, VoxelMath.rotateX(props.defaultShape));
						case EAST -> VoxelMath.rotate(270, VoxelMath.rotateX(props.defaultShape));
						case WEST -> VoxelMath.rotate(90, VoxelMath.rotateX(props.defaultShape));
						case UP -> props.defaultShape;
						case DOWN -> VoxelMath.rotateX(VoxelMath.rotateX(props.defaultShape));
					});
				case HORIZONTAL_FACING, HOPPER_FACING -> shapes.put(state, switch ((Direction) state.get(props.rotProp.prop)) {
					case NORTH, UP -> props.defaultShape; //up should never happen here so this should be fine
					case SOUTH -> VoxelMath.rotate(180, props.defaultShape);
					case EAST -> VoxelMath.rotate(270, props.defaultShape);
					case WEST -> VoxelMath.rotate(90, props.defaultShape);
					case DOWN -> VoxelMath.rotateX(props.defaultShape);
				});
				case NONE -> shapes.put(state, props.defaultShape);
			}
		}
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState base = super.getPlacementState(ctx);
		if (base == null) return null;
		if (base.contains(Properties.WATERLOGGED)) {
			base = base.with(Properties.WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
		}
		switch (props.rotProp) {
			case AXIS: return switch (props.placement) {
				case SIDE, OPPOSITE_SIDE -> base.with(Properties.AXIS, ctx.getSide().getAxis());
				case PLAYER, OPPOSITE_PLAYER -> base.with(Properties.AXIS, ctx.getPlayerLookDirection().getAxis());
			};
			case HORIZONTAL_AXIS: switch (props.placement) {
				case SIDE, OPPOSITE_SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_AXIS, ctx.getPlayerFacing().getAxis());
					return base.with(Properties.HORIZONTAL_AXIS, ctx.getSide().getAxis());
				}
				case PLAYER, OPPOSITE_PLAYER -> {
					return base.with(Properties.HORIZONTAL_AXIS, ctx.getPlayerFacing().getAxis());
				}
			}
			case FACING: return switch(props.placement) {
				case SIDE -> base.with(Properties.FACING, ctx.getSide());
				case OPPOSITE_SIDE -> base.with(Properties.FACING, ctx.getSide().getOpposite());
				case PLAYER -> base.with(Properties.FACING, ctx.getPlayerLookDirection());
				case OPPOSITE_PLAYER -> base.with(Properties.FACING, ctx.getPlayerLookDirection().getOpposite());
			};
			case HORIZONTAL_FACING: switch(props.placement) {
				case SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing());
					return base.with(Properties.HORIZONTAL_FACING, ctx.getSide());
				}
				case OPPOSITE_SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
					return base.with(Properties.HORIZONTAL_FACING, ctx.getSide().getOpposite());
				}
				case PLAYER -> {
					return base.with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing());
				}
				case OPPOSITE_PLAYER -> {
					return base.with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
				}
			}
			case HOPPER_FACING: switch (props.placement) {
				case SIDE -> {
					if (ctx.getSide() == Direction.UP)
						return base.with(Properties.HOPPER_FACING, Direction.DOWN);
					return base.with(Properties.HOPPER_FACING, ctx.getSide());
				}
				case OPPOSITE_SIDE -> {
					if (ctx.getSide() == Direction.DOWN)
						return base.with(Properties.HOPPER_FACING, Direction.DOWN);
					return base.with(Properties.HOPPER_FACING, ctx.getSide().getOpposite());
				}
				case PLAYER -> {
					if (ctx.getPlayerLookDirection() == Direction.UP)
						return base.with(Properties.HOPPER_FACING, Direction.DOWN);
					return base.with(Properties.HOPPER_FACING, ctx.getPlayerLookDirection());
				}
				case OPPOSITE_PLAYER -> {
					if (ctx.getPlayerLookDirection() == Direction.DOWN)
						return base.with(Properties.HOPPER_FACING, Direction.DOWN);
					return base.with(Properties.HOPPER_FACING, ctx.getPlayerLookDirection().getOpposite());
				}
			}
			case VERTICAL_DIRECTION: switch (props.placement) {
				case SIDE -> {
					if (ctx.getSide().getAxis() != Direction.Axis.Y)
						return base.with(Properties.VERTICAL_DIRECTION, ctx.getHitPos().y - (double)ctx.getBlockPos().getY() > 0.5? Direction.UP : Direction.DOWN);
					return base.with(Properties.VERTICAL_DIRECTION, ctx.getSide());
				}
				case OPPOSITE_SIDE -> {
					if (ctx.getSide().getAxis() != Direction.Axis.Y)
						return base.with(Properties.VERTICAL_DIRECTION, ctx.getHitPos().y - (double)ctx.getBlockPos().getY() > 0.5? Direction.DOWN : Direction.UP);
					return base.with(Properties.VERTICAL_DIRECTION, ctx.getSide().getOpposite());
				}
				case PLAYER -> {
					return base.with(Properties.VERTICAL_DIRECTION, ctx.getVerticalPlayerLookDirection());
				}
				case OPPOSITE_PLAYER -> {
					return base.with(Properties.VERTICAL_DIRECTION, ctx.getVerticalPlayerLookDirection().getOpposite());
				}
			}
			default:
				return base;
		}
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		BlockState base = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
		if (state.contains(Properties.WATERLOGGED)) {
			if (state.get(Properties.WATERLOGGED)) {
				world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
			}
		}
		return base;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		BlockState base = super.rotate(state, rotation);
		switch (props.rotProp) {
			case FACING, HORIZONTAL_FACING, HOPPER_FACING -> {
				return base.with((DirectionProperty) props.rotProp.prop, rotation.rotate((Direction) state.get(props.rotProp.prop)));
			}
			case AXIS, HORIZONTAL_AXIS -> {
				if (rotation == BlockRotation.CLOCKWISE_90 || rotation == BlockRotation.COUNTERCLOCKWISE_90) {
					return switch ((Direction.Axis) state.get(props.rotProp.prop)) {
						case X -> base.with((EnumProperty<Direction.Axis>) props.rotProp.prop, Direction.Axis.Z);
						case Z -> base.with((EnumProperty<Direction.Axis>) props.rotProp.prop, Direction.Axis.X);
						default -> base;
					};
				} else {
					return base;
				}
			}
			default -> {
				return base;
			}
		}
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		BlockState base = super.mirror(state, mirror);
		switch (props.rotProp) {
			case FACING, HORIZONTAL_FACING, HOPPER_FACING -> {
				Direction dir = (Direction) base.get(props.rotProp.prop);
				if (dir.getAxis() == Direction.Axis.Y) return base;
				return base.with((DirectionProperty) props.rotProp.prop, mirror.apply(dir));
			}
			default -> {
				//mirroring is only for horizontal axes, so vertical direction, axis, and no rot prop stay the same
				return base;
			}
		}
	}

	private boolean runFunction(World world, BlockPos pos, @Nullable Entity user, FunctionPoint point) {
		if (!world.isClient) {
			Identifier functionId = props.functions.get(point);
			if (functionId != null) {
				Optional<CommandFunction> funcOpt = world.getServer().getCommandFunctionManager().getFunction(functionId);
				if (funcOpt.isPresent()) {
					CommandFunction func = funcOpt.get();
					ServerCommandSource src = user != null? user.getCommandSource() : world.getServer().getCommandSource();
					src = src.withPosition(new Vec3d(pos.getX(), pos.getY(), pos.getZ())).withLevel(2).withSilent();
					world.getServer().getCommandFunctionManager().execute(func, src);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		runFunction(world, pos, placer, FunctionPoint.PLACED);
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		super.onBreak(world, pos, state, player);
		runFunction(world, pos, player, FunctionPoint.BROKEN);
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
		if (newState.getBlock() != this) runFunction(world, pos, null, FunctionPoint.REMOVED);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		super.onUse(state, world, pos, player, hand, hit);
		if (runFunction(world, pos, player, FunctionPoint.USED)) return ActionResult.SUCCESS;
		else return ActionResult.PASS;
	}

	@Override
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		super.onBlockBreakStart(state, world, pos, player);
		runFunction(world, pos, player, FunctionPoint.PUNCHED);
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		super.onProjectileHit(world, state, hit, projectile);
		runFunction(world, hit.getBlockPos(), projectile.getOwner(), FunctionPoint.SHOT);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return shapes.get(state);
	}

	public record KdlyBlockProperties(boolean hasWaterlogged, RotationProperty rotProp, PlacementRule placement,
									  VoxelShape defaultShape, PistonBehavior pistonBehavior, Map<FunctionPoint, Identifier> functions) { }

	public enum RotationProperty {
		FACING("facing", Properties.FACING),
		HORIZONTAL_FACING("horizontal_facing", Properties.HORIZONTAL_FACING),
		HOPPER_FACING("hopper_facing", Properties.HOPPER_FACING),
		AXIS("axis", Properties.AXIS),
		HORIZONTAL_AXIS("horizontal_axis", Properties.HORIZONTAL_AXIS),
		VERTICAL_DIRECTION("vertical_direction", Properties.VERTICAL_DIRECTION),
		//TODO: 12-way facing
		NONE("none", null);

		private final String name;
		private final Property<?> prop;

		RotationProperty(String name, Property<?> thisProp) {
			this.name = name;
			this.prop = thisProp;
		}

		public String getName() {
			return name;
		}

		public Property<?> getProp() {
			return prop;
		}

		public static RotationProperty forName(String name) {
			for (RotationProperty prop : values()) {
				if (name.equals(prop.name)) return prop;
			}
			throw new IllegalArgumentException("Unknown rotation property " + name);
		}
	}

	public enum PlacementRule {
		SIDE("side"),
		PLAYER("player"),
		OPPOSITE_SIDE("opposite_side"),
		OPPOSITE_PLAYER("opposite_player");

		private final String name;
		PlacementRule(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static PlacementRule forName(String name) {
			for (PlacementRule rule : values()) {
				if (name.equals(rule.name)) return rule;
			}
			throw new IllegalArgumentException("Unknown placement rule " + name);
		}
	}

	public enum FunctionPoint {
		PLACED("placed"),
		BROKEN("broken"),
		REMOVED("removed"),
		USED("used"),
		PUNCHED("punched"),
		SHOT("shot");

		private final String name;

		FunctionPoint(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static FunctionPoint forName(String name) {
			for (FunctionPoint point : values()) {
				if (name.equals(point.name)) return point;
			}
			throw new IllegalArgumentException("Unknown function point " + name);
		}
	}
}
