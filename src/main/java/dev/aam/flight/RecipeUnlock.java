package dev.aam.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** 玩家进服时自动解锁本模组的所有配方喵 */
public final class RecipeUnlock {

    private RecipeUnlock() {}

    public static void unlock(ServerPlayer player) {
        var server = player.level().getServer();
        if (server == null) return;

        List<RecipeHolder<?>> recipes = server.getRecipeManager().getRecipes().stream()
                .filter(h -> h.id().identifier().getNamespace().equals("aamflight"))
                .toList();

        if (!recipes.isEmpty()) {
            player.awardRecipes(recipes);
        }
    }
}
