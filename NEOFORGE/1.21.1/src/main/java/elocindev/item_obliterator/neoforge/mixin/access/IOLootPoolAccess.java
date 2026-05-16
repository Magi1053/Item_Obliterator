package elocindev.item_obliterator.neoforge.mixin.access;

import java.util.List;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPool.class)
public interface IOLootPoolAccess {
    @Accessor("entries")
    List<LootPoolEntryContainer> io$getEntries();

    @Accessor("conditions")
    List<LootItemCondition> io$getConditions();

    @Accessor("functions")
    List<LootItemFunction> io$getFunctions();
}
