package gay.lemmaeof.kdlycontent.client;

import gay.lemmaeof.kdlycontent.content.type.BlockContentType;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import java.util.HashMap;
import java.util.Map;

public class KdlyContentClient implements ClientModInitializer {
	public static final Map<String, RenderLayer> RENDER_LAYERS = new HashMap<>();

	@Override
	public void onInitializeClient(ModContainer mod) {
		for (Block block : BlockContentType.KDLY_RENDER_LAYERS.keySet()) {
			BlockRenderLayerMap.put(RENDER_LAYERS.get(BlockContentType.KDLY_RENDER_LAYERS.get(block)), block);
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
