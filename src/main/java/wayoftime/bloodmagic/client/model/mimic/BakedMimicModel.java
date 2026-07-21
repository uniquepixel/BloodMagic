package wayoftime.bloodmagic.client.model.mimic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.block.BlockMimic;
import wayoftime.bloodmagic.common.blockentity.TileMimic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ported from 1.20.1's MimicBakedModel (a Forge {@code IDynamicBakedModel}). When the tile has a
 * mimic BlockState set, this delegates entirely to that block's own baked model (so the Mimic
 * genuinely looks/behaves like whatever it's disguised as, AO and all); otherwise it falls back to
 * a plain hand-built single-texture cube using the "solid/ethereal opaque mimic" placeholder
 * texture, which is also what the Mimic's inventory icon looks like (see
 * models/item/mimic.json -> models/block/solidopaquemimic.json, a plain cube_all model that
 * exists purely so this texture gets stitched into the block atlas - this dynamic model never
 * declares texture dependencies of its own, matching upstream).
 * <p>
 * The 1.20.1 original hand-rolled quads via Forge's QuadBakingVertexConsumer.Buffered subclass
 * with an explicit endVertex() call; NeoForge 21.1's QuadBakingVertexConsumer instead directly
 * implements the modern chainable VertexConsumer (addVertex(...).setColor(...).setUv(...)), with
 * no separate "buffered" subtype and no endVertex - a vertex is completed by supplying its
 * position via addVertex.
 */
public class BakedMimicModel implements IDynamicBakedModel {
	private final ResourceLocation texture;

	public BakedMimicModel(ResourceLocation texture) {
		this.texture = texture;
	}

	private TextureAtlasSprite getTexture() {
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	private static Vec3 v(double x, double y, double z) {
		return new Vec3(x, y, z);
	}

	private BakedQuad createQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, TextureAtlasSprite sprite) {
		Vec3 normal = v3.subtract(v2).cross(v1.subtract(v2)).normalize();
		Direction direction = Direction.getNearest(normal.x, normal.y, normal.z);

		QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
		consumer.setSprite(sprite);
		consumer.setDirection(direction);

		putVertex(consumer, v1, sprite, 0, 0);
		putVertex(consumer, v2, sprite, 0, 16);
		putVertex(consumer, v3, sprite, 16, 16);
		putVertex(consumer, v4, sprite, 16, 0);

		return consumer.bakeQuad();
	}

	private void putVertex(QuadBakingVertexConsumer consumer, Vec3 pos, TextureAtlasSprite sprite, float u, float v) {
		consumer.addVertex((float) pos.x, (float) pos.y, (float) pos.z)
				.setColor(1.0f, 1.0f, 1.0f, 1.0f)
				.setUv(sprite.getU(u), sprite.getV(v));
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType layer) {
		BlockState mimic = extraData.get(TileMimic.MIMIC);
		if (mimic != null && !(mimic.getBlock() instanceof BlockMimic)) {
			BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(mimic);
			ChunkRenderTypeSet renderTypes = model.getRenderTypes(mimic, rand, ModelData.EMPTY);
			if (layer == null || renderTypes.contains(layer)) {
				try {
					return model.getQuads(mimic, side, rand, ModelData.EMPTY, layer);
				} catch (Exception e) {
					return Collections.emptyList();
				}
			}
			return Collections.emptyList();
		}

		if (side != null || (layer != null && !layer.equals(RenderType.solid()))) {
			return Collections.emptyList();
		}

		TextureAtlasSprite sprite = getTexture();
		List<BakedQuad> quads = new ArrayList<>();
		double l = 0;
		double r = 1;
		quads.add(createQuad(v(l, r, l), v(l, r, r), v(r, r, r), v(r, r, l), sprite));
		quads.add(createQuad(v(l, l, l), v(r, l, l), v(r, l, r), v(l, l, r), sprite));
		quads.add(createQuad(v(r, r, r), v(r, l, r), v(r, l, l), v(r, r, l), sprite));
		quads.add(createQuad(v(l, r, l), v(l, l, l), v(l, l, r), v(l, r, r), sprite));
		quads.add(createQuad(v(r, r, l), v(r, l, l), v(l, l, l), v(l, r, l), sprite));
		quads.add(createQuad(v(l, r, r), v(l, l, r), v(r, l, r), v(r, r, r), sprite));

		return quads;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return getTexture();
	}

	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	@Override
	public ItemTransforms getTransforms() {
		return ItemTransforms.NO_TRANSFORMS;
	}
}
