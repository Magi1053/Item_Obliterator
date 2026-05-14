package elocindev.item_obliterator.neoforge.utils;

import javax.annotation.Nullable;

import elocindev.item_obliterator.neoforge.ItemObliterator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Utils {

    public static String getItemId(Item item) {
        // BuiltinRegistries.ITEM is deprecated in newer versions, but still works for now. Will update later
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static boolean shouldRecipeBeDisabled(Item item) {
        return shouldRecipeBeDisabled(getItemId(item));
    }

    public static boolean shouldRecipeBeDisabled(String itemid) {
        if (isDisabled(itemid)) return true;

        if (!ItemObliterator.Config.use_hashmap_optimizations) {
            for (String blacklisted_id : ItemObliterator.Config.only_disable_recipes) {
                if (blacklisted_id == null || blacklisted_id.startsWith("//")) continue;

                if (blacklisted_id.equals(itemid)) return true;

                if (blacklisted_id.startsWith("!")) {
                    String regex = blacklisted_id.substring(1);
                    if (itemid.matches(regex)) return true;
                }
            }
        } else {
            return ItemObliterator.only_disable_recipes.contains(itemid);
        }

        return false;
    }

    public static boolean isDisabled(String itemid) {
        if (itemid.equals("minecraft:air")) return false;

        if (!ItemObliterator.Config.use_hashmap_optimizations) {
            for (String blacklisted_id : ItemObliterator.Config.blacklisted_items) {
                if (blacklisted_id == null || blacklisted_id.startsWith("//")) continue;

                if (blacklisted_id.equals(itemid)) return true;

                if (blacklisted_id.startsWith("!")) {
                    String regex = blacklisted_id.substring(1);
                    if (itemid.matches(regex)) return true;
                }
            }
        } else {
            return ItemObliterator.blacklisted_items.contains(itemid);
        }

        return false;
    }

    public static boolean isDisabled(ItemStack stack) {
        return isDisabled(stack, null);
    }

    /**
     * @param registryAccess registry context for encoding stack SNBT; when null, uses the current logical server when available
     */
    public static boolean isDisabled(ItemStack stack, @Nullable HolderLookup.Provider registryAccess) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) return false;

        if (!ItemObliterator.Config.blacklisted_nbt.isEmpty()) {
            String customDataString = "";
            if (stack.has(DataComponents.CUSTOM_DATA)) {
                CustomData tag = stack.get(DataComponents.CUSTOM_DATA);
                if (tag != null) {
                    customDataString = tag.toString();
                }
            }

            HolderLookup.Provider registries = registryAccess != null ? registryAccess : defaultRegistryAccess();
            String stackSnbt = "";
            if (ItemObliterator.Config.blacklisted_nbt_match_stack_snbt && registries != null) {
                stackSnbt = encodeStackSnbt(stack, registries);
            }

            for (String blacklisted_nbt : ItemObliterator.Config.blacklisted_nbt) {
                if (blacklisted_nbt == null || blacklisted_nbt.startsWith("//")) continue;

                if (blacklisted_nbt.startsWith("!")) {
                    String regex = blacklisted_nbt.substring(1);
                    if (customDataString.matches(regex) || stackSnbt.matches(regex)) return true;
                } else {
                    if (customDataString.contains(blacklisted_nbt) || stackSnbt.contains(blacklisted_nbt)) return true;
                }
            }
        }

        return isDisabled(getItemId(stack.getItem()));
    }

    private static String encodeStackSnbt(ItemStack stack, HolderLookup.Provider registries) {
        try {
            Tag tag = stack.save(registries);
            return tag != null ? tag.toString() : "";
        } catch (Exception e) {
            ItemObliterator.LOGGER.debug("Item Obliterator: failed to encode stack to SNBT for blacklist check: {}", e.toString());
            return "";
        }
    }

    @Nullable
    private static HolderLookup.Provider defaultRegistryAccess() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        return null;
    }

    // Check if a MerchantOffer has blacklisted items in costs or results
    public static boolean isDisabled(MerchantOffer offer) {
        HolderLookup.Provider registries = defaultRegistryAccess();
        if (isDisabled(offer.getResult(), registries)) return true;
        if (isDisabled(offer.getBaseCostA(), registries)) return true;
        if (offer.getCostB() != null && isDisabled(offer.getCostB(), registries)) return true;
        return false;
    }

    // Get a summary of the MerchantOffer for debugging purposes
    public static String getOfferSummary(MerchantOffer offer) {
        String resultId = getItemId(offer.getResult().getItem());
        String buyAId = getItemId(offer.getBaseCostA().getItem());
        String buyBId = offer.getCostB() != null ? getItemId(offer.getCostB().getItem()) : "none";
        return "BuyA: " + buyAId + ", BuyB: " + buyBId + ", Result: " + resultId;
    }

    public static boolean isDisabledInteract(String itemid) {
        for (String blacklisted_id : ItemObliterator.Config.only_disable_interactions) {
            if (blacklisted_id == null || blacklisted_id.startsWith("//")) continue;

            if (blacklisted_id.equals(itemid)) return true;

            if (blacklisted_id.startsWith("!")) {
                String regex = blacklisted_id.substring(1);
                if (itemid.matches(regex)) return true;
            }
        }

        return false;
    }

    public static boolean isDisabledInteract(ItemStack stack) {
        return stack != null && isDisabledInteract(getItemId(stack.getItem()));
    }

    public static boolean isDisabledAttack(String itemid) {
        for (String blacklisted_id : ItemObliterator.Config.only_disable_attacks) {
            if (blacklisted_id == null || blacklisted_id.startsWith("//")) continue;

            if (blacklisted_id.equals(itemid)) return true;

            if (blacklisted_id.startsWith("!")) {
                String regex = blacklisted_id.substring(1);
                if (itemid.matches(regex)) return true;
            }
        }

        return false;
    }
}
