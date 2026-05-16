package elocindev.item_obliterator.neoforge.mixin.access;

import java.util.List;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeEntryBase.class)
public interface IOCompositeEntryBaseAccess {
    @Accessor("children")
    List<LootPoolEntryContainer> io$getChildren();
}
