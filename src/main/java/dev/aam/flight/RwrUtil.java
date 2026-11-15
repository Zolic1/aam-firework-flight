package dev.aam.flight;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 判断玩家是否戴着 RWR 头盔（客户端 / 服务端共用） */
public final class RwrUtil {

    private RwrUtil() {}

    public static boolean hasHelmet(Player p) {
        if (p == null) return false;
        ItemStack helmet = p.getItemBySlot(EquipmentSlot.HEAD);
        var data = helmet.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean("aam_rwr").orElse(false);
    }
}
