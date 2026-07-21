package wayoftime.bloodmagic.api.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.soulnetwork.SoulTicket;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Full interface for the block entity a {@link Ritual} runs on top of - ported from 1.20.1's
 * interface of the same name.
 */
public interface IMasterRitualStone {
    UUID getOwner();

    SoulNetwork getOwnerNetwork();

    boolean activateRitual(ItemStack activationCrystal, Player activator, Ritual ritual);

    void performRitual(Level level, BlockPos pos);

    void stopRitual(Ritual.BreakType breakType);

    int getCooldown();

    void setCooldown(int cooldown);

    boolean isActive();

    void setActive(boolean active);

    Direction getDirection();

    int getRunningTime();

    Level getWorldObj();

    BlockPos getMasterBlockPos();

    String getNextBlockRange(String range);

    void provideInformationOfRitualToPlayer(Player player);

    void provideInformationOfRangeToPlayer(Player player, String range);

    void provideInformationOfWillConfigToPlayer(Player player, List<EnumWillType> typeList);

    void setActiveWillConfig(Player player, List<EnumWillType> typeList);

    EnumReaderBoundaries setBlockRangeByBounds(Player player, String range, BlockPos offset1, BlockPos offset2);

    List<EnumWillType> getActiveWillConfig();

    default SoulTicket ticket(int amount) {
        return SoulTicket.block(getWorldObj(), getMasterBlockPos(), amount);
    }

    /**
     * Ported from 1.20.1's {@code SoulNetwork.causeNausea()} - punishes an owner whose network
     * couldn't cover a ritual's upkeep. Moved here (keyed off the owner UUID this interface
     * already exposes) since this branch's {@link SoulNetwork} no longer keeps a live player ref.
     */
    default void causeNausea() {
        Player player = getWorldObj().getPlayerByUUID(getOwner());
        if (player != null) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 99));
        }
    }

    AreaDescriptor getBlockRange(String range);

    void addBlockRanges(Map<String, AreaDescriptor> blockRanges);

    void addBlockRange(String range, AreaDescriptor defaultRange);

    void setBlockRanges(Map<String, AreaDescriptor> blockRanges);

    void setBlockRange(String range, AreaDescriptor defaultRange);

    Ritual getCurrentRitual();
}
