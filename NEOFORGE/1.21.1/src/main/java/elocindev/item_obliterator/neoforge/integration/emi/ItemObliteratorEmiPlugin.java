package elocindev.item_obliterator.neoforge.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import elocindev.item_obliterator.neoforge.utils.Utils;

@EmiEntrypoint
public class ItemObliteratorEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.removeEmiStacks(ItemObliteratorEmiPlugin::emiStackObliterated);
        registry.removeRecipes(ItemObliteratorEmiPlugin::emiRecipeObliterated);
    }

    private static boolean emiStackObliterated(EmiStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return Utils.isDisabled(stack.getItemStack());
    }

    private static boolean emiRecipeObliterated(EmiRecipe recipe) {
        for (EmiIngredient output : recipe.getOutputs()) {
            for (EmiStack stack : output.getEmiStacks()) {
                if (emiStackObliterated(stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
