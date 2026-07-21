package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.common.dataattachment.BMDataAttachments;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.common.will.WorldWillHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Feathered Knife ({@code RitualFeatheredKnife}):
 * finds a Blood Altar in its altar range (cached, like {@link WellOfSufferingRitual}), then bleeds
 * every player in its damage range down to a Will-scaled health threshold each pulse, feeding LP
 * into that altar. Steadfast Will raises the threshold (gentler); Vengeful Will lets it target
 * non-owner players much harder; Destructive Will boosts the LP conversion rate; Corrosive Will
 * activates the incense bonus (via the pre-existing {@code IncenseAltarTile}/incense attachment),
 * consuming a player's built-up incense for a proportional LP multiplier. The original's Soul Fray
 * debuff (which would normally follow an incense-boosted sacrifice) and its Living Armor
 * self-sacrifice-upgrade bonus aren't ported - neither potion nor that upgrade hook exist in this
 * branch yet - everything else is intact. The health-sync packet the original needed is
 * unnecessary here: health is synced entity data in modern Minecraft.
 */
public class FeatheredKnifeRitual extends Ritual {
    public static final String ALTAR_RANGE = "altar";
    public static final String DAMAGE_RANGE = "damage";

    public static final double RAW_WILL_DRAIN = 0.05;
    public static final double DESTRUCTIVE_WILL_DRAIN = 0.05;
    public static final double CORROSIVE_WILL_THRESHOLD = 10;
    public static final double STEADFAST_WILL_THRESHOLD = 10;
    public static final double VENGEFUL_WILL_THRESHOLD = 10;
    public static final int DEFAULT_REFRESH_TIME = 20;

    private int refreshTime = DEFAULT_REFRESH_TIME;
    private BlockPos altarOffsetPos = BlockPos.ZERO;

