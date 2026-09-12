package gay.lemmaeof.kdlycontent.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.util.shape.VoxelShape;

public record Cuboid(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)  {
	public static final Cuboid FULL = new Cuboid(0, 0, 0, 16, 16, 16);
	public static final Codec<Cuboid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.optionalFieldOf("minX", 0f).forGetter(Cuboid::minX),
		Codec.FLOAT.optionalFieldOf("minY", 0f).forGetter(Cuboid::minY),
		Codec.FLOAT.optionalFieldOf("minZ", 0f).forGetter(Cuboid::minZ),
		Codec.FLOAT.optionalFieldOf("maxX", 16f).forGetter(Cuboid::maxX),
		Codec.FLOAT.optionalFieldOf("maxY", 16f).forGetter(Cuboid::maxY),
		Codec.FLOAT.optionalFieldOf("maxZ", 16f).forGetter(Cuboid::maxZ)
	).apply(instance, Cuboid::new));

	public VoxelShape asVoxelShape() {
		return Block.createCuboidShape(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
