package com.github.markozajc.lrpg.game;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lrpg.game.Combat.FightingCharacter;
import com.github.markozajc.lrpg.game.Enemies.EnemyInformation;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.HealingItemDatabase;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.Items.UsableItemDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;
import com.google.gson.GsonBuilder;

public class LRpgExposed {

	static final Logger LOG = LoggerFactory.getLogger("LRPG");

	private LRpgExposed() {}

	public static class RangedValue implements RangedValueObject {

		private final int min;
		private final int max;

		public RangedValue(int min, int max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public int getMin() {
			return this.min;
		}

		@Override
		public int getMax() {
			return this.max;
		}

	}

	public static interface EmotableObject {

		@Nonnull
		public String getEmote();

	}

	public static interface PicturableObject {

		public String getImageUrl();

	}

	public static interface ObjectWithRarity {

		@Nonnegative
		public float getRarity();

	}

	public static interface NamedObject {

		@Nonnull
		public String getName();

	}

	public static interface IdentifiableObject {

		@Nonnull
		public String getIdentification();

		@Nonnull
		public String getDatabaseToken();

	}

	public static interface RangedValueObject {

		public int getMin();

		public int getMax();

		public default int calculateValue() {
			return this.calculateValue(0, 0);
		}

		public default int calculateValue(int minOffset, int maxOffset) {
			return Utilities.intRange(getMin() + minOffset, getMax() + maxOffset);
		}

	}

	public static interface ObjectWithReputation {

		public long getReputation();

	}

	public static interface ObjectWithSpeed {

		public float getSpeed();

	}

	public static interface TurnActionObject {

		public static class TurnInfo {

			@Nonnull
			private final FightInfo fight;
			@Nonnull
			private final FightingCharacter self;
			@Nonnull
			private final FightingCharacter foe;

			public TurnInfo(@Nonnull FightInfo fight, @Nonnull FightingCharacter self,
					@Nonnull FightingCharacter foe) {
				this.fight = fight;
				this.self = self;
				this.foe = foe;
			}

			@Nonnull
			public FightInfo getFight() {
				return this.fight;
			}

			@Nonnull
			public FightingCharacter getSelf() {
				return this.self;
			}

			@Nonnull
			public FightingCharacter getFoe() {
				return this.foe;
			}

		}

		public void onTurn(TurnInfo turn);

	}

	public static interface AttackDefenseCharacter {

		@Nonnull
		public RangedValueObject getAttack();

		@Nonnull
		public RangedValueObject getDefense();

	}

	@Nonnull
	public static Player getStarterPlayer() {
		Player player = new Player(25, 1L, new ArmorItem(ArmorDatabase.NAKED, 0),
				new WeaponItem(WeaponDatabase.FISTS, 0));

		player.getInventory().addItem(HealingItemDatabase.FOOD_RATION, 3);
		player.getInventory().addItem(HealingItemDatabase.POTION_HEALING, 1);
		player.getInventory().addItem(UsableItemDatabase.SCROLL_UPGRADE, 1);
		player.getInventory().addItem(new ArmorItem(ArmorDatabase.SHIRT, 0), 1);
		player.getInventory().addItem(new WeaponItem(WeaponDatabase.SHORTSWORD, 0), 1);
		player.getInventory().addItem(new WeaponItem(WeaponDatabase.AXE, 0), 1);
		player.getInventory().addItem(new WeaponItem(WeaponDatabase.DAGGER, 1), 2);

		return player;
	}

	public static void startGame(@Nonnull CommandContext context) {
		GameInfo game = new GameInfo(context);

		if (game.isFirstTimeLaunch()) {
			new ChoiceDialog(context, Assets.FIRST_LAUNCH_PREPARED.generate(context),
					action -> Castle.displayCastle(game), "c").display(context.getChannel());
		} else {
			Castle.displayCastle(game);
		}
	}

	public static GsonBuilder registerGsonTypeAdapters(GsonBuilder gsonBuilder) {
		return gsonBuilder
				.registerTypeHierarchyAdapter(Item.class, Utilities.getTypeAdapterFactory(Utilities.UNPACK_ITEM))
				.registerTypeHierarchyAdapter(EnemyInformation.class,
					Utilities.getTypeAdapterFactory(Utilities.UNPACK_ENEMY));
	}

}