    public FeatheredKnifeRitual() {
        super(RitualRegistry.rl("feathered_knife"), 0, 25000, "ritual.bloodmagic.feathered_knife");
        addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
        addBlockRange(DAMAGE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-15, -20, -15), 31, 41, 31));

        setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
        setMaximumVolumeAndDistanceOfRange(DAMAGE_RANGE, 0, 25, 25);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        BlockPos pos = masterRitualStone.getMasterBlockPos();
        List<EnumWillType> willConfig = masterRitualStone.getActiveWillConfig();

        double corrosiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.CORROSIVE, willConfig);
        double destructiveWill = this.getWillRespectingConfig(level, pos, EnumWillType.DESTRUCTIVE, willConfig);
        double rawWill = this.getWillRespectingConfig(level, pos, EnumWillType.DEFAULT, willConfig);
        double steadfastWill = this.getWillRespectingConfig(level, pos, EnumWillType.STEADFAST, willConfig);
        double vengefulWill = this.getWillRespectingConfig(level, pos, EnumWillType.VENGEFUL, willConfig);

        refreshTime = getRefreshTimeForRawWill(rawWill);
        boolean consumeRawWill = rawWill >= RAW_WILL_DRAIN && refreshTime != DEFAULT_REFRESH_TIME;

        int maxEffects = currentEssence / getRefreshCost();
        int totalEffects = 0;

        BlockPos altarPos = pos.offset(altarOffsetPos);
        BlockEntity tile = level.getBlockEntity(altarPos);
        AreaDescriptor altarRange = masterRitualStone.getBlockRange(ALTAR_RANGE);

        if (!altarRange.isWithinArea(altarOffsetPos) || !(tile instanceof BloodAltarTile)) {
            for (BlockPos newPos : altarRange.getContainedPositions(pos)) {
                BlockEntity nextTile = level.getBlockEntity(newPos);
                if (nextTile instanceof BloodAltarTile) {
                    tile = nextTile;
                    altarOffsetPos = newPos.subtract(pos);
                    altarRange.resetCache();
                    break;
                }
            }
        }

        boolean useIncense = corrosiveWill >= CORROSIVE_WILL_THRESHOLD;

        if (tile instanceof BloodAltarTile altar) {
            AreaDescriptor damageRange = masterRitualStone.getBlockRange(DAMAGE_RANGE);
            AABB range = damageRange.getAABB(pos);
            double destructiveDrain = 0;

            List<Player> players = level.getEntitiesOfClass(Player.class, range);

            for (Player player : players) {
                float healthThreshold = steadfastWill >= STEADFAST_WILL_THRESHOLD ? 0.7f : 0.3f;

                if (vengefulWill >= VENGEFUL_WILL_THRESHOLD && !player.getGameProfile().getId().equals(masterRitualStone.getOwner())) {
                    healthThreshold = 0.1f;
                }

                float health = player.getHealth();
                float maxHealth = player.getMaxHealth();

                if (health / maxHealth > healthThreshold) {
                    float sacrificedHealth = 1;
                    double lpModifier = 1;

                    if (useIncense) {
                        double incenseAmount = player.getData(BMDataAttachments.INCENSE);
                        sacrificedHealth = health - maxHealth * healthThreshold;
                        lpModifier *= 1 + incenseAmount;
                        player.setData(BMDataAttachments.INCENSE, 0.0);
                    }

                    if (destructiveWill >= DESTRUCTIVE_WILL_DRAIN * sacrificedHealth) {
                        lpModifier *= getLPModifierForWill(destructiveWill);
                        destructiveWill -= DESTRUCTIVE_WILL_DRAIN * sacrificedHealth;
                        destructiveDrain += DESTRUCTIVE_WILL_DRAIN * sacrificedHealth;
                    }

                    player.setHealth(health - sacrificedHealth);
                    altar.sacrificialDaggerCall((int) (BloodMagic.SERVER_CONFIG.SELF_SACRIFICE_CONVERSION.get() * lpModifier * sacrificedHealth), false);

                    totalEffects++;
                    if (totalEffects >= maxEffects) {
                        break;
                    }
                }
            }

            if (destructiveDrain > 0) {
                WorldWillHelper.drainWill(level, pos, EnumWillType.STEADFAST, destructiveDrain);
            }
        }

        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost() * totalEffects));
        if (totalEffects > 0 && consumeRawWill) {
            WorldWillHelper.drainWill(level, pos, EnumWillType.DEFAULT, RAW_WILL_DRAIN);
        }
    }

    @Override
    public int getRefreshTime() {
        return refreshTime;
    }

    @Override
    public int getRefreshCost() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.DUSK);
        addParallelRunes(components, 2, -1, EnumRuneType.WATER);
        addCornerRunes(components, 1, -1, EnumRuneType.AIR);
        addOffsetRunes(components, 2, 4, -1, EnumRuneType.FIRE);
        addOffsetRunes(components, 2, 4, 0, EnumRuneType.EARTH);
        addOffsetRunes(components, 4, 3, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 3, 0, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new FeatheredKnifeRitual();
    }

    @Override
    public Component[] provideInformationOfRitualToPlayer(Player player) {
        return new Component[]{
                Component.translatable(this.getTranslationKey() + ".info"),
                Component.translatable(this.getTranslationKey() + ".default.info"),
                Component.translatable(this.getTranslationKey() + ".corrosive.info"),
                Component.translatable(this.getTranslationKey() + ".steadfast.info"),
                Component.translatable(this.getTranslationKey() + ".destructive.info"),
                Component.translatable(this.getTranslationKey() + ".vengeful.info")
        };
    }

    public double getLPModifierForWill(double destructiveWill) {
        return 1 + destructiveWill * 0.2 / 100;
    }

    public int getRefreshTimeForRawWill(double rawWill) {
        return rawWill >= RAW_WILL_DRAIN ? 10 : DEFAULT_REFRESH_TIME;
    }
}
