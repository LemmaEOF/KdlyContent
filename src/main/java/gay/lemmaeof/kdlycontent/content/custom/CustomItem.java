package gay.lemmaeof.kdlycontent.content.custom;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CustomItem extends Item implements FunctionRunnable<KdlyItemProperties.ItemFunctionPoint> {
	private final KdlyItemProperties props;

	public CustomItem(Settings settings, KdlyItemProperties props) {
		super(settings);
		this.props = props;
	}

	@Override
	public Map<KdlyItemProperties.ItemFunctionPoint, Identifier> getFunctions() {
		return props.functions();
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		if (props.charge().isPresent()) {
			return props.charge().get().maxChargeDuration();
		}
		return super.getMaxUseTime(stack);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		if (props.charge().isPresent()) {
			return props.charge().get().action();
		}
		return super.getUseAction(stack);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (runFunction(attacker.getWorld(), target.getPos(), attacker, KdlyItemProperties.ItemFunctionPoint.HIT_ENTITY)) return true;
		return super.postHit(stack, target, attacker);
	}

	@Override
	public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (runFunction(world, pos, miner, KdlyItemProperties.ItemFunctionPoint.HIT_BLOCK)) return true;
		return super.postMine(stack, world, state, pos, miner);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (runFunction(world, user.getPos(), user, KdlyItemProperties.ItemFunctionPoint.USE_IN_AIR))
			return TypedActionResult.success(user.getStackInHand(hand));
		if (props.charge().isPresent()) {
			user.setCurrentHand(hand);
		}
		return super.use(world, user, hand);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (runFunction(context.getWorld(), context.getBlockPos(), context.getPlayer(), KdlyItemProperties.ItemFunctionPoint.USE_ON_BLOCK))
			return ActionResult.SUCCESS;
		return super.useOnBlock(context);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (runFunction(user.getWorld(), entity.getPos(), user, KdlyItemProperties.ItemFunctionPoint.USE_ON_ENTITY))
			return ActionResult.SUCCESS;
		return super.useOnEntity(stack, user, entity, hand);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (props.charge().isPresent()) {
			KdlyItemProperties.ChargeProperties charge = props.charge().get();
			if (getMaxUseTime(stack) - remainingUseTicks > charge.minChargeDuration()) {
				runFunction(world, user.getPos(), user, KdlyItemProperties.ItemFunctionPoint.CHARGE_RELEASE);
			}
		}
		super.onStoppedUsing(stack, world, user, remainingUseTicks);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		runFunction(world, user.getPos(), user, KdlyItemProperties.ItemFunctionPoint.CHARGE_FINISH);
		return super.finishUsing(stack, world, user);
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		if (props.bar().isPresent()) {
			return props.bar().get().barColor();
		}
		return super.getItemBarColor(stack);
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		if (props.bar().isPresent()) {
			KdlyItemProperties.BarProperties bar = props.bar().get();
			if (stack.hasNbt()) {
				int value = stack.getNbt().getInt(bar.barTag());
				int max = bar.barMax();
				return Math.round(13.0F - (float) value * 13.0F / (float) max);
			}
			return 13;
		}
		return super.getItemBarStep(stack);
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		if (props.bar().isPresent()) {
			KdlyItemProperties.BarProperties bar = props.bar().get();
			if (bar.showWhenFull()) return true;
			if (stack.hasNbt()) {
				int value = stack.getNbt().getInt(bar.barTag());
				int max = bar.barMax();
				return value < max;
			}
			return false;
		}
		return super.isItemBarVisible(stack);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		if (props.hasGlint()) return true;
		return super.hasGlint(stack);
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack) {
		if (props.selfRemainder()) return stack;
		return super.getRecipeRemainder(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.addAll(props.lore());
	}
}
