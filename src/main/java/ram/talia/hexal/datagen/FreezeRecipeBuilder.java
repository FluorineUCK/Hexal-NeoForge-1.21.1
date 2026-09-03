package ram.talia.hexal.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import ram.talia.hexal.common.recipe.FreezeRecipe;

import java.util.LinkedHashMap;
import java.util.Map;

/** 1.21 recipe/datagen adapter for Hexal's block-state freeze recipe. */
public final class FreezeRecipeBuilder implements RecipeBuilder {
    private final Block blockIn;
    private final BlockState result;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public FreezeRecipeBuilder(Block blockIn, BlockState result) {
        this.blockIn = blockIn;
        this.result = result;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return result.getBlock().asItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }

        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        criteria.forEach(advancement::addCriterion);

        output.accept(
                id,
                new FreezeRecipe(blockIn, result),
                advancement.build(id.withPrefix("recipes/freeze/"))
        );
    }
}
