package com.github.markozajc.lrpg.game;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lrpg.game.Enemies.Enemy;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.LRpgExposed.AttackDefenseCharacter;
import com.github.markozajc.lrpg.game.LRpgExposed.EmotableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.NamedObject;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithSpeed;
import com.github.markozajc.lrpg.game.LRpgExposed.TurnActionObject;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

class Combat {

	public static class SurrenderException extends RuntimeException {}

	private Combat() {}

	/**
	 * The chance of a critical hit
	 */
	public static final float CRIT_CHANCE = 0.1f;

	public static final int MAX_GUARD = 5;

	public static float hitTurn(FightingCharacter self, FightingCharacter foe, int foeGuard, FightInfo fight) {
		return hitTurn(self, foe, foeGuard, fight, 0);
	}

	public static float hitTurn(FightingCharacter self, FightingCharacter foe, int foeGuard, FightInfo fight, int selfAttackOffset) {
		boolean critical = Utilities.getChance(Combat.CRIT_CHANCE);
		// Calculates whether the hit is a critical

		int attack = Utilities.calculateAttack(self.getAttack().calculateValue(selfAttackOffset, selfAttackOffset),
			foe.getDefense().calculateValue() + foeGuard * 2, critical);
		// Calculates the attack

		foe.decreaseHp(attack);
		// Decreases foe's HP

		fight.getPlayerFight()
				.getFeed()
				.append((self.equals(fight.getPlayerFighter()) ? "+ " : "- ") + self.getName() + " attacks "
						+ foe.getName() + ". ");

		if (attack > 0) {
			fight.getPlayerFight()
					.getFeed()
					.append(foe.getName() + " loses " + attack + " HP" + (critical ? " [CRITICAL]" : "") + ".\n");

		} else {
			fight.getPlayerFight().getFeed().append(foe.getName() + " dodges " + self.getName() + "'s attack.\n");
		}

		return self.getSpeed();
	}

	public static void fightEnemy(@Nonnull FightInfo fight, BiConsumer<Boolean, StringBuilder> callback) {
		if (!fight.isResumed())
			fight.getPlayerFighter().addTime(1f);
		// Adds starting time to the player if the fight is fresh (not resumed)

		Consumer<FightingCharacter> callbackInvoker = fc -> callback.accept(fc.equals(fight.getPlayerFighter()),
			fight.getPlayerFight().getFeed());
		if (!fight.getPlayerFight().getEnemy().getInfo().isBoss() && Utilities.getChance(.5f)) {
			// If enemy struck first (only non-bosses) - 50%
			// TODO V2 stealth stat
			turnEnemy(fight, callbackInvoker);
		} else {
			// If player struck first
			turnPlayer(fight, callbackInvoker);
		}
	}

	private static void turnPlayer(@Nonnull FightInfo fight, @Nonnull Consumer<FightingCharacter> endCallback) {
		turnCharacter(fight, fight.getPlayerFighter(), fight.getPlayerFight().getEnemy(),
			v -> turnEnemy(fight, endCallback), endCallback);
	}

	private static void turnEnemy(@Nonnull FightInfo fight, @Nonnull Consumer<FightingCharacter> endCallback) {
		turnCharacter(fight, fight.getPlayerFight().getEnemy(), fight.getPlayerFighter(),
			v -> turnPlayer(fight, endCallback), endCallback);
	}

	public static void turnCharacter(@Nonnull FightInfo fight, @Nonnull FightingCharacter self, @Nonnull FightingCharacter foe, @Nonnull Consumer<Void> callback, @Nonnull Consumer<FightingCharacter> endCallback) {
		self.turn(fight, speed -> {
			self.takeTime(speed);
			// Take away own time

			self.addTime((float) Math.abs(Math.ceil(foe.getTime())));
			foe.setTime(0f);
			// Adds foe's "borrowed" time to self

			if (speed == -1f) {
				endCallback.accept(foe);
				return;
			}
			// Surrender

			if (foe.getHp() <= 0) {
				endCallback.accept(self);
				return;
			}
			// Victory

			if (self.getTime() > 0) {
				turnCharacter(fight, self, foe, callback, endCallback);
				return;
			}
			// Have the turn again if they have remaining time

			foe.setTime(1f);
			callback.accept(null);
			// Give the foe a turn
		});
	}

	public static String trimFeed(StringBuilder feed, int lines) {
		if (feed.toString().split("\n").length > lines) {
			int index = feed.lastIndexOf("\n");
			for (int i = 0; i < lines; i++) {
				int indexChk = feed.lastIndexOf("\n", index - 1);
				if (indexChk == -1)
					break;

				index = indexChk;
			}

			char[] chars = new char[feed.length() - 1 - index];
			feed.getChars(index + 1, feed.length(), chars, 0);

			return new String(chars);

		}

		return feed.toString();
	}

	public static MessageEmbed getVictoryStatus(Enemy enemy, StringBuilder feed, Item drop) {
		EmbedBuilder builder = new EmbedBuilder();

		String feedString = trimFeed(feed, 4);

		builder.setColor(Constants.GREEN);
		builder.setThumbnail(enemy.getInfo().getImageUrl());
		builder.setTitle(
			"You have defeated " + (enemy.getInfo().isBoss() ? "" : "the ") + enemy.getInfo().getName() + "!");
		if (feedString.length() > 0)
			builder.setDescription("```diff\n" + feedString + "```");
		builder.addField("Reward",
			"You earned **" + enemy.getInfo().getGoldDrop() + " gold" + Assets.GOLD_EMOTE + "** and **"
					+ enemy.getInfo().getXpDrop() + " XP**."
					+ (drop != null ? " The enemy has also dropped a **" + drop.getNameWithEmote() + "**." : ""),
			false);

		return builder.build();
	}

	public abstract static class FightingCharacter implements NamedObject, AttackDefenseCharacter, ObjectWithSpeed {

		private float time = 0f;

		public final void decreaseHp(int value) {
			this.setHp(getHp() - value);
		}

		public abstract void setHp(int hp);

		public abstract int getHp();

		public abstract int getMaxHp();

		public abstract void turn(FightInfo fight, Consumer<Float> callback);

		public float getTime() {
			return this.time;
		}

		public void setTime(float time) {
			this.time = time;
		}

		public final void addTime(float time) {
			setTime(getTime() + time);
		}

		public final void takeTime(float time) {
			setTime(getTime() - time);
		}
	}

	public static interface Effect extends NamedObject, EmotableObject, TurnActionObject {}

	public static interface HealthLossEffect extends Effect {

		public float getHpLossPercentage();

		@Override
		public default void onTurn(TurnInfo turn) {
			int hploss = Math.round(turn.getSelf().getMaxHp() * getHpLossPercentage());
			turn.getSelf().decreaseHp(hploss);
			turn.getFight()
					.getPlayerFight()
					.getFeed()
					.append(turn.getSelf().getName() + " lost " + hploss + " HP due to " + getName() + "! ");
		}

	}

	public enum HealthLossEffectDatabase implements HealthLossEffect {
		POISON("Poison", "", .02f),
		CAUSTIC("Caustic ooze", "", .1f),
		BLEED("Bleeding", "", .05f),
		FIRE("Fire", "", .15f);

		@Nonnull
		private final String name;
		@Nonnull
		private final String emote;
		private final float healthPercentage;

		private HealthLossEffectDatabase(@Nonnull String name, @Nonnull String emote, float healthPercentage) {
			this.name = name;
			this.emote = emote;
			this.healthPercentage = healthPercentage;
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
		public float getHpLossPercentage() {
			return this.healthPercentage;
		}

	}

}
