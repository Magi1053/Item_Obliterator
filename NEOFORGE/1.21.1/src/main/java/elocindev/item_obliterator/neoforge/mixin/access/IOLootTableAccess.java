package elocindev.item_obliterator.neoforge.mixin.access;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTable.class)
public interface IOLootTableAccess {
    @Accessor("pools")
    List<LootPool> io$getPools();

    @Accessor("functions")
    List<LootItemFunction> io$getFunctions();

    @Accessor("randomSequence")
    Optional<ResourceLocation> io$getRandomSequence();
}
