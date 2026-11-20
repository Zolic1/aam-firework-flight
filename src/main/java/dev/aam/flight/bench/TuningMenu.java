package dev.aam.flight.bench;

import dev.aam.flight.ModItems;
import dev.aam.flight.gun.LmgGun;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 调参工作台菜单喵：0 号格放枪，右侧由 Screen 负责编辑喵 */
public class TuningMenu extends AbstractContainerMenu {

    public static final int GUN_SLOT = 0;
    private final Container container;
    private final TuningBenchBlockEntity bench;

    /** 客户端构造（Fabric 需要喵） */
    public TuningMenu(int id, Inventory inv) {
        this(id, inv, new TuningBenchBlockEntity(net.minecraft.core.BlockPos.ZERO,
                ModBlocks.TUNING_BENCH.defaultBlockState()));
    }

    public TuningMenu(int id, Inventory inv, TuningBenchBlockEntity bench) {
        super(ModBlocks.TUNING_MENU, id);
        this.bench = bench;
        this.container = bench.container();
        addSlot(new Slot(container, 0, 26, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LmgGun.isGun(null, stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    public TuningBenchBlockEntity bench() {
        return bench;
    }

    public ItemStack gun() {
        return container.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == GUN_SLOT) {
            if (!moveItemStackTo(stack, GUN_SLOT + 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!LmgGun.isGun(null, stack)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, GUN_SLOT, GUN_SLOT + 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** 服务端应用参数后刷新喵 */
    public void refresh() {
        if (bench != null) {
            bench.setChanged();
        }
        broadcastChanges();
    }

    public static Container emptyContainer() {
        return new SimpleContainer(1);
    }
}
