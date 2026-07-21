package wayoftime.bloodmagic.client.model.mimic;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

/**
 * Ported from 1.20.1's MimicModelGeometry.
 */
public class UnbakedMimicModel implements IUnbakedGeometry<UnbakedMimicModel> {
	private final ResourceLocation texture;

	public UnbakedMimicModel(ResourceLocation texture) {
		this.texture = texture;
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
		return new BakedMimicModel(texture);
	}
}
