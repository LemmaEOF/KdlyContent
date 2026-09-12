package gay.lemmaeof.kdlycontent.client;

import gay.lemmaeof.kdlycontent.content.type.BlockContentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

import java.util.HashMap;
import java.util.Map;

public class KdlyContentClient implements ClientModInitializer {
	public static final Map<String, RenderLayer> RENDER_LAYERS = new HashMap<>();

	@Override
	public void onInitializeClient() {
		for (Block block : BlockContentType.KDLY_RENDER_LAYERS.keySet()) {
			BlockRenderLayerMap.INSTANCE.putBlock(block, RENDER_LAYERS.get(BlockContentType.KDLY_RENDER_LAYERS.get(block)));
		}
	}

	static {
		RENDER_LAYERS.put("solid", RenderLayer.getSolid());
		RENDER_LAYERS.put("cutout", RenderLayer.getCutout());
		RENDER_LAYERS.put("cutout_mipped", RenderLayer.getCutoutMipped());
		RENDER_LAYERS.put("translucent", RenderLayer.getTranslucent());
		RENDER_LAYERS.put("tripwire", RenderLayer.getTripwire());
	}
}
