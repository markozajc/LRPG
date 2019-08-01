package com.github.markozajc.lrpg.game;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.BotUtils;
import com.github.markozajc.lithium.utilities.dialogs.waiter.BooleanDialog;
import com.github.markozajc.lrpg.game.Enemies.BossInformationDatabase;
import com.github.markozajc.lrpg.game.Enemies.EnemyDatabase;
import com.github.markozajc.lrpg.game.Enemies.EnemyInformation;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.BattleItem;
import com.github.markozajc.lrpg.game.Items.BattleItemDatabase;
import com.github.markozajc.lrpg.game.Items.DungeonItem;
import com.github.markozajc.lrpg.game.Items.GearDatabase;
import com.github.markozajc.lrpg.game.Items.GearItem;
import com.github.markozajc.lrpg.game.Items.HealingItem;
import com.github.markozajc.lrpg.game.Items.HealingItemDatabase;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.Items.ItemDatabase;
import com.github.markozajc.lrpg.game.Items.ItemRarityPack;
import com.github.markozajc.lrpg.game.Items.UsableItem;
import com.github.markozajc.lrpg.game.Items.UsableItemDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.LRpgExposed.IdentifiableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithRarity;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithReputation;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class Utilities {

	private Utilities() {}

	public static final IntUnaryOperator CALCULATE_MAX_HEALTH = lvl -> 40 + lvl * 2;
	public static final LongFunction<RegionDatabase> GET_REGION = rep -> {
		for (int i = 0; i < RegionDatabase.values().length; i++)
			if (rep < RegionDatabase.values()[i].getReputation())
				return RegionDatabase.values()[i - 1];

		return RegionDatabase.values()[RegionDatabase.values().length - 1];
	};

	public static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static <T extends ObjectWithReputation> List<T> getObjectsWithReputation(List<T> objects, long reputation) {
		return objects.stream().filter(i -> i.getReputation() < reputation).collect(Collectors.toList());
	}

	//// CAPAT x4
	public static int capAt(int number, int maxCap) {
		return capAt(number, maxCap, 0);
	}

	public static int capAt(int number, int maxCap, int minCap) {
		if (number > maxCap) {
			return maxCap;

		} else if (number < minCap) {
			return minCap;

		} else {
			return number;
		}
	}

	public static float capAt(float number, float maxCap) {
		return capAt(number, maxCap, 0f);
	}

	public static float capAt(float number, float maxCap, float minCap) {
		if (number > maxCap) {
			return maxCap;

		} else if (number < minCap) {
			return minCap;

		} else {
			return number;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// RANDOM VALUES
	//////////////////////////////////////////////////////////////////////////////////////
	public static final LongUnaryOperator HALF_UP_OR_DOWN_RANDOMIZE = base -> {

		int amount = BotUtils.getRandom().nextInt(Math.round(base * .5f));
		// Gets a random amount ranging from 0 to half of the base

		if (BotUtils.getRandom().nextBoolean())
			amount *= -1;
		// Negates the number half of the time

		return base + amount;
	};
	////

	public static int calculateAttack(int attackArgument, int opposingDefense, boolean critical) {
		float defenseFloat = opposingDefense * .01f;
		// Converts the defense (0 - 100) to a float (0.0 - 1.0)

		int attackValue = Math.round(attackArgument * (1f - defenseFloat));
		// Calculates the base attack

		if (critical)
			attackValue *= 2;
		// Doubles the attack if critical

		if (attackValue <= 3 && Utilities.getChance(defenseFloat))
			attackValue = 0;
		// Nullifies (dodges) the attack under a condition

		if (attackValue < 0)
			attackValue = 0;
		// Caps the attack at 0

		LRpgExposed.LOG.debug("Calculated attack value of {} with arguments atk = {}, def = {}, crit = {}", attackValue,
			attackArgument, opposingDefense, critical);

		return attackValue;
	}

	@Nonnull
	public static <T> T getRandomMustMatch(@Nonnull T[] array, @Nonnull Predicate<T> predicate) {
		return getRandomElement(Arrays.asList(array).stream().filter(predicate).collect(Collectors.toList()));
	}

	public static boolean getChance(@Nonnegative float chance) {
		boolean result = BotUtils.getRandom().nextFloat() <= chance;
		LRpgExposed.LOG.debug("Randomly evaluated chance of {} to {}.", chance, result);
		return result;
	}

	public static int intRange(int min, int max) {
		return BotUtils.getRandom().nextInt(max - min + 1) + min;
	}

	@Nonnegative
	public static int getRandomGold(Player player, float topPercentage) {
		return Math.round(player.getGold() * (BotUtils.getRandom().nextFloat() * topPercentage)) + 1;
	}

	// GETRANDOMELEMENT x2

	public static <T> T getRandomElement(T[] array) {
		if (array.length <= 0)
			throw Assets.UTILITIES_EMPTY_CONTAINER_EXCEPTION;

		return array[BotUtils.getRandom().nextInt(array.length)];
	}

	@SuppressWarnings("null")
	@Nonnull
	public static <T> T getRandomElement(List<T> list) {
		if (list.isEmpty())
			throw Assets.UTILITIES_EMPTY_CONTAINER_EXCEPTION;

		return list.get(BotUtils.getRandom().nextInt(list.size()));
	}

	/// GETRANDOMITEM x3

	@Nullable
	public static Item getRandomItem(long reputation, @Nonnull ItemRarityPack... items) {
		return getRandomItem(reputation, 1f, items);
	}

	@Nullable
	public static Item getRandomItem(long reputation, float chanceMultiplier, @Nonnull ItemRarityPack... items) {
		return getRandomItemWithFallback(reputation, chanceMultiplier, null, items);
	}

	@SuppressWarnings("null")
	@Nullable
	public static Item getRandomItemWithFallback(long reputation, float chanceMultiplier, @Nullable ItemRarityPack fallback, @Nonnull ItemRarityPack... items) {
		ItemRarityPack selected = getRandomValue(Arrays.asList(items), chanceMultiplier);
		// Selects a random ItemRarityPack based on its rarity

		if (selected == null) {
			if (fallback == null)
				return null;

			selected = fallback;
		}
		// Use fallback if nothing is selected

		return selected.maybeGetItem(reputation);
		// Selects a random object
	}

	// GETRANDOMVALUE x2

	@Nullable
	public static <T extends ObjectWithRarity> T getRandomValue(@Nonnull List<T> objects) {
		return getRandomValue(objects, 1f);
	}

	@Nullable
	public static <T extends ObjectWithRarity> T getRandomValue(@Nonnull List<T> objects, float multiplier) {
		Collections.sort(objects, Assets.OBJECTWITHRARITY_COMPARATOR);
		for (T object : objects) {
			if (getChance(object.getRarity() * multiplier))
				return object;
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// FRONTEND / USER INTERACTION LAYER
	//////////////////////////////////////////////////////////////////////////////////////
	@Nonnull
	public static Field getGearField(@Nonnull GameInfo game) {
		return new Field("Gear", "Armor: " + game.getPlayer().getArmor().getNameWithEmote() + "\nWeapon: "
				+ game.getPlayer().getWeapon().getNameWithEmote(), true);
	}

	@Nonnull
	public static String plural(@Nonnull String countableNoun, int number) {
		if (number == 1)
			return countableNoun;

		return countableNoun + "s";
	}

	@SuppressWarnings("null")
	@Nonnull
	public static String getXpBar(@Nonnull Player player) {
		StringBuilder result = new StringBuilder();
		int lvl = player.getLevel();
		long xpForLvl = Player.calculateXp(lvl);
		result.append("**Level " + lvl + "**");
		result.append(
			" " + displayProgress(player.getXp() - xpForLvl, Player.calculateXp(lvl + 1L) - xpForLvl, 14) + " ");
		result.append("**Level " + (lvl + 1) + "**");
		result.append(" (" + (player.getXp() - xpForLvl) + "/" + (Player.calculateXp(lvl + 1L) - xpForLvl) + ")");

		return result.toString();
	}

	public static void confirmExit(CommandContext context, Runnable back) {
		new BooleanDialog(context, Assets.EXIT_MESSAGE, e -> {
			if (!e) {
				back.run();
			} else {
				Assets.BYE_MESSAGE.display(context.getChannel());
			}
		}).display(context.getChannel());
	}

	// DISPLAYPROGRESS x2

	@Nonnull
	public static String displayProgress(int value, int max) {
		return displayProgress(value, max, 10);
	}

	@SuppressWarnings("null")
	@Nonnull
	public static String displayProgress(long value, long max, int size) {
		int characters = (int) Math.round((double) value / (double) max * size);

		StringBuilder sb = new StringBuilder("**");
		for (int i = 0; i < characters; i++)
			sb.append("\u25B0");
		for (int i = 0; i < size - characters; i++)
			sb.append("\u25B1");
		sb.append("**");

		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// DATA MANAGEMENT
	//////////////////////////////////////////////////////////////////////////////////////
	public static class IdentifiableObjectTypeAdapter<T extends IdentifiableObject>
			implements JsonSerializer<T>, JsonDeserializer<T> {

		private final Function<String, T> unpackFunction;

		IdentifiableObjectTypeAdapter(Function<String, T> unpackFunction) {
			this.unpackFunction = unpackFunction;
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			return this.unpackFunction.apply(json.getAsString());
		}

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(PACK_IDENTIFIABLE.apply(src));
		}

	}

	@SuppressWarnings("null")
	public static final Function<String, EnemyInformation> UNPACK_ENEMY = id -> {
		String[] split = id.split(":");

		EnemyInformation[] database;

		switch (split[0]) {
			case "E":
				database = EnemyDatabase.values();
				break;

			case "B":
				database = BossInformationDatabase.values();
				break;

			default:
				return null;
		}

		return findFromDatabase(database, split[1]);
	};
	@SuppressWarnings("null")
	public static final Function<String, Item> UNPACK_ITEM = id -> {
		String[] split = id.split(":");

		if (split[0].startsWith("WI")) {
			return new WeaponItem(findFromDatabase(WeaponDatabase.values(), split[1]),
					Integer.parseInt(split[0].split("-")[1]));

		} else if (split[0].startsWith("AI")) {
			return new ArmorItem(findFromDatabase(ArmorDatabase.values(), split[1]),
					Integer.parseInt(split[0].split("-")[1]));
		}

		Item[] database;

		switch (split[0]) {
			case "I":
				database = ItemDatabase.values();
				break;

			case "UI":
				database = UsableItemDatabase.values();
				break;

			case "BI":
				database = BattleItemDatabase.values();
				break;

			case "HI":
				database = HealingItemDatabase.values();
				break;

			default:
				return null;
		}

		return findFromDatabase(database, split[1]);
	};
	public static final Function<IdentifiableObject, String> PACK_IDENTIFIABLE = io -> io.getDatabaseToken() + ":"
			+ io.getIdentification();
	////

	public static <T extends IdentifiableObject> IdentifiableObjectTypeAdapter<T> getTypeAdapterFactory(Function<String, T> unpackFunction) {
		return new IdentifiableObjectTypeAdapter<>(unpackFunction);
	}

	@SuppressWarnings("null")
	@Nonnull
	public static <T extends GearDatabase> GearItem<T>[] gearDatabaseToGearItems(@Nonnull GearDatabase[] database) {
		List<GearItem<?>> result = new ArrayList<>();

		for (GearDatabase gear : database) {
			if (gear instanceof WeaponDatabase) {
				if (!gear.equals(WeaponDatabase.FISTS))
					result.add(new WeaponItem((WeaponDatabase) gear, 0));
			} else {
				if (!gear.equals(ArmorDatabase.NAKED))
					result.add(new ArmorItem((ArmorDatabase) gear, 0));
			}
		}

		return result.toArray(new GearItem[result.size()]);
	}

	@Nullable
	public static <T extends IdentifiableObject> T findFromDatabase(T[] database, @Nonnull String query) {
		for (T object : database)
			if (object.getIdentification().equals(query))
				return object;

		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ITEM PREDICATES
	//////////////////////////////////////////////////////////////////////////////////////
	public static final Predicate<Item> NO_UNUSABLE_ITEMS = item -> item instanceof UsableItem;
	public static final Predicate<Item> NO_GEAR_ITEMS = item -> !(item instanceof GearItem);
	public static final Predicate<Item> NO_FIGHT_ITEMS = item -> !(item instanceof BattleItem);
	public static final Predicate<Item> NO_DUNGEON_ITEMS = item -> !(item instanceof DungeonItem);
	public static final Predicate<Item> HEALING_ITEMS = item -> item instanceof HealingItem;
	public static final Predicate<Item> FIGHT_PICK = NO_GEAR_ITEMS.and(NO_UNUSABLE_ITEMS);
	public static final Predicate<Item> DUNGEON_PICK = NO_GEAR_ITEMS.and(NO_FIGHT_ITEMS)
			.or(HEALING_ITEMS)
			.and(NO_UNUSABLE_ITEMS);
	public static final Predicate<Item> CASTLE_PICK = NO_DUNGEON_ITEMS.and(NO_FIGHT_ITEMS).and(NO_UNUSABLE_ITEMS);

}
