package wayoftime.bloodmagic.client.model.mimic;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

/**
 * Ported from 1.20.1's MimicModelLoader. Registered twice (once per fallback texture - solid vs
 * ethereal, see ClientModEventHandler) rather than reading the texture from the model json, same
 * as upstream.
 */
public class MimicLoader implements IGeometryLoader<UnbakedMimicModel> {
	private final ResourceLocation texture;

	public MimicLoader(ResourceLocation texture) {
		this.texture = texture;
	}

	@Override
	public UnbakedMimicModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		return new UnbakedMimicModel(texture);
	}
}
