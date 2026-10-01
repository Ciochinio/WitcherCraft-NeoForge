package net.redboltmedia.witchercraft;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.common.util.RecipeMatcher;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * One alchemy recipe, loaded from {@code data/<ns>/recipe/**.json} with
 * {@code "type": "witchercraft:alchemy"}:
 *
 * <pre>
 * {
 *   "type": "witchercraft:alchemy",
 *   "category": "potion",
 *   "base": "#witchercraft:alcohol",
 *   "ingredients": ["witchercraft:rebis", "witchercraft:rebis", "witchercraft:vitriol"],
 *   "result": "witchercraft:swallow",
 *   "starter": true
 * }
 * </pre>
 *
 * The base and each ingredient are an item or a {@code #tag}. {@code result} is
 * an item id or {@code {"id": ..., "count": N}}. {@code starter} (default false)
 * and {@code manuscript} (default true) are optional. A recipe with
 * {@code "manuscript": false} never drops as a manuscript (see AlchemyManuscripts);
 * starter recipes never do either.
 *
 * Matching is shapeless and EXACT: the base must match the base slot, and the
 * filled ingredient slots must pair one-to-one with the recipe's ingredients,
 * nothing missing and nothing extra. Repeats are separate slots.
 *
 * Alchemy recipes never appear in the vanilla recipe book: they have no
 * {@link #display()} entries and are marked special.
 *
 * HAND-MAINTAINED: locked code element.
 */
public class AlchemyRecipe implements Recipe<AlchemyInput> {
	public static final int MAX_INGREDIENTS = 5;

	/**
	 * Picks the recipe book tab. Explicit, because alcohol is the base of both
	 * potions and White Gull. White Gull shows in the Decoctions tab.
	 */
	public enum Category implements StringRepresentable {
		POTION("potion"), DECOCTION("decoction"), OIL("oil"), BOMB("bomb"), MUTAGEN("mutagen"), WHITE_GULL("white_gull");

		public static final Codec<Category> CODEC = StringRepresentable.fromEnum(Category::values);
		public static final StreamCodec<RegistryFriendlyByteBuf, Category> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], Category::ordinal).cast();

		private final String name;

		Category(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final MapCodec<AlchemyRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Category.CODEC.fieldOf("category").forGetter(AlchemyRecipe::category),
			Ingredient.CODEC.fieldOf("base").forGetter(AlchemyRecipe::base),
			Ingredient.CODEC.listOf(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(AlchemyRecipe::ingredients),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlchemyRecipe::result),
			Codec.BOOL.optionalFieldOf("starter", false).forGetter(AlchemyRecipe::starter),
			Codec.BOOL.optionalFieldOf("manuscript", true).forGetter(AlchemyRecipe::manuscriptFlag)).apply(i, AlchemyRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AlchemyRecipe> STREAM_CODEC = StreamCodec.composite(
			Category.STREAM_CODEC, AlchemyRecipe::category,
			Ingredient.CONTENTS_STREAM_CODEC, AlchemyRecipe::base,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INGREDIENTS)), AlchemyRecipe::ingredients,
			ItemStackTemplate.STREAM_CODEC, AlchemyRecipe::result,
			ByteBufCodecs.BOOL, AlchemyRecipe::starter,
			ByteBufCodecs.BOOL, AlchemyRecipe::manuscriptFlag,
			AlchemyRecipe::new);

	private final Category category;
	private final Ingredient base;
	private final List<Ingredient> ingredients;
	private final ItemStackTemplate result;
	private final boolean starter;
	private final boolean manuscript;

	public AlchemyRecipe(Category category, Ingredient base, List<Ingredient> ingredients, ItemStackTemplate result, boolean starter, boolean manuscript) {
		this.category = category;
		this.base = base;
		this.ingredients = List.copyOf(ingredients);
		this.result = result;
		this.starter = starter;
		this.manuscript = manuscript;
	}

	public Category category() {
		return category;
	}

	public Ingredient base() {
		return base;
	}

	public List<Ingredient> ingredients() {
		return ingredients;
	}

	public ItemStackTemplate result() {
		return result;
	}

	public boolean starter() {
		return starter;
	}

	/** The JSON {@code manuscript} flag as written. */
	public boolean manuscriptFlag() {
		return manuscript;
	}

	/** Whether manuscripts may teach this recipe: never for starters or {@code "manuscript": false}. */
	public boolean dropsAsManuscript() {
		return manuscript && !starter;
	}

	// ---- matching -------------------------------------------------------------

	@Override
	public boolean matches(AlchemyInput input, Level level) {
		if (!base.test(input.base()))
			return false;
		List<ItemStack> filled = input.filledIngredients();
		if (filled.size() != ingredients.size())
			return false;
		// one-to-one pairing; a tag and a specific item can overlap, so this is a
		// real matching, not a per-slot test
		return RecipeMatcher.findMatches(filled, ingredients) != null;
	}

	@Override
	public ItemStack assemble(AlchemyInput input) {
		return result.create();
	}

	// ---- vanilla recipe plumbing (alchemy stays out of the vanilla book) --------

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public RecipeSerializer<AlchemyRecipe> getSerializer() {
		return AlchemyRecipes.SERIALIZER.get();
	}

	@Override
	public RecipeType<AlchemyRecipe> getType() {
		return AlchemyRecipes.TYPE.get();
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		// required by the interface; unused, since display() is empty
		return RecipeBookCategories.CRAFTING_MISC;
	}
}
