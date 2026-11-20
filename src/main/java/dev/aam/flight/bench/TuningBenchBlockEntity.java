package dev.aam.flight.bench;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 调参工作台方块实体喵：存一把枪喵 */
public class TuningBenchBlockEntity extends BlockEntity {

    private final SimpleContainer container = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            TuningBenchBlockEntity.this.setChanged();
        }
    };

    public TuningBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TUNING_BE, pos, state);
    }

    public Container container() {
        return container;
    }

    public ItemStack stack() {
        return container.getItem(0);
    }

    public void setStack(ItemStack stack) {
        container.setItem(0, stack);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ItemStack st = container.getItem(0);
        if (!st.isEmpty()) {
            out.store("Gun", ItemStack.CODEC, st);
        }
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.read("Gun", ItemStack.CODEC).ifPresent(st -> container.setItem(0, st));
    }

    public Component title() {
        return Component.translatable("block.aamflight.tuning_bench");
    }

    /** 供菜单使用喵 */
    public Player ownerFor(Player p) {
        return p;
    }
}
