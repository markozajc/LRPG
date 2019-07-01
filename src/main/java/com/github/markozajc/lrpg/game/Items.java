package com.github.markozajc.lrpg.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.exceptions.runtime.NumberOverflowException;
import com.github.markozajc.lithium.utilities.BotUtils;
import com.github.markozajc.lithium.utilities.Parser;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.EventWaiterDialog;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.InventoryChoiceDialog.InventoryAction;
import com.github.markozajc.lrpg.game.LRpgExposed.EmotableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.IdentifiableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.NamedObject;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithRarity;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithReputation;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithSpeed;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValueObject;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;
import com.google.gson.annotations.SerializedName;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Items {

	public static interface Item
			extends EmotableObject, ObjectWithRarity, NamedObject, IdentifiableObject, ObjectWithReputation {

		public String getDescription();

		public default String getNameWithEmote() {
			return getEmote() + " " + getName();
		}

		public default long getBasePrice() {
			return Math.round((1f - getRarity()) * 100f) + 10 + getReputation();
		}

		public default MessageDialog getForbiddenUsageDialog() {
			return Assets.ITEM_CANT_USE_PREPARED.generate(this);
		}

	}

	public enum ItemDatabase implements Item {
		ANKH("Blessed Ankh",
				"Used to resurrect beings. You need a resurrection device if you'll want to use it for anything serious.",
				"<:Ankh:495642000872439818>", 5000, .005f),
		KEY("Key",
				"Used to unlock a locked chest. This kind of key gets stuck in the keyhole by design, meaning you can only use it on one chest.",
				"<:Key:496689661499015178>", 0, .55f);

		@Nonnull
		private final String name;
		@Nonnull
		private final String description;
		@Nonnull
		private final String emote;
		@Nonnegative
		private final long reputation;
		@Nonnegative
		private final float rarity;

		private ItemDatabase(@Nonnull String name, @Nonnull String description, @Nonnull String emote,
				@Nonnegative long reputation, @Nonnegative float rarity) {
			this.name = name;
			this.description = description;
			this.emote = emote;
			this.reputation = reputation;
			this.rarity = rarity;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Override
		public float getRarity() {
			return this.rarity;
		}

		@Override
		public String getDatabaseToken() {
			return "I";
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSUMABLE / USABLE
	//////////////////////////////////////////////////////////////////////////////////////

	public static interface UsableItem extends Item, ObjectWithSpeed {

		public void use(GameInfo game, Consumer<Boolean> callback);

	}

	public static interface DungeonItem extends UsableItem {

		public void use(DungeonInfo dungeon, Consumer<Boolean> callback);

		@Override
		public default void use(GameInfo game, Consumer<Boolean> callback) {
			if (game instanceof DungeonInfo)
				use((DungeonInfo) game, callback);

			getForbiddenUsageDialog().display(game.getChannel());
			callback.accept(false);
		}

		@Override
		default MessageDialog getForbiddenUsageDialog() {
			return Assets.ITEM_ONLY_DUNGEON_PREPARED.generate(this);
		}

	}

	public abstract static class GearItem<T extends GearDatabase> implements UsableItem, RangedValueObject {

		public static final int MAX_GEAR_LEVEL = 5;
		public static final float RARITY = .1f;

		protected T type;
		protected int level;

		public GearItem(T type, int level) {
			this.type = type;
			this.level = level;
		}

		public abstract String getEffectivenessDescription();

		public int getLevel() {
			return this.level;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public T getType() {
			return this.type;
		}

		@Override
		public String getDescription() {
			return "This **tier " + this.type.getTier()
					+ "** piece of gear drastically eases fighting. You loose all your gear if you die in a battle. "
					+ "Your gear can't be changed while you're in the dungeon. " + getEffectivenessDescription();
		}

		@Override
		public String getName() {
			return this.type.getName() + " +" + getLevel();
		}

		@Override
		public String getEmote() {
			return this.type.getEmote();
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.level);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GearItem<?> other = (GearItem<?>) obj;
			return Objects.equals(this.type, other.type) && this.level == other.level;
		}

		@Override
		public final void use(GameInfo game, Consumer<Boolean> callback) {
			if (game instanceof DungeonInfo) {
				getForbiddenUsageDialog().display(game.getChannel());
				callback.accept(false);
			} else {
				equip(game);
				callback.accept(true);
			}
		}

		public abstract void equip(GameInfo game);

		@Override
		public long getReputation() {
			return this.type.getReputation();
		}

		@Override
		public float getRarity() {
			return RARITY;
		}

		@Override
		public MessageDialog getForbiddenUsageDialog() {
			return Assets.GEAR_CANT_EQUIP_MESSAGE;
		}

	}

	public static interface BattleItem extends DungeonItem {

		public void use(FightInfo fight, Consumer<String> callback);

		@Override
		public default void use(GameInfo game, Consumer<Boolean> callback) {
			if (game instanceof DungeonInfo) {
				use((DungeonInfo) game, callback);
			} else {
				getForbiddenUsageDialog().display(game.getChannel());
				callback.accept(false);
			}
		}

		@Override
		default MessageDialog getForbiddenUsageDialog() {
			return Assets.ITEM_ONLY_DUNGEON_FIGHT_MESSAGE;
		}

	}

	public static interface HealingItem extends BattleItem {

		public int getHealingValue();

		@Override
		default String getDescription() {
			return "This healing item can be used to ease the situation while in the dungeon. It restores **"
					+ getHealingValue() + "** HP.";
		}

		@Override
		default void use(DungeonInfo dungeon, Consumer<Boolean> callback) {
			if (dungeon.getPlayerDungeon().getHp() == dungeon.getPlayer().getMaxHp()) {
				Assets.HEALIE_NOGAIN_MESSAGE.display(dungeon.getChannel());
				callback.accept(false);

			} else {
				dungeon.getPlayerDungeon().getStatistics().healieConsumed();
				dungeon.getPlayerDungeon()
						.setHp(dungeon.getPlayerDungeon().getHp() + getHealingValue(), dungeon.getPlayer().getMaxHp());
				Assets.HEALIE_GAIN_PREPARED.generate(getHealingValue()).display(dungeon.getChannel());
				callback.accept(true);
			}
		}

		@Override
		default void use(FightInfo fight, Consumer<String> callback) {
			if (fight.getPlayerDungeon().getHp() == fight.getPlayer().getMaxHp()) {
				callback.accept(fight.getAuthor().getName() + " consumes a " + getName() + ". It doesn't do much as "
						+ fight.getAuthor().getName() + " was already at full health.");

			} else {
				fight.getPlayerDungeon().getStatistics().healieConsumed();
				fight.getPlayerDungeon()
						.setHp(fight.getPlayerDungeon().getHp() + getHealingValue(), fight.getPlayer().getMaxHp());

				callback.accept(fight.getAuthor().getName() + " consumes a " + getName() + ". "
						+ fight.getAuthor().getName() + " gained " + getHealingValue() + " HP.");

			}
		}
	}

	public enum UsableItemDatabase implements UsableItem {
		@SuppressWarnings("null")
		SCROLL_UPGRADE("Scroll of Upgrade", Assets.SCROLL_UPGRADE_DESCRIPTION_TEXT,
				"<:ScrollOfUpgrade:495591594628743169>", 20, .05f, 1,
				(game, callback) -> new ChoiceDialog(game.getContext(),
						Assets.UPGRADE_PREPARED.generate(game.getPlayer()), choice -> {
							if (choice == 2) {
								callback.accept(false);
							} else {

								GearItem<?> gear;

								if (choice == 0) {
									gear = game.getPlayer().getWeapon();
								} else if (choice == 1) {
									gear = game.getPlayer().getArmor();
								} else {
									callback.accept(false);
									return;
								}

								if (gear.getLevel() >= GearItem.MAX_GEAR_LEVEL) {
									game.getChannel()
											.sendMessage("You can't upgrade your gear to a level higher than **"
													+ GearItem.MAX_GEAR_LEVEL + "**.")
											.queue();

									callback.accept(false);
								}

								if (gear.getType().equals(ArmorDatabase.NAKED)) {
									game.getChannel()
											.sendMessage(
												BotUtils.buildEmbed("You can't upgrade your bare skin.", Constants.RED))
											.queue();

									callback.accept(false);

								} else if (gear.getType().equals(WeaponDatabase.FISTS)) {
									game.getChannel()
											.sendMessage(
												BotUtils.buildEmbed("You can't upgrade your fists.", Constants.RED))
											.queue();

									callback.accept(false);
								} else {
									gear.setLevel(gear.getLevel() + 1);
									game.getChannel()
											.sendMessage(BotUtils.buildEmbed(Assets.UP_EMOTE + " You upgrade your **"
													+ gear.getEmote() + " " + gear.getType().getName()
													+ "** to level **" + gear.getLevel() + "**.",
												Constants.GREEN))
											.queue();
									// Manually getting the name + emote to avoid the old level sneaking in

									callback.accept(true);
								}
							}
						}, "w", "a", "exit").display(game.getChannel())),

		@SuppressWarnings("null")
		POTION_EXP("Potion of Experience", Assets.POTION_EXPERIENCE_DESCRIPTION_TEXT, "<:ExpPotion:495592469908553749>",
				0, 0.05f, 1, game -> {
					long xp = game.getPlayer().getXpRequired();
					game.getPlayer().setXp(game.getPlayer().getXp() + xp);
					Assets.POTION_EXPERIENCE_GAIN_PREPARED.generate(xp).display(game.getChannel());
					game.getChannel().sendMessage("You gained " + xp + " XP!").queue();
				});

		@Nonnull
		private final String name;
		@Nonnull
		private final String description;
		@Nonnull
		private final String emote;
		private final long reputation;
		private final float rarity;
		private final float speed;
		@Nonnull
		private final BiConsumer<GameInfo, Consumer<Boolean>> action;

		private UsableItemDatabase(@Nonnull String name, @Nonnull String description, @Nonnull String emote,
				@Nonnegative long reputation, @Nonnegative float rarity, @Nonnegative float speed,
				@Nonnull Consumer<GameInfo> action) {
			this.name = name;
			this.description = description;
			this.emote = emote;
			this.reputation = reputation;
			this.rarity = rarity;
			this.speed = speed;
			this.action = (game, callback) -> {
				action.accept(game);
				callback.accept(true);
			};
		}

		private UsableItemDatabase(@Nonnull String name, @Nonnull String description, @Nonnull String emote,
				@Nonnegative long reputation, @Nonnegative float rarity, @Nonnegative float speed,
				@Nonnull BiConsumer<GameInfo, Consumer<Boolean>> action) {
			this.name = name;
			this.description = description;
			this.emote = emote;
			this.reputation = reputation;
			this.rarity = rarity;
			this.speed = speed;
			this.action = action;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Override
		public float getRarity() {
			return this.rarity;
		}

		@Override
		public void use(GameInfo game, Consumer<Boolean> callback) {
			this.action.accept(game, callback);
		}

		@Override
		public String getDatabaseToken() {
			return "UI";
		}

		@Override
		public float getSpeed() {
			return this.speed;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

	}

	public enum BattleItemDatabase implements BattleItem {
		@SuppressWarnings("null")
		SCROLL_WIPEOUT("Scroll of Wipeout", Assets.SCROLL_WIPEOUT_DESCRIPTION_TEXT,
				"<:WipeoutScroll:495642000926834698>", 2000, .005f, 1, (fight, callback) -> {
					if (fight.getPlayerFight().getEnemy().getInfo().isBoss()) {
						callback.accept(fight.getAuthor().getName() + " reads the Scroll of Wipeout. "
								+ fight.getPlayerFight().getEnemy().getInfo().getName()
								+ " seems to be unaffected by it. Maybe you have chosen an enemy that's too strong?");
					} else {
						fight.getPlayerFight().getEnemy().decreaseHp(fight.getPlayerFight().getEnemy().getHp());

						callback.accept(fight.getAuthor().getName() + " reads the Scroll of Wipeout. "
								+ fight.getPlayerFight().getEnemy().getInfo().getName()
								+ " dissolves into a white liquid.");
					}
				});

		@Nonnull
		private final String name;
		@Nonnull
		private final String description;
		@Nonnull
		private final String emote;
		@Nonnegative
		private final long reputation;
		@Nonnegative
		private final float rarity;
		@Nonnegative
		private final float speed;
		@Nonnull
		private final BiConsumer<FightInfo, Consumer<String>> action;

		private BattleItemDatabase(@Nonnull String name, @Nonnull String description, @Nonnull String emote,
				@Nonnegative long reputation, @Nonnegative float rarity, @Nonnegative float speed,
				@Nonnull BiConsumer<FightInfo, Consumer<String>> action) {
			this.name = name;
			this.description = description;
			this.emote = emote;
			this.reputation = reputation;
			this.rarity = rarity;
			this.speed = speed;
			this.action = action;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getDescription() {
			return this.description;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public void use(FightInfo fight, Consumer<String> callback) {
			this.action.accept(fight, callback);
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Override
		public float getRarity() {
			return this.rarity;
		}

		@Override
		public String getDatabaseToken() {
			return "BI";
		}

		@Override
		public float getSpeed() {
			return this.speed;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public void use(DungeonInfo dungeon, Consumer<Boolean> callback) {
			Assets.ITEM_ONLY_FIGHT_MESSAGE.display(dungeon.getChannel());
			callback.accept(false);
		}
	}

	public enum HealingItemDatabase implements HealingItem {
		POTION_HEALING("Potion of Healing", "<:HealingPotion:495592469900034050>", 1000),
		FOOD_RATION("Ration of Food", "<:FoodRation:495642001086218250>", 30),
		FOOD_MEAT_RAW("Raw Steak", "<:RawMeat:495642000834428928>", 10),
		FOOD_MEAT_COOKED("Cooked Steak", "<:CookedMeat:495642000880566292>", 20);

		@Nonnull
		private final String name;
		@Nonnull
		private final String emote;
		@Nonnegative
		private final int heal;

		private HealingItemDatabase(@Nonnull String name, @Nonnull String emote, @Nonnegative int heal) {
			this.name = name;
			this.emote = emote;
			this.heal = heal;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public int getHealingValue() {
			return this.heal;
		}

		@Override
		public long getReputation() {
			return 0;
		}

		@Override
		public float getRarity() {
			return Assets.HEALIE_RARITY;
		}

		@Override
		public String getDatabaseToken() {
			return "HI";
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public float getSpeed() {
			return .5f;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// GEAR
	//////////////////////////////////////////////////////////////////////////////////////
	public static class WeaponItem extends GearItem<WeaponDatabase> {

		private static String getSpeedType(float speed) {
			if (speed < 1)
				return "lightweight";

			if (speed > 1)
				return "heavy";

			if (speed > 2)
				return "extremely heavy";

			return "regular";
		}

		public WeaponItem(WeaponDatabase type, int upgrades) {
			super(type, upgrades);
		}

		@Override
		public String getEffectivenessDescription() {
			return "This **" + getSpeedType(this.getType().getSpeed()) + "** weapon deals **" + getMin() + "-"
					+ getMax() + "** damage to your opponents and takes.";
		}

		@Override
		public void equip(GameInfo game) {

			if (!game.getPlayer().getWeapon().getType().equals(WeaponDatabase.FISTS))
				game.getPlayer().getInventory().addItem(game.getPlayer().getWeapon(), 1);

			game.getPlayer().setWeapon(this);

			game.getChannel()
					.sendMessage(
						BotUtils.buildEmbed("You equip the **" + this.getNameWithEmote() + "**.", Constants.GREEN))
					.queue();
		}

		@Override
		public String getDatabaseToken() {
			return "WI-" + getLevel();
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return getType().name();
		}

		@Override
		public int getMax() {
			return this.getType().getTier() * this.getType().getTier() + this.getLevel() * this.getType().getTier();
		}

		@Override
		public int getMin() {
			return Math.round(getMax() * .8f);
		}

		@Override
		public float getSpeed() {
			throw new UnsupportedOperationException(
					"WeaponItem doesn't have speed. Use WeaponDatabase#getSpeed() instead.");
		}

	}

	public static class ArmorItem extends GearItem<ArmorDatabase> {

		public ArmorItem(ArmorDatabase type, int upgrades) {
			super(type, upgrades);
		}

		@Override
		public void equip(GameInfo game) {

			if (!game.getPlayer().getArmor().getType().equals(ArmorDatabase.NAKED))
				game.getPlayer().getInventory().addItem(game.getPlayer().getArmor(), 1);

			game.getPlayer().setArmor(this);

			game.getChannel()
					.sendMessage(BotUtils.buildEmbed("You put on the **" + getNameWithEmote() + "**.", Constants.GREEN))
					.queue();
		}

		@Override
		public String getEffectivenessDescription() {
			return "This armor absorbs **" + getMin() + "% to " + getMax() + "%** damage.";
		}

		@Override
		public String getDatabaseToken() {
			return "AI-" + getLevel();
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return getType().name();
		}

		@Override
		public int getMax() {
			return this.type.getTier() * 6 + this.getLevel() * 4;
		}

		@Override
		public int getMin() {
			return this.type.getTier() * 5 + this.getLevel() * 3;
		}

		@Override
		public float getSpeed() {
			throw new UnsupportedOperationException("ArmorItem doesn't have speed.");
		}

	}

	public static interface GearDatabase extends EmotableObject, NamedObject, IdentifiableObject, ObjectWithReputation {

		public int getTier();

		public RegionDatabase getRegion();

		@Override
		public default long getReputation() {
			return getRegion().getReputation() + getTier() * 10L;
		}

	}

	public enum WeaponDatabase implements GearDatabase, ObjectWithSpeed {
		@SerializedName("W1")
		FISTS("Fists", 1, 1, RegionDatabase.SEWERS, "<:Fists:495591594649452557>"),

		@SerializedName("W2")
		STICK("Stick", 2, 1, RegionDatabase.SEWERS, "<:Stick:495591594460839949>"),

		@SerializedName("W3")
		DAGGER("Dagger", 2, .5f, RegionDatabase.SEWERS, "<:Dagger:495591594494394389>"),

		@SerializedName("W4")
		PITCHFORK("Pitchfork", 3, 2, RegionDatabase.SEWERS, "<:Pitchfork:495591594616029194>"),

		@SerializedName("W5")
		AXE("Hatchet", 3, 1, RegionDatabase.SEWERS, "<:WoodAxe:495591594620223498>"),

		@SerializedName("W6")
		SHORTSWORD("Shortsword", 4, 1, RegionDatabase.PRISON, "<:ShortSword:495591594435674113>"),

		@SerializedName("W7")
		SPEAR("Spear", 5, 2, RegionDatabase.PRISON, "<:Spear:495591594683269120>"),

		@SerializedName("W8")
		SWORD("Sword", 6, 1, RegionDatabase.CAVES, "<:Sword:495591594347724801>"),

		@SerializedName("W9")
		BAXE("Battle Axe", 7, 2, RegionDatabase.CAVES, "<:BattleAxe:495591594406445067>"),

		@SerializedName("W10")
		GSWORD("Greatsword", 7, 1, RegionDatabase.LOST_CITY, "<:GreatSword:495591594653646889>"),

		@SerializedName("W11")
		ESWORD("Spiritual Sword", 7, .5f, RegionDatabase.LOST_CITY, "<:EnchantedSword:495591594645389342>"),

		@SerializedName("W12")
		HAMMER("War Hammer", 10, 3, RegionDatabase.HELL, "<:HeroHammer:495591594645258280>"),

		@SerializedName("W13")
		HSPEAR("Heroic Spear", 9, 2, RegionDatabase.HELL, "<:HeroSpear:495591594632806430>"),

		@SerializedName("W14")
		HSWORD("Heroic Sword", 9, 1, RegionDatabase.HELL, "<:HeroSword:495591594645520384>");

		@Nonnull
		private final String name;
		@Nonnegative
		private final int tier;
		@Nonnegative
		private final float speed;
		@Nonnull
		private final RegionDatabase region;
		@Nonnull
		private final String emote;

		private WeaponDatabase(@Nonnull String name, @Nonnegative int tier, @Nonnegative float speed,
				@Nonnull RegionDatabase region, @Nonnull String emote) {
			this.name = name;
			this.tier = tier;
			this.speed = speed;
			this.region = region;
			this.emote = emote;
		}

		@Override
		public int getTier() {
			return this.tier;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public RegionDatabase getRegion() {
			return this.region;
		}

		@Override
		public float getSpeed() {
			return this.speed;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public String getDatabaseToken() {
			return "W";
		}

	}

	public enum ArmorDatabase implements GearDatabase {

		@SerializedName("A0")
		NAKED("Nothing", 0, RegionDatabase.SEWERS, "<:Nothing:495590529212350474>"),

		@SerializedName("A1")
		RAGS("Worn Shirt", 1, RegionDatabase.SEWERS, "<:Rags:495590528818085890>"),

		@SerializedName("A2")
		SHIRT("Shirt", 2, RegionDatabase.SEWERS, "<:Shirt:495591594632675334>"),

		@SerializedName("A3")
		ROBE("Robe", 3, RegionDatabase.PRISON, "<:Robe:495591594557440001>"),

		@SerializedName("A4")
		LEATHER("Leather Jacket", 4, RegionDatabase.PRISON, "<:LeatherArmor:495590529153892353>"),

		@SerializedName("A5")
		CHAINMAIL("Chain mail", 5, RegionDatabase.CAVES, "<:ChainMail:495591594272096257>"),

		@SerializedName("A6")
		SCALE("Reinforced Breastplate", 6, RegionDatabase.LOST_CITY, "<:ReinforcedArmor:495642000943480852>"),

		@SerializedName("A7")
		PLATE("Heavy Breastplate", 7, RegionDatabase.HELL, "<:PlateArmor:495642000834560010>"),

		@SerializedName("A8")
		HERO("Heroic Breastplate", 8, RegionDatabase.HELL, "<:HeroArmor:495591594729144320>");

		@Nonnull
		private final String name;
		@Nonnegative
		private final int tier;
		@Nonnull
		private final RegionDatabase region;
		@Nonnull
		private final String emote;

		private ArmorDatabase(@Nonnull String name, @Nonnegative int tier, @Nonnull RegionDatabase region,
				@Nonnull String emote) {
			this.name = name;
			this.tier = tier;
			this.region = region;
			this.emote = emote;
		}

		@Override
		public int getTier() {
			return this.tier;
		}

		@Override
		public String getEmote() {
			return this.emote;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public RegionDatabase getRegion() {
			return this.region;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public String getDatabaseToken() {
			return "A";
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INVENTORY
	//////////////////////////////////////////////////////////////////////////////////////
	public static class Inventory {

		private final List<ItemStack> items = new ArrayList<>();

		public void addItemStack(ItemStack item) {
			addItem(item.getItem(), item.getQuantity());
		}

		public void addItem(Item type, int quantity) {
			ItemStack stack = getItemStack(type);
			if (stack == null) {
				this.items.add(new ItemStack(type, quantity));
			} else {
				stack.setQuantity(stack.getQuantity() + quantity);
			}
		}

		public boolean removeItemStack(ItemStack item) {
			if (this.items.contains(item))
				return this.items.remove(item);

			return removeItem(item.getItem(), item.getQuantity());
		}

		public boolean removeItem(Item type, int quantity) {
			ItemStack stack = getItemStack(type);

			if (stack == null || stack.getQuantity() < quantity)
				return false;

			if (stack.getQuantity() - quantity == 0) {
				this.items.remove(stack);

			} else {
				stack.setQuantity(stack.getQuantity() - quantity);
			}

			return true;
		}

		public int getQuantity(Item type) {
			ItemStack stack = this.getItemStack(type);
			if (stack == null)
				return 0;
			return stack.getQuantity();
		}

		@Nullable
		public ItemStack getItemStack(Item type) {
			return this.items.stream().filter(is -> is.getItem().equals(type)).findAny().orElse(null);
		}

		public List<ItemStack> getItems() {
			return this.items;
		}

	}

	public static void useUsableItem(UsableItem item, GameInfo game, Consumer<Boolean> callback) {
		Consumer<Boolean> removal = used -> {
			if (used)
				game.getPlayer().getInventory().removeItem(item, 1);
		};
		item.use(game, removal.andThen(callback));
	}

	public static void useBattleItem(BattleItem item, FightInfo fight, Consumer<String> callback) {
		Consumer<String> removal = result -> {
			if (result.isEmpty())
				fight.getPlayer().getInventory().removeItem(item, 1);
		};
		item.use(fight, removal.andThen(callback));
	}

	public static class ItemRarityPack implements ObjectWithRarity {

		public final Item[] items;
		public final float rarity;

		public ItemRarityPack(float combinedRarity, Item... items) {
			this.items = items;
			this.rarity = combinedRarity;
		}

		@Nullable
		public Item maybeGetItem(long reputation) {
			List<Item> applicable = Utilities.getObjectsWithReputation(Arrays.asList(this.items), reputation);
			if (applicable.isEmpty())
				return null;

			if (this.rarity < 0f)
				return Utilities.getRandomValue(applicable);

			return Utilities.getRandomElement(applicable);
		}

		public Item[] getItems() {
			return this.items;
		}

		@Override
		public float getRarity() {
			if (this.rarity < 0f)
				throw Assets.ITEMRARITYPACK_NO_RARITY_EXCEPTION;

			return this.rarity;
		}

		public ItemRarityPack changeRarity(float newRarity) {
			return new ItemRarityPack(newRarity, this.items);
		}

		public static ItemRarityPack combine(float rarity, ItemRarityPack... packs) {
			List<Item> newItems = new ArrayList<>(Arrays.asList(packs)
					.stream()
					.map(ItemRarityPack::getItems)
					.flatMap(ia -> Arrays.asList(ia).stream())
					.collect(Collectors.toList()));

			return new ItemRarityPack(rarity, newItems.toArray(new Item[newItems.size()]));
		}

	}

	public static class ItemStack {

		private final Item item;
		private int quantity;

		public ItemStack(Item type, int quantity) {
			this.item = type;
			this.quantity = quantity;
		}

		public Item getItem() {
			return this.item;
		}

		public int getQuantity() {
			return this.quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

	}

	public static void openInventory(GameInfo game, Consumer<Item> itemPicked, Predicate<Item> canPick, Runnable exit) {
		new InventoryChoiceDialog(game, (act, stack) -> {
			if (act.equals(InventoryAction.EXIT)) {
				exit.run();

			} else if (act.equals(InventoryAction.USE)) {
				itemPicked.accept(stack.getItem());
			}
		}, canPick).display(game.getChannel());
	}

	public static void openInventoryRepeating(GameInfo game, Predicate<Item> canPick, Runnable exit) {
		openInventory(game, item -> Items.useUsableItem((UsableItem) item, game,
			u -> openInventoryRepeating(game, canPick.and(Utilities.NO_UNUSABLE_ITEMS), exit)), canPick, exit);
	}

	public static class InventoryChoiceDialog extends EventWaiterDialog<MessageReceivedEvent> {

		public enum InventoryAction {
			USE("u"),
			EXIT("exit");

			private final String keyword;

			private InventoryAction(String keyword) {
				this.keyword = keyword;
			}

			public String getKeyword() {
				return this.keyword;
			}

		}

		private final Predicate<MessageReceivedEvent> isRight;
		private final long userId;

		public InventoryChoiceDialog(GameInfo game, BiConsumer<InventoryAction, ItemStack> action,
				Predicate<Item> isAllowed) {
			super(game.getContext(), MessageReceivedEvent.class,
					Assets.INVENTORY_STATUS_PREPARED.generate(Pair.of(game, isAllowed)), event -> {
						String message = event.getMessage().getContentRaw().toLowerCase();
						if (message.equals(InventoryAction.EXIT.getKeyword())) {
							action.accept(InventoryAction.EXIT, null);
							return;
						}

						String[] actionString = message.split(" ");
						ItemStack item = game.getPlayer()
								.getInventory()
								.getItems()
								.get(Parser.parseInt(actionString[1]) - 1);

						if (!actionString[0].equals(InventoryAction.USE.getKeyword()))
							throw new IllegalStateException(
									"An unknown keyword has passed the isRight predicate but does not match any known keyword: "
											+ actionString[0]);

						action.accept(InventoryAction.USE, item);
					});
			this.isRight = getIsRight(game, isAllowed);
			this.userId = game.getAuthor().getIdLong();
		}

		@Override
		public CompletionStage<Message> display(MessageChannel channel) {
			registerWaiter(this.createWaiter(this.isRight, channel, this.userId));
			return this.getMessageDialog().display(channel);
		}

		private static Predicate<MessageReceivedEvent> getIsRight(GameInfo game, Predicate<Item> isAllowed) {
			return event -> {
				if (event.getChannel().getIdLong() != game.getChannel().getIdLong()
						|| event.getAuthor().getIdLong() != game.getAuthor().getIdLong())
					return false;
				// Rejects all events that are not from the same user and channel

				String message = event.getMessage().getContentDisplay().toLowerCase();
				if (message.startsWith(InventoryAction.USE.getKeyword()) || message.startsWith("i")) {
					// If the message invokes an action
					String[] actionString = message.split(" ");

					int i;
					try {
						i = Parser.parseInt(actionString[1]) - 1;
					} catch (NumberOverflowException | NumberFormatException e) {
						// The given index is not a number
						Assets.INVENTORY_INVALID_CHOICE_MESSAGE.display(game.getChannel());
						return false;
					}

					if (i + 1 > game.getPlayer().getInventory().getItems().size() || i < 0) {
						// The user has requested an incorrect index
						Assets.INVENTORY_INDEX_NOT_FOUND.generate(i + 1).display(game.getChannel());
						return false;
					}

					ItemStack item = game.getPlayer().getInventory().getItems().get(i);
					if (item.getQuantity() <= 0) {
						// Should not occur. Indicates that a phantom stack (stack with quantity of 0)
						// resides within the player's inventory. This further indicates a programming error
						// in a part of the code (likely the Inventory class / anything that accesses the
						// inventory directly). This will remove the phantom stack and send an error dialog.
						game.getPlayer().getInventory().removeItemStack(item);
						Assets.INVENTORY_DONT_HAVE_MESSAGE.display(game.getChannel());
						return false;
					}

					if (message.startsWith("i")) {
						// The user has requested just the item info, display it and don't cancel the Waiter
						Assets.ITEM_INFO_PREPARED.generate(item).display(game.getChannel());
						return false;
					}

					if (!isAllowed.test(item.getItem())) {
						item.getItem().getForbiddenUsageDialog().display(event.getChannel());
						return false;
					}

					return true;

				} else if (message.equals(InventoryAction.EXIT.getKeyword())) {
					// If the message requests exit
					return true;
				}

				// If the user has picked a wrong keyword
				Assets.INVENTORY_INVALID_CHOICE_MESSAGE.display(game.getChannel());
				return false;
			};
		}

	}

}
