package wayoftime.bloodmagic.client.hud;

import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.tuple.Pair;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.client.Sprite;
import wayoftime.bloodmagic.client.hud.element.ElementDemonAura;
import wayoftime.bloodmagic.client.hud.element.ElementDivinedInformation;
import wayoftime.bloodmagic.client.hud.element.ElementHolding;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.common.blockentity.IncenseAltarTile;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Registers every draggable HUD overlay element, wiring each one to this branch's current data
 * sources. Ported from 1.20.1's {@code Elements}; see {@link ElementDivinedInformation} for why
 * the old "simple"/"advanced" Blood Altar split collapsed into a single registration here.
 */
public class Elements {
    public static void registerElements() {
        ElementRegistry.registerHandler(BloodMagic.rl("demon_will_aura"), new ElementDemonAura(), new Vec2(0.01f, 0.01f));

        ElementRegistry.registerHandler(BloodMagic.rl("blood_altar"), new ElementDivinedInformation<BloodAltarTile>(2, BloodAltarTile.class) {
            @Override
            public void gatherInformation(Consumer<Pair<Sprite, Function<BloodAltarTile, String>>> information) {
                // Current tier
                information.accept(Pair.of(new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 0, 46, 16, 16), altar -> altar == null
                        ? "IV"
                        : ChatUtil.toRoman(altar.getTier())));
                // Stored/Capacity
                information.accept(Pair.of(new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 16, 46, 16, 16), altar -> String.format("%d/%d", altar == null
                        ? 0
                        : altar.getFluidInTank(0).getAmount(), altar == null ? 10000 : altar.getMainCapacity())));
            }
        }, new Vec2(0.01f, 0.01f));

        ElementRegistry.registerHandler(BloodMagic.rl("incense_altar"), new ElementDivinedInformation<IncenseAltarTile>(2, IncenseAltarTile.class) {
            @Override
            public void gatherInformation(Consumer<Pair<Sprite, Function<IncenseAltarTile, String>>> information) {
                // Current tranquility
                information.accept(Pair.of(new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 80, 46, 16, 16), incense -> incense == null
                        ? "0"
                        : String.valueOf((int) ((100D * (int) (100 * incense.getTranquility())) / 100D))));
                // Incense bonus
                information.accept(Pair.of(new Sprite(BloodMagic.rl("textures/gui/widgets.png"), 96, 46, 16, 16), incense -> incense == null
                        ? "0"
                        : String.valueOf((int) (100 * incense.getIncenseBonus()))));
            }
        }, new Vec2(0.01f, 0.01f));

        ElementRegistry.registerHandler(BloodMagic.rl("holding"), new ElementHolding(), new Vec2(0.72F, 0.9F));
    }
}
