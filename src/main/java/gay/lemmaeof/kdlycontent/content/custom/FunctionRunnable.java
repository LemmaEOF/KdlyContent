package gay.lemmaeof.kdlycontent.content.custom;

import com.unascribed.lib39.core.api.util.LatchReference;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public interface FunctionRunnable<T> {
	Map<T, Identifier> getFunctions();

	default boolean runFunction(World world, BlockPos pos, @Nullable Entity user, T point) {
		return runFunction(world, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), user, point);
	}

	default boolean runFunction(World world, Vec3d pos, @Nullable Entity user, T point) {
		if (!world.isClient) {
			LatchReference<Integer> latch = LatchReference.empty();
			Identifier functionId = getFunctions().get(point);
			if (functionId != null) {
				Optional<CommandFunction> funcOpt = world.getServer().getCommandFunctionManager().getFunction(functionId);
				if (funcOpt.isPresent()) {
					CommandFunction func = funcOpt.get();
					ServerCommandSource src = user != null? user.getCommandSource() : world.getServer().getCommandSource();
					src = src.withPosition(pos).withLevel(2).withSilent().method_51411(latch::set);
					world.getServer().getCommandFunctionManager().execute(func, src);
					if (latch.isPresent()) return latch.get() != 0;
					else return true;
				}
			}
		}
		return false;
	}
}
