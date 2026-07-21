package wayoftime.bloodmagic.client.model.mimic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.blockentity.TileMimic;

/**
 * Ported from 1.20.1's MimicColor - lets tint-dependent disguises (e.g. mimicking a grass block)
 * render with the correct tint instead of the fallback white.
 */
public class MimicColor implements BlockColor {
	@Override
	public int getColor(BlockState blockState, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
		if (world == null || pos == null) {
			return -1;
		}

		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof TileMimic mimicTile) {
			BlockState mimic = mimicTile.getMimic();
			if (mimic != null) {
				return Minecraft.getInstance().getBlockColors().getColor(mimic, world, pos, tint);
			}
		}

		return -1;
	}
}
