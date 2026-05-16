package elocindev.item_obliterator.neoforge.event;

import elocindev.item_obliterator.neoforge.loot.LootTableStripper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class LootTableLoadHandler {
    private LootTableLoadHandler() {}

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTableStripper.apply(event);
    }
}
