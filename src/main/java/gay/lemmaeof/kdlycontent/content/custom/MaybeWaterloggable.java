package gay.lemmaeof.kdlycontent.content.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.Optional;

public interface MaybeWaterloggable extends Waterloggable {

	@Override
	default boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		return state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED) && fluid == Fluids.WATER;
	}

	@Override
	default boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (state.contains(Properties.WATERLOGGED)) {
			return Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
		} else {
			return false;
		}
	}

	@Override
	default ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
		if (state.contains(Properties.WATERLOGGED)) {
			return Waterloggable.super.tryDrainFluid(world, pos, state);
		} else {
			return ItemStack.EMPTY;
		}
	}
}
