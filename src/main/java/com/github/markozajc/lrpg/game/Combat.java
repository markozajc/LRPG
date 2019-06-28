package com.github.markozajc.lrpg.game;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lrpg.game.Enemies.Enemy;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.LRpgExposed.AttackDefenseCharacter;
import com.github.markozajc.lrpg.game.LRpgExposed.NamedObject;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithSpeed;
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

	public static float hitTurn(FightingCharacter self, FightingCharacter foe, int foeGuard, FightInfo fight, int selfAttackOffset) {
		boolean critical = Utilities.getChance(Combat.CRIT_CHANCE);
		// Calculates whether the hit is a critical

		int attack = Utilities.calculateAttack(self.getAttack().calculateValue(selfAttackOffset, selfAttackOffset),
			foe.getDefense().calculateValue() + foeGuard * 2, critical);
		// Calculates the attack

		foe.decreaseHp(attack);
		// Decreases foe's HP

		fight.getFeed()
				.append((self.equals(fight.getPlayerFighter()) ? "+ " : "- ") + self.getName() + " attacks "
						+ foe.getName() + ". ");

		if (attack > 0) {
			fight.getFeed()
					.append(foe.getName() + " loses " + attack + " HP" + (critical ? " [CRITICAL]" : "") + ".\n");

		} else {
			fight.getFeed().append(foe.getName() + " dodges " + self.getName() + "'s attack.\n");
		}

		return self.getSpeed();
	}

	public static float hitTurn(FightingCharacter self, FightingCharacter foe, int foeGuard, FightInfo fight) {
		return hitTurn(self, foe, foeGuard, fight, 0);
	}

	public static void fightEnemy(@Nonnull FightInfo fight, BiConsumer<Boolean, StringBuilder> callback) {
		fight.getPlayerFighter().addTime(1f);
		turnPlayer(fight, fc -> callback.accept(fc.equals(fight.getPlayerFighter()), fight.getFeed()));
	}

	public static void turnPlayer(@Nonnull FightInfo fight, @Nonnull Consumer<FightingCharacter> endCallback) {
		turnCharacter(fight, fight.getPlayerFighter(), fight.getEnemy(), v -> turnEnemy(fight, endCallback),
			endCallback);
	}

	public static void turnEnemy(@Nonnull FightInfo fight, @Nonnull Consumer<FightingCharacter> endCallback) {
		turnCharacter(fight, fight.getEnemy(), fight.getPlayerFighter(), v -> turnPlayer(fight, endCallback),
			endCallback);
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

		public void decreaseHp(int value) {
			this.setHp(getHp() - value);
		}

		public abstract void setHp(int hp);

		public abstract int getHp();

		public abstract void turn(FightInfo fight, Consumer<Float> callback);

		public float getTime() {
			return this.time;
		}

		public void addTime(float time) {
			this.time += time;
		}

		public void takeTime(float time) {
			this.time -= time;
		}

		public void setTime(float time) {
			this.time = time;
		}

	}

}
