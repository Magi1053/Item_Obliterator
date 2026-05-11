package elocindev.item_obliterator.neoforge.integration.jei;

import elocindev.item_obliterator.neoforge.ItemObliterator;
import elocindev.item_obliterator.neoforge.utils.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ItemObliteratorJeiPlugin implements IModPlugin {

    private static final List<RecipeType<?>> RECIPE_TYPES_TO_SCAN = List.of(
        RecipeTypes.CRAFTING,
        RecipeTypes.STONECUTTING,
        RecipeTypes.SMELTING,
        RecipeTypes.SMOKING,
        RecipeTypes.BLASTING,
        RecipeTypes.CAMPFIRE_COOKING,
        RecipeTypes.SMITHING,
        RecipeTypes.BREWING,
        RecipeTypes.ANVIL,
        RecipeTypes.FUELING,
        RecipeTypes.COMPOSTING,
        RecipeTypes.GRINDSTONE,
        RecipeTypes.INFORMATION
    );

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ItemObliterator.MODID, "jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        removeBlacklistedIngredients(runtime);
        hideRecipesWithObliteratedOutputs(runtime);
    }

    private static void removeBlacklistedIngredients(IJeiRuntime runtime) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String id : ItemObliterator.Config.blacklisted_items) {
            if (id == null || id.startsWith("//") || !id.contains(":")) {
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).ifPresent(item -> stacks.add(new ItemStack(item)));
        }
        if (!stacks.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        }
    }

    private static void hideRecipesWithObliteratedOutputs(IJeiRuntime runtime) {
        IRecipeManager recipeManager = runtime.getRecipeManager();
        for (RecipeType<?> type : RECIPE_TYPES_TO_SCAN) {
            hideMatchingRecipes(recipeManager, type);
        }
    }

    private static <T> void hideMatchingRecipes(IRecipeManager recipeManager, RecipeType<T> type) {
        IRecipeCategory<T> category = recipeManager.getRecipeCategory(type);
        if (category == null) {
            return;
        }
        List<T> toHide = recipeManager.createRecipeLookup(type)
            .includeHidden()
            .get()
            .filter(recipe -> outputsContainObliterated(recipeManager, category, recipe))
            .toList();
        if (!toHide.isEmpty()) {
            recipeManager.hideRecipes(type, toHide);
        }
    }

    private static <T> boolean outputsContainObliterated(IRecipeManager recipeManager, IRecipeCategory<T> category, T recipe) {
        for (ITypedIngredient<?> ingredient : recipeManager.getRecipeIngredients(category, recipe).getIngredients(RecipeIngredientRole.OUTPUT)) {
            if (ingredient.getItemStack().map(Utils::isDisabled).orElse(false)) {
                return true;
            }
        }
        return false;
    }
}
