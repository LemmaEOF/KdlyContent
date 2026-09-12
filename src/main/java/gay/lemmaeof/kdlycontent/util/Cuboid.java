package gay.lemmaeof.kdlycontent.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;

public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)  {
	public static final Cuboid FULL = new Cuboid(0, 0, 0, 16, 16, 16);
	public static final Codec<Cuboid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("min_x", 0f).forGetter(Cuboid::minX),
		Codec.FLOAT.optionalFieldOf("min_y", 0f).forGetter(Cuboid::minY),
		Codec.FLOAT.optionalFieldOf("min_z", 0f).forGetter(Cuboid::minZ),
		Codec.FLOAT.optionalFieldOf("max_x", 16f).forGetter(Cuboid::maxX),
		Codec.FLOAT.optionalFieldOf("max_y", 16f).forGetter(Cuboid::maxY),
		Codec.FLOAT.optionalFieldOf("max_z", 16f).forGetter(Cuboid::maxZ)
	).apply(instance, Cuboid::new));

	public VoxelShape asVoxelShape() {
		return Block.createCuboidShape(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
