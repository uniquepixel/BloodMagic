package wayoftime.bloodmagic.client.hud.element;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;
import wayoftime.bloodmagic.client.Sprite;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic "info readout for whatever block entity you're looking at" HUD element: a fixed list of
 * (icon, text) rows, each row's text recomputed live off the currently-targeted block entity of
 * type {@code T}. Ported from 1.20.1's {@code ElementTileInformation} unchanged in structure.
 */
public abstract class ElementTileInformation<T extends BlockEntity> extends HUDElement {

    protected final Class<T> tileClass;
    private final List<Pair<Sprite, Function<T, String>>> information;

    public ElementTileInformation(int width, int lines, Class<T> tileClass) {
        super(width, 18 * lines - 2);

        this.tileClass = tileClass;
        this.information = Lists.newArrayList();
        gatherInformation(information::add);
    }

    public abstract void gatherInformation(Consumer<Pair<Sprite, Function<T, String>>> information);

    @SuppressWarnings("unchecked")
    @Override
    public void draw(GuiGraphics guiGraphics, float partialTicks, int drawX, int drawY) {
        HitResult trace = Minecraft.getInstance().hitResult;
        T tile;
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            tile = null;
        } else {
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(((BlockHitResult) trace).getBlockPos());
            tile = (be != null && be.getClass() == tileClass) ? (T) be : null;
        }

        int yOffset = 0;
        for (Pair<Sprite, Function<T, String>> sprite : information) {
            sprite.getLeft().draw(guiGraphics, drawX, drawY + yOffset);
            int textY = drawY + yOffset + (sprite.getLeft().getTextureHeight() / 4);
            guiGraphics.drawString(Minecraft.getInstance().font, tile != null
                    ? sprite.getRight().apply(tile)
                    : "?", drawX + sprite.getLeft().getTextureWidth() + 2, textY, Color.WHITE.getRGB(), true);
            yOffset += sprite.getLeft().getTextureHeight() + 2;
        }
    }

    @Override
    public boolean shouldRender(Minecraft minecraft) {
        HitResult trace = minecraft.hitResult;
        if (trace == null || trace.getType() != HitResult.Type.BLOCK)
            return false;

        BlockEntity tile = minecraft.level.getBlockEntity(((BlockHitResult) trace).getBlockPos());
        return tile != null && tileClass.isAssignableFrom(tile.getClass());
    }
}
