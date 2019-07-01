package com.github.markozajc.lrpg.game;

import java.util.function.Consumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.github.markozajc.lithium.utilities.dialogs.waiter.BooleanDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lrpg.game.Combat.FightingCharacter;
import com.github.markozajc.lrpg.game.Enemies.Enemy;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.BattleItem;
import com.github.markozajc.lrpg.game.Items.Inventory;
import com.github.markozajc.lrpg.game.Items.UsableItem;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValueObject;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;

public class Player {

	public static long calculateXp(long level) {
		return level * level * 3;
	}

	private long gold;
	private long xp;
	@Nonnull
	private ArmorItem armor;
	@Nonnull
	private WeaponItem weapon;
	@Nonnull
	private Inventory inventory = new Inventory();
	private PlayerDungeon playerDungeon;

	public Player(long gold, long xp, @Nonnull ArmorItem armor, @Nonnull WeaponItem weapon) {
		this.gold = gold;
		this.xp = xp;
		this.armor = armor;
		this.weapon = weapon;
	}

	@Nonnull
	public ArmorItem getArmor() {
		return this.armor;
	}

	public long getXpRequired() {
		return getNextLevelXp() - getXp();
	}

	public long getGold() {
		return this.gold;
	}

	@Nonnull
	public Inventory getInventory() {
		return this.inventory;
	}

	public int getLevel() {
		return (int) Math.sqrt(this.xp / 3f);
	}

	public long getNextLevelXp() {
		return calculateXp(getLevel() + 1L);
	}

	@Nonnull
	public WeaponItem getWeapon() {
		return this.weapon;
	}

	public long getXp() {
		return this.xp;
	}

	public void setArmor(@Nonnull ArmorItem armor) {
		this.armor = armor;
	}

	public void setGold(long gold) {
		this.gold = gold;
	}

	public int getMaxHp() {
		return Utilities.CALCULATE_MAX_HEALTH.applyAsInt(this.getLevel());
	}

	public void setWeapon(@Nonnull WeaponItem weapon) {
		this.weapon = weapon;
	}

	public void setXp(long xp) {
		this.xp = xp;
	}

	public PlayerDungeon getPlayerDungeon() {
		return this.playerDungeon;
	}

	public void createPlayerDungeon() {
		if (this.playerDungeon == null)
			this.playerDungeon = new PlayerDungeon(this.getXp(), this.getMaxHp());
	}

	public void removePlayerDungeon() {
		this.playerDungeon = null;
	}

	public static class PlayerDungeon {

		@Nonnegative
		private int lastEncounter;
		@Nonnegative
		private int step;
		@Nonnegative
		private int levelMark;
		@Nonnull
		private RegionDatabase lastRegionBossDefeated = RegionDatabase.SEWERS;
		private int hp;
		private PlayerFight playerFight;
		@Nonnull
		private final PlayerStatistics statistics = new PlayerStatistics();
		@Nonnegative
		private final long reputationMark;

		public PlayerDungeon(long xp, int maxHp) {
			this.reputationMark = xp;
			this.hp = maxHp;
		}

		@Nonnull
		public RegionDatabase getLastRegionBoss() {
			return this.lastRegionBossDefeated;
		}

		public void setLastRegionBoss(@Nonnull RegionDatabase region) {
			this.lastRegionBossDefeated = region;
		}

		@Nonnegative
		public int getStep() {
			return this.step;
		}

		@Nonnegative
		public int addStep() {
			return this.step++;
		}

		@Nonnegative
		public int getLastEncounter() {
			return this.lastEncounter;
		}

		@Nonnegative
		public void resetLastEncounter() {
			this.lastEncounter = getStep();
		}

		@Nonnull
		public PlayerStatistics getStatistics() {
			return this.statistics;
		}

		public int getLevelMark() {
			return this.levelMark;
		}

		public void setLevelMark(int level) {
			this.levelMark = level;
		}

		public void setHp(int health, int maxHp) {
			this.hp = Utilities.capAt(health, maxHp);
		}

		public long getReputation(long xp) {
			return xp - this.reputationMark;
		}

		public int getHp() {
			return this.hp;
		}

		public RegionDatabase getRegion(long xp) {
			return Utilities.GET_REGION.apply(getReputation(xp));
		}

		public PlayerFight getPlayerFight() {
			return this.playerFight;
		}

		public void createPlayerFight(@Nonnull Enemy enemy) {
			if (this.playerFight == null)
				this.playerFight = new PlayerFight(enemy);
		}

		public void removePlayerFight() {
			this.playerFight = null;
		}

		public static class PlayerFight {

			private int guard = 0;
			@Nonnull
			private final Enemy enemy;
			@Nonnull
			private final StringBuilder feed;
			private float playerTime;

			public PlayerFight(@Nonnull Enemy enemy) {
				this.enemy = enemy;
				this.feed = new StringBuilder();
			}

			public int getGuard() {
				return this.guard;
			}

