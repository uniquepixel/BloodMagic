package wayoftime.bloodmagic.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.menu.FilterMenu;

/**
 * Client screen for {@link FilterMenu}. Reuses the Trainer bracelet's background texture as a
 * placeholder (this system has no dedicated GUI art yet - see {@code BMItemModelProvider}'s
 * {@code reuseTexture} calls for the same convention applied to item icons). Functional but plain,
 * matching this pass's priority order (working filters first, polished art later).
 */
public class FilterScreen extends AbstractGhostScreen<FilterMenu> {
    private Button modeButton;

    public FilterScreen(FilterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 187;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = Button.builder(modeLabel(), button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0))
                .pos(leftPos + 100, topPos + 34)
                .size(66, 20)
                .build();
        addRenderableWidget(modeButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (modeButton != null) {
            modeButton.setMessage(modeLabel());
        }
    }

    private Component modeLabel() {
        boolean composite = this.menu.isNestedSlots();
        boolean toggled = this.menu.getData(FilterMenu.DATA_BLACKLIST) != 0;
        if (composite) {
            return Component.translatable(toggled ? "filter.bloodmagic.matchall" : "filter.bloodmagic.matchany");
        }
        return Component.translatable(toggled ? "filter.bloodmagic.blacklist" : "filter.bloodmagic.whitelist");
    }

    @Override
    public ResourceLocation background() {
        return BloodMagic.rl("textures/gui/container/training_bracelet.png");
    }
}
