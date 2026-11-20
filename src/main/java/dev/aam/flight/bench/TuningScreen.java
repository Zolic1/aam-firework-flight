package dev.aam.flight.bench;

import dev.aam.flight.AamConfig;
import dev.aam.flight.config.ConfigOption;
import dev.aam.flight.item.ItemStats;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 调参工作台界面喵：左边放枪，右边列表调参数，点保存生效喵 */
public class TuningScreen extends AbstractContainerScreen<TuningMenu> {

    private static final int ROW_H = 14;
    private static final int VISIBLE = 9;

    private final Map<String, Double> edits = new LinkedHashMap<>();
    private int scroll = 0;

    public TuningScreen(TuningMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 340;
        this.imageHeight = 176;
        this.inventoryLabelY = 70;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + imageWidth - 150;
        int y = topPos + imageHeight - 22;
        addRenderableWidget(Button.builder(Component.literal("保存"), b -> apply(false))
                .bounds(x, y, 70, 18).build());
        addRenderableWidget(Button.builder(Component.literal("重置"), b -> apply(true))
                .bounds(x + 74, y, 70, 18).build());
    }

    private void apply(boolean reset) {
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        if (!reset) {
            edits.forEach((k, v) -> {
                names.add(k);
                values.add(v);
            });
        }
        ClientPlayNetworking.send(new TuneApplyPayload(blockPos(), names, values, reset));
        edits.clear();
    }

    private net.minecraft.core.BlockPos blockPos() {
        return menu.bench().getBlockPos();
    }

    private List<ConfigOption> options() {
        return AamConfig.options();
    }

    private double valueOf(ConfigOption opt) {
        Double edit = edits.get(opt.name());
        if (edit != null) return edit;
        ItemStack gun = menu.gun();
        if (!gun.isEmpty()) return ItemStats.effective(gun, opt.name());
        return opt.get();
    }

    private void adjust(ConfigOption opt, double delta) {
        double step = opt.integer() ? Math.max(1.0, delta) : delta;
        double v = opt.clamp(valueOf(opt) + step);
        edits.put(opt.name(), v);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1B1B22);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF2A2A33);
        g.fill(x + 8, y + 16, x + 50, y + 60, 0xFF14141A);   // 枪格子底色喵
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);

        // 右侧参数面板喵
        int px = leftPos + 150;
        int py = topPos + 18;
        g.fill(px - 4, py - 4, leftPos + imageWidth - 4, py + VISIBLE * ROW_H + 4, 0x80000000);
        g.drawString(font, "参数（左键 - / 右键 + 喵）", px, py - 12, 0xFFFFE07A, false);

        List<ConfigOption> list = options();
        for (int i = 0; i < VISIBLE; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            ConfigOption opt = list.get(idx);
            int y = py + i * ROW_H;
            String name = ItemStats.displayName(opt.name());
            double v = valueOf(opt);
            String text = name + "：" + opt.format(v);
            boolean edited = edits.containsKey(opt.name());
            g.drawString(font, text, px, y, edited ? 0xFF7CFF7C : 0xFFE0E0E0, false);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) return true;
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        int px = leftPos + 150;
        int py = topPos + 18;
        if (mx >= px && mx <= leftPos + imageWidth - 8 && my >= py && my <= py + VISIBLE * ROW_H) {
            int idx = scroll + (int) ((my - py) / ROW_H);
            List<ConfigOption> list = options();
            if (idx >= 0 && idx < list.size()) {
                ConfigOption opt = list.get(idx);
                double delta = opt.integer() ? 1.0 : 0.1;
                adjust(opt, button == 2 ? delta : -delta);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int max = Math.max(0, options().size() - VISIBLE);
        scroll = (int) Math.max(0, Math.min(max, scroll - Math.signum(dy)));
        return true;
    }
}