			@Nonnull
			public Enemy getEnemy() {
				return this.enemy;
			}

			public void setGuard(int guard) {
				this.guard = guard;
			}

			@Nonnull
			public StringBuilder getFeed() {
				return this.feed;
			}

			public float getPlayerTime() {
				return this.playerTime;
			}

			public void setPlayerTime(float playerTime) {
				this.playerTime = playerTime;
			}

		}

		public static class PlayerStatistics {

			private int enemiesSlain = 0;
			private int healiesConsumed = 0;
			private int chestsOpened = 0;
			private int mysteriousBooksRead = 0;
			private int itemsPurchased = 0;

			public void enemySlain() {
				this.enemiesSlain++;
			}

			public void healieConsumed() {
				this.healiesConsumed++;
			}

			public void chestOpened() {
				this.chestsOpened++;
			}

			public void mysteriousBookRead() {
				this.mysteriousBooksRead++;
			}

			public void itemPurchased() {
				this.itemsPurchased++;
			}

			public int getEnemiesSlain() {
				return this.enemiesSlain;
			}

			public int getHealiesConsumed() {
				return this.healiesConsumed;
			}

			public int getChestsOpened() {
				return this.chestsOpened;
			}

			public int getMysteriousBooksRead() {
				return this.mysteriousBooksRead;
			}

			public int getItemsPurchased() {
				return this.itemsPurchased;
			}

		}

	}

	public static class PlayerFighter extends FightingCharacter {

		private final Player player;
		private final FightInfo fight;

		public PlayerFighter(Player player, FightInfo fight) {
			this.player = player;
			this.fight = fight;
		}

		public static void guard(FightInfo fight, Consumer<Float> callback) {
			if (fight.getPlayerFight().getGuard() >= Combat.MAX_GUARD) {
				Assets.MAX_GUARD_MESSAGE.display(fight.getChannel());
				Utilities.sleep(1000);
				callback.accept(0f);

			} else {
				fight.getPlayerFight().getFeed().append("+ " + fight.getAuthor().getName() + " raises their guard.\n");
				fight.getPlayerFight().setGuard(fight.getPlayerFight().getGuard() + 2);
				callback.accept(1f);
			}
		}

		private static void inventory(FightInfo fight, Consumer<Float> callback) {
			Items.openInventory(fight, item -> {
				// Use
				if (item instanceof BattleItem) {
					// If the item is a BattleItem (has text)
					Items.useBattleItem((BattleItem) item, fight, feed -> {
						fight.getPlayerFight().getFeed().append("+ " + feed + "\n");
						callback.accept(((UsableItem) item).getSpeed());
					});

				} else {
					// If the item is not a BattleItem (doesn't have text)
					Items.useUsableItem((UsableItem) item, fight, used -> {
						if (used) {
							fight.getPlayerFight()
									.getFeed()
									.append("+ " + fight.getAuthor().getName() + " uses the " + item.getName() + ".\n");
							callback.accept(((UsableItem) item).getSpeed());
						} else {
							callback.accept(0f);
						}
					});
				}
			}, () -> callback.accept(0f));
		}

		@SuppressWarnings("null")
		@Override
		public String getName() {
			return this.fight.getAuthor().getName();
		}

		@Override
		public RangedValueObject getAttack() {
			return this.player.getWeapon();
		}

		@Override
		public RangedValueObject getDefense() {
			return this.player.getArmor();
		}

		@Override
		public void setHp(int hp) {
			this.player.getPlayerDungeon().setHp(hp, this.player.getMaxHp());
		}

		@Override
		public int getHp() {
			return this.player.getPlayerDungeon().getHp();
		}

		@Override
		public void turn(FightInfo fight, Consumer<Float> callback) {
			new ChoiceDialog(fight.getContext(), Assets.FIGHT_STATUS_PREPARED.generate(fight), choice -> {

				if (choice == 0) {
					callback.accept(Combat.hitTurn(this, fight.getPlayerFight().getEnemy(), 0, fight,
						Math.round(this.player.getLevel() * .1f)));

				} else if (choice == 1) {
					guard(fight, callback);

				} else if (choice == 2) {
					inventory(fight, callback);

				} else if (choice == 3) {
					new BooleanDialog(fight.getContext(), Assets.SURRENDER_MESSAGE, surrender -> {
						if (surrender) {
							callback.accept(-1f);
						} else {
							turn(fight, callback);
						}

					}).display(fight.getChannel());

				} else {
					Utilities.confirmExit(fight.getContext(), () -> turn(fight, callback));
				}

			}, "h", "g", "i", "s", "exit").display(fight.getChannel());
		}

		@Override
		public float getSpeed() {
			return this.player.getWeapon().getType().getSpeed();
		}

		@Override
		public float getTime() {
			return this.fight.getPlayerFight().getPlayerTime();
		}

		@Override
		public void setTime(float time) {
			this.fight.getPlayerFight().setPlayerTime(time);
		}

	}
}