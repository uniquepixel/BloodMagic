package wayoftime.bloodmagic.client.hud.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.will.WillHelper;

import java.util.List;

/**
 * Demon Will aura meter: one small bar per Will type showing how much of that type the player is
 * currently carrying.
 * <p>
 * Ported from 1.20.1's {@code ElementDemonAura}, re-wired to this branch's Will system
 * ({@code common/will/}). 1.20.1 showed a per-chunk *ambient* aura value ({@code DemonWillHolder}),
 * synced from the server to the client with a dedicated {@code DemonAuraClientPacket} whenever the
 * player carried an "IDemonWillViewer" item, and scaled the bars against a per-item "resolution".
 * Neither of those exist on this branch: {@link wayoftime.bloodmagic.common.will.WorldWillHelper}
 * (the chunk-based ambient pool 1.20.1's element actually read from) only exposes a server-side
 * accessor with no client sync channel, and adding one is a network/common-side change out of
 * scope for this client-only HUD port. Instead this reads {@link WillHelper#getTotalWill}, which
 * is this branch's equivalent "how much Will does the player have access to" source of truth (what
 * a player's Soul Gems/Tartaric Gems hold) - and unlike the old ambient pool, it's already fully
 * client-safe since Soul Gem contents are ordinary networked item data components, no new syncing
 * required. The bar scale (max drawable amount per type) is a fixed constant rather than a
 * per-item "resolution", matching 1.20.1's own fallback default of 100 for when no viewer item was
 * held.
 */
public class ElementDemonAura extends HUDElement {

    private static final ResourceLocation BAR_LOCATION = BloodMagic.rl("textures/hud/bars.png");
    private static final double MAX_DISPLAYED_WILL = 100;

    // Render order is keyed to bars.png's layout (each row is baked into the texture at a fixed
    // index), not to EnumWillType's declaration order - keep this order even if the enum's order
    // changes.
    private final List<EnumWillType> orderedTypes = Lists.newArrayList(EnumWillType.DEFAULT, EnumWillType.CORROSIVE, EnumWillType.STEADFAST, EnumWillType.DESTRUCTIVE, EnumWillType.VENGEFUL);

    public ElementDemonAura() {
        super(80, 46);
    }

    @Override
    public void draw(GuiGraphics guiGraphics, float partialTicks, int drawX, int drawY) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        guiGraphics.blit(BAR_LOCATION, drawX, drawY, 0, 210, 80, 46);

        int i = 0;
        for (EnumWillType type : orderedTypes) {
            i++;
            int textureXOffset = (i > 3) ? (i - 3) : (3 - i);
            int maxBarSize = 30 - 2 * textureXOffset;

            double amount = WillHelper.getTotalWill(type, player);
            double ratio = Math.max(Math.min(amount / MAX_DISPLAYED_WILL, 1), 0);

            double width = maxBarSize * ratio * 2;
            double height = 2;
            double x = drawX + 2 * textureXOffset + 10;
            double y = drawY + 4 * i + 10;

            double textureX = 2 * textureXOffset + 2 * 42;
            double textureY = 4 * i + 220;

            guiGraphics.blit(BAR_LOCATION, (int) x, (int) y, (int) textureX, (int) textureY, (int) width, (int) height);

            if (player.isShiftKeyDown()) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(x - 2 * textureXOffset + 70, (y - 2), 0);
                poseStack.scale(0.5f, 0.5f, 1f);
                guiGraphics.drawString(minecraft.font, String.valueOf((int) amount), 0, 2, 0xffffffff, true);
                poseStack.popPose();
            }
        }
    }

    @Override
    public boolean shouldRender(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            return false;
        }
        for (EnumWillType type : EnumWillType.values()) {
            if (WillHelper.getTotalWill(type, player) > 0) {
                return true;
            }
        }
        return false;
    }
}
