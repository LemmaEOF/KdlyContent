package gay.lemmaeof.kdlycontent.content.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gay.lemmaeof.kdlycontent.KdlyContent;
import gay.lemmaeof.kdlycontent.util.VoxelMath;
import gay.lemmaeof.kdlycontent.util.Cuboid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomBlock extends Block implements MaybeWaterloggable, FunctionRunnable<CustomBlock.BlockFunctionPoint> {
	public static final MapCodec<CustomBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		createSettingsCodec(),
		KdlyBlockBehaviors.CODEC.fieldOf("behaviors").forGetter(CustomBlock::behaviors)
	).apply(instance, CustomBlock::create));

	private static final BooleanProperty POWERED = Properties.POWERED;
	private final KdlyBlockBehaviors behaviors;
	private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

	public static CustomBlock create(Settings settings, KdlyBlockBehaviors behaviors) {
		return new CustomBlock(settings, behaviors) {
			@Override
			protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
				super.appendProperties(builder);
				if (behaviors.hasWaterlogged()) builder.add(Properties.WATERLOGGED);
				if (behaviors.rotProp() != RotationProperty.NONE) builder.add(behaviors.rotProp().getProp());
				if (behaviors.functions().containsKey(BlockFunctionPoint.POWERED) || behaviors.functions().containsKey(BlockFunctionPoint.UNPOWERED))
					builder.add(Properties.POWERED);
			}
		};
	}

	public CustomBlock(Settings settings, KdlyBlockBehaviors behaviors) {
		super(settings);
		this.behaviors = behaviors;
		VoxelShape defaultShape = behaviors.getShape();
		for (BlockState state : this.getStateManager().getStates()) {
			switch (behaviors.rotProp) {
				case AXIS -> shapes.put(state, switch ((Direction.Axis) (Object) state.get(behaviors.rotProp.prop)) {
					case X -> VoxelMath.rotateZ(defaultShape);
					case Y -> defaultShape;
					case Z -> VoxelMath.rotateX(defaultShape);
				});
				case HORIZONTAL_AXIS -> shapes.put(state, (Object) state.get(behaviors.rotProp.prop) == Direction.Axis.X?
					defaultShape : VoxelMath.rotate(90, defaultShape));
				case FACING, VERTICAL_DIRECTION -> shapes.put(state, switch ((Direction) state.get(behaviors.rotProp.prop)) {
					case NORTH -> VoxelMath.rotateX(defaultShape);
					case SOUTH -> VoxelMath.rotate(180, VoxelMath.rotateX(defaultShape));
					case EAST -> VoxelMath.rotate(270, VoxelMath.rotateX(defaultShape));
					case WEST -> VoxelMath.rotate(90, VoxelMath.rotateX(defaultShape));
					case UP -> defaultShape;
					case DOWN -> VoxelMath.rotateX(VoxelMath.rotateX(defaultShape));
				});
				case HORIZONTAL_FACING, HOPPER_FACING -> shapes.put(state, switch ((Direction) state.get(behaviors.rotProp.prop)) {
					case NORTH, UP -> defaultShape; //up should never happen here so this should be fine
					case SOUTH -> VoxelMath.rotate(180, defaultShape);
					case EAST -> VoxelMath.rotate(270, defaultShape);
					case WEST -> VoxelMath.rotate(90, defaultShape);
					case DOWN -> VoxelMath.rotateX(defaultShape);
				});
				case NONE -> shapes.put(state, defaultShape);
			}
		}
	}

	public KdlyBlockBehaviors behaviors() {
		return this.behaviors;
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState base = super.getPlacementState(ctx);
		if (base == null) return null;
		if (base.contains(Properties.WATERLOGGED)) {
			base = base.with(Properties.WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
		}
		switch (behaviors.rotProp) {
			case AXIS: return switch (behaviors.placement) {
				case SIDE, OPPOSITE_SIDE -> base.with(Properties.AXIS, ctx.getSide().getAxis());
				case PLAYER, OPPOSITE_PLAYER -> base.with(Properties.AXIS, ctx.getPlayerLookDirection().getAxis());
			};
			case HORIZONTAL_AXIS: switch (behaviors.placement) {
				case SIDE, OPPOSITE_SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_AXIS, ctx.getPlayerLookDirection().getAxis());
					return base.with(Properties.HORIZONTAL_AXIS, ctx.getSide().getAxis());
				}
				case PLAYER, OPPOSITE_PLAYER -> {
					return base.with(Properties.HORIZONTAL_AXIS, ctx.getPlayerLookDirection().getAxis());
				}
			}
			case FACING: return switch(behaviors.placement) {
				case SIDE -> base.with(Properties.FACING, ctx.getSide());
				case OPPOSITE_SIDE -> base.with(Properties.FACING, ctx.getSide().getOpposite());
				case PLAYER -> base.with(Properties.FACING, ctx.getPlayerLookDirection());
				case OPPOSITE_PLAYER -> base.with(Properties.FACING, ctx.getPlayerLookDirection().getOpposite());
			};
			case HORIZONTAL_FACING: switch(behaviors.placement) {
				case SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());
					return base.with(Properties.HORIZONTAL_FACING, ctx.getSide());
				}
				case OPPOSITE_SIDE -> {
					if (ctx.getSide().getAxis() == Direction.Axis.Y)
						return base.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
					return base.with(Properties.HORIZONTAL_FACING, ctx.getSide().getOpposite());
				}
				case PLAYER -> {
					return base.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());
				}
				case OPPOSITE_PLAYER -> {
					return base.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
				}
			}
			case HOPPER_FACING: switch (behaviors.placement) {
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
			case VERTICAL_DIRECTION: switch (behaviors.placement) {
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
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if (state.contains(POWERED)) {
			boolean isGettingPowered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
			boolean isAlreadyPowered = state.get(POWERED);
			if (isGettingPowered && !isAlreadyPowered) {
				world.scheduleBlockTick(pos, this, 4);
				world.setBlockState(pos, state.with(POWERED, true), 4);
			} else if (!isGettingPowered && isAlreadyPowered) {
				world.scheduleBlockTick(pos, this, 4);
				world.setBlockState(pos, state.with(POWERED, false), 4);
			}
		}
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.contains(POWERED)) {
			if (state.get(POWERED)) {
				this.runFunction(world, pos, null, BlockFunctionPoint.POWERED);
			} else {
				this.runFunction(world, pos, null, BlockFunctionPoint.UNPOWERED);
			}
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
	public FluidState getFluidState(BlockState state) {
	  return state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)
		? Fluids.WATER.getStill(false)
		: super.getFluidState(state);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		BlockState base = super.rotate(state, rotation);
		switch (behaviors.rotProp) {
			case FACING, HORIZONTAL_FACING, HOPPER_FACING -> {
				return base.with((DirectionProperty) behaviors.rotProp.prop, rotation.rotate((Direction) state.get(behaviors.rotProp.prop)));
			}
			case AXIS, HORIZONTAL_AXIS -> {
				if (rotation == BlockRotation.CLOCKWISE_90 || rotation == BlockRotation.COUNTERCLOCKWISE_90) {
					return switch ((Direction.Axis) (Object) state.get(behaviors.rotProp.prop)) {
						case X -> base.with((EnumProperty<Direction.Axis>) behaviors.rotProp.getProp(), Direction.Axis.Z);
						case Z -> base.with((EnumProperty<Direction.Axis>) behaviors.rotProp.getProp(), Direction.Axis.X);
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
		switch (behaviors.rotProp) {
			case FACING, HORIZONTAL_FACING, HOPPER_FACING -> {
				Direction dir = (Direction) base.get(behaviors.rotProp.prop);
				if (dir.getAxis() == Direction.Axis.Y) return base;
				return base.with((DirectionProperty) behaviors.rotProp.prop, mirror.apply(dir));
			}
			default -> {
				//mirroring is only for horizontal axes, so vertical direction, axis, and no rot prop stay the same
				return base;
			}
		}
	}

	@Override
	public Map<BlockFunctionPoint, Identifier> getFunctions() {
		return behaviors.functions;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		runFunction(world, pos, placer, BlockFunctionPoint.PLACED);
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		BlockState res = super.onBreak(world, pos, state, player);
		runFunction(world, pos, player, BlockFunctionPoint.BROKEN);
		return res;
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
		if (newState.getBlock() != this) runFunction(world, pos, null, BlockFunctionPoint.REMOVED);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		super.onUse(state, world, pos, player, hit);
		if (runFunction(world, pos, player, BlockFunctionPoint.USED)) return ActionResult.SUCCESS;
		else return ActionResult.PASS;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		super.onUseWithItem(stack, state, world, pos, player, hand, hit);
		if (runFunction(world, pos, player, BlockFunctionPoint.USED)) return ItemActionResult.SUCCESS;
		else return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		super.onBlockBreakStart(state, world, pos, player);
		runFunction(world, pos, player, BlockFunctionPoint.PUNCHED);
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		super.onProjectileHit(world, state, hit, projectile);
		runFunction(world, hit.getBlockPos(), projectile.getOwner(), BlockFunctionPoint.SHOT);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return shapes.get(state);
	}

	public record KdlyBlockBehaviors(boolean hasWaterlogged, RotationProperty rotProp, PlacementRule placement,
									 List<Cuboid> defaultShape, Map<BlockFunctionPoint, Identifier> functions) {
		public static final Codec<KdlyBlockBehaviors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("waterloggable", false).forGetter(KdlyBlockBehaviors::hasWaterlogged),
			RotationProperty.CODEC.optionalFieldOf("rotation", RotationProperty.NONE).forGetter(KdlyBlockBehaviors::rotProp),
			PlacementRule.CODEC.optionalFieldOf("placement", PlacementRule.PLAYER).forGetter(KdlyBlockBehaviors::placement),
			Cuboid.CODEC.listOf().optionalFieldOf("shape", List.of(Cuboid.FULL)).forGetter(KdlyBlockBehaviors::defaultShape),
			Codec.unboundedMap(BlockFunctionPoint.CODEC, Identifier.CODEC).optionalFieldOf("functions", Map.of()).forGetter(KdlyBlockBehaviors::functions)
		).apply(instance, KdlyBlockBehaviors::new));

		public VoxelShape getShape() {
			VoxelShape ret = VoxelShapes.empty();
			for (Cuboid cuboid : defaultShape) {
				ret = VoxelShapes.union(ret, cuboid.asVoxelShape());
			}
			return ret;
		}
	}

	public enum RotationProperty implements StringIdentifiable {
		FACING("facing", Properties.FACING),
		HORIZONTAL_FACING("horizontal_facing", Properties.HORIZONTAL_FACING),
		HOPPER_FACING("hopper_facing", Properties.HOPPER_FACING),
		AXIS("axis", Properties.AXIS),
		HORIZONTAL_AXIS("horizontal_axis", Properties.HORIZONTAL_AXIS),
		VERTICAL_DIRECTION("vertical_direction", Properties.VERTICAL_DIRECTION),
		//TODO: 12-way facing
		NONE("none", null);

		public static final Codec<RotationProperty> CODEC = StringIdentifiable.createCodec(RotationProperty::values);

		private final String name;
		private final Property<?> prop;

		RotationProperty(String name, Property<?> thisProp) {
			this.name = name;
			this.prop = thisProp;
		}

		public String getName() {
			return name;
		}

		@Override
		public String asString() {
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

	public enum PlacementRule implements StringIdentifiable {
		SIDE("side"),
		PLAYER("player"),
		OPPOSITE_SIDE("opposite_side"),
		OPPOSITE_PLAYER("opposite_player");

		public static final Codec<PlacementRule> CODEC = StringIdentifiable.createCodec(PlacementRule::values);

		private final String name;
		PlacementRule(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String asString() {
			return name;
		}

		public static PlacementRule forName(String name) {
			for (PlacementRule rule : values()) {
				if (name.equals(rule.name)) return rule;
			}
			throw new IllegalArgumentException("Unknown placement rule " + name);
		}
	}

	public enum BlockFunctionPoint implements StringIdentifiable {
		PLACED("placed"),
		BROKEN("broken"),
		REMOVED("removed"),
		USED("used"),
		PUNCHED("punched"),
		SHOT("shot"),
		POWERED("powered"),
		UNPOWERED("unpowered");

		public static final Codec<BlockFunctionPoint> CODEC = StringIdentifiable.createCodec(BlockFunctionPoint::values);

		private final String name;

		BlockFunctionPoint(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String asString() {
			return name;
		}

		public static BlockFunctionPoint forName(String name) {
			for (BlockFunctionPoint point : values()) {
				if (name.equals(point.name)) return point;
			}
			throw new IllegalArgumentException("Unknown block function point " + name);
		}
	}
}
