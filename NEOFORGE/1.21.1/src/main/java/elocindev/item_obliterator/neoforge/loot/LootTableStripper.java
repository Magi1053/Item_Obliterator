package elocindev.item_obliterator.neoforge.loot;

import elocindev.item_obliterator.neoforge.mixin.access.IOCompositeEntryBaseAccess;
import elocindev.item_obliterator.neoforge.mixin.access.IOLootItemAccess;
import elocindev.item_obliterator.neoforge.mixin.access.IOLootPoolAccess;
import elocindev.item_obliterator.neoforge.mixin.access.IOLootPoolEntryContainerAccess;
import elocindev.item_obliterator.neoforge.mixin.access.IOLootTableAccess;
import elocindev.item_obliterator.neoforge.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.SequentialEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class LootTableStripper {
    private LootTableStripper() {}

    @FunctionalInterface
    private interface CompositeAssembler {
        LootPoolEntryContainer assemble(LootPoolEntryContainer parent, List<LootPoolEntryContainer> children);
    }

    public static void apply(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (table == LootTable.EMPTY) {
            return;
        }
        AtomicBoolean changed = new AtomicBoolean(false);
        IOLootTableAccess tableAccess = (IOLootTableAccess) (Object) table;
        List<LootPool> originalPools = tableAccess.io$getPools();
        List<LootPool> newPools = new ArrayList<>(originalPools.size());
        for (LootPool pool : originalPools) {
            newPools.add(filterPool(pool, changed));
        }
        boolean same = newPools.size() == originalPools.size();
        if (same) {
            for (int i = 0; i < newPools.size(); i++) {
                if (newPools.get(i) != originalPools.get(i)) {
                    same = false;
                    break;
                }
            }
        }
        if (same) {
            return;
        }
        LootTable.Builder tableBuilder = LootTable.lootTable().setParamSet(table.getParamSet());
        tableAccess.io$getRandomSequence().ifPresent(tableBuilder::setRandomSequence);
        for (LootItemFunction function : tableAccess.io$getFunctions()) {
            tableBuilder.apply(() -> function);
        }
        for (LootPool pool : newPools) {
            tableBuilder.withPool(poolToBuilder(pool));
        }
        LootTable rebuilt = tableBuilder.build();
        rebuilt.setLootTableId(event.getName());
        event.setTable(rebuilt);
    }

    private static LootPool.Builder poolToBuilder(LootPool pool) {
        IOLootPoolAccess poolAccess = (IOLootPoolAccess) (Object) pool;
        LootPool.Builder poolBuilder = LootPool.lootPool().setRolls(pool.getRolls()).setBonusRolls(pool.getBonusRolls());
        if (pool.getName() != null) {
            poolBuilder.name(pool.getName());
        }
        for (LootItemCondition condition : poolAccess.io$getConditions()) {
            poolBuilder.when(() -> condition);
        }
        for (LootItemFunction function : poolAccess.io$getFunctions()) {
            poolBuilder.apply(() -> function);
        }
        for (LootPoolEntryContainer entry : poolAccess.io$getEntries()) {
            poolBuilder.add(wrapBuilt(entry));
        }
        return poolBuilder;
    }

    private static LootPool filterPool(LootPool pool, AtomicBoolean changed) {
        IOLootPoolAccess poolAccess = (IOLootPoolAccess) (Object) pool;
        List<LootPoolEntryContainer> originalEntries = poolAccess.io$getEntries();
        List<LootPoolEntryContainer> newEntries = new ArrayList<>(originalEntries.size());
        for (LootPoolEntryContainer entry : originalEntries) {
            filterContainer(entry, changed).ifPresent(newEntries::add);
        }
        boolean structSame = newEntries.size() == originalEntries.size();
        if (structSame) {
            for (int i = 0; i < newEntries.size(); i++) {
                if (newEntries.get(i) != originalEntries.get(i)) {
                    structSame = false;
                    break;
                }
            }
        }
        if (structSame) {
            return pool;
        }
        changed.set(true);
        return poolToBuilder(pool).build();
    }

    private static Optional<LootPoolEntryContainer> filterContainer(LootPoolEntryContainer container, AtomicBoolean changed) {
        if (container instanceof LootItem lootItem) {
            if (Utils.isDisabled(((IOLootItemAccess) (Object) lootItem).io$getItem().value())) {
                changed.set(true);
                return Optional.empty();
            }
            return Optional.of(lootItem);
        }
        if (container instanceof AlternativesEntry alternatives) {
            return filterComposite(alternatives, childrenOf(alternatives), changed, LootTableStripper::assembleAlternatives);
        }
        if (container instanceof EntryGroup group) {
            return filterComposite(group, childrenOf(group), changed, LootTableStripper::assembleEntryGroup);
        }
        if (container instanceof SequentialEntry sequence) {
            return filterComposite(sequence, childrenOf(sequence), changed, LootTableStripper::assembleSequential);
        }
        return Optional.of(container);
    }

    private static List<LootPoolEntryContainer> childrenOf(CompositeEntryBase composite) {
        return ((IOCompositeEntryBaseAccess) (Object) composite).io$getChildren();
    }

    private static Optional<LootPoolEntryContainer> filterComposite(
        LootPoolEntryContainer parent,
        List<LootPoolEntryContainer> children,
        AtomicBoolean changed,
        CompositeAssembler assembler
    ) {
        List<LootPoolEntryContainer> next = new ArrayList<>(children.size());
        for (LootPoolEntryContainer child : children) {
            filterContainer(child, changed).ifPresent(next::add);
        }
        if (next.isEmpty()) {
            if (!children.isEmpty()) {
                changed.set(true);
            }
            return Optional.empty();
        }
        boolean structSame = next.size() == children.size();
        if (structSame) {
            for (int i = 0; i < next.size(); i++) {
                if (next.get(i) != children.get(i)) {
                    structSame = false;
                    break;
                }
            }
        }
        if (structSame) {
            return Optional.of(parent);
        }
        changed.set(true);
        return Optional.of(assembler.assemble(parent, next));
    }

    private static LootPoolEntryContainer assembleAlternatives(LootPoolEntryContainer parent, List<LootPoolEntryContainer> children) {
        AlternativesEntry.Builder builder = AlternativesEntry.alternatives(wrapBuilders(children));
        copyConditions(parent, builder);
        return builder.build();
    }

    private static LootPoolEntryContainer assembleEntryGroup(LootPoolEntryContainer parent, List<LootPoolEntryContainer> children) {
        EntryGroup.Builder builder = EntryGroup.list(wrapBuilders(children));
        copyConditions(parent, builder);
        return builder.build();
    }

    private static LootPoolEntryContainer assembleSequential(LootPoolEntryContainer parent, List<LootPoolEntryContainer> children) {
        SequentialEntry.Builder builder = SequentialEntry.sequential(wrapBuilders(children));
        copyConditions(parent, builder);
        return builder.build();
    }

    private static LootPoolEntryContainer.Builder<?>[] wrapBuilders(List<LootPoolEntryContainer> children) {
        LootPoolEntryContainer.Builder<?>[] builders = new LootPoolEntryContainer.Builder<?>[children.size()];
        for (int i = 0; i < children.size(); i++) {
            builders[i] = wrapBuilt(children.get(i));
        }
        return builders;
    }

    private static void copyConditions(LootPoolEntryContainer parent, LootPoolEntryContainer.Builder<?> builder) {
        for (LootItemCondition condition : ((IOLootPoolEntryContainerAccess) (Object) parent).io$getConditions()) {
            builder.when(() -> condition);
        }
    }

    private static LootPoolEntryContainer.Builder<?> wrapBuilt(LootPoolEntryContainer built) {
        return new FixedLootEntryBuilder(built);
    }

    private static final class FixedLootEntryBuilder extends LootPoolEntryContainer.Builder<FixedLootEntryBuilder> {
        private final LootPoolEntryContainer built;

        private FixedLootEntryBuilder(LootPoolEntryContainer built) {
            this.built = built;
        }

        @Override
        protected FixedLootEntryBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.built;
        }
    }
}
