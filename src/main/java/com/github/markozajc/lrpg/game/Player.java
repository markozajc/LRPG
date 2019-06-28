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
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;

public class Player {

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

	public static class PlayerFighter extends FightingCharacter {

		private final Player player;
		private final DungeonInfo dungeon;

		public PlayerFighter(Player player, DungeonInfo dungeon) {
			this.player = player;
			this.dungeon = dungeon;
		}

		public static void guard(FightInfo fight, Consumer<Float> callback) {
			if (fight.getPlayerGuard() >= Combat.MAX_GUARD) {
				Assets.MAX_GUARD_MESSAGE.display(fight.getChannel());
				Utilities.sleep(1000);
				callback.accept(0f);

			} else {
				fight.getFeed().append("+ " + fight.getAuthor().getName() + " raises their guard.\n");
				fight.setPlayerGuard(fight.getPlayerGuard() + 2);
				callback.accept(1f);
			}
		}

		private static void inventory(FightInfo fight, Consumer<Float> callback) {
			Items.openInventory(fight, item -> {
				// Use
				if (item instanceof BattleItem) {
					// If the item is a BattleItem (has text)
					Items.useBattleItem((BattleItem) item, fight, feed -> {
						fight.getFeed().append("+ " + feed + "\n");
						callback.accept(((UsableItem) item).getSpeed());
					});

				} else {
					// If the item is not a BattleItem (doesn't have text)
					Items.useUsableItem((UsableItem) item, fight, used -> {
						if (used) {
							fight.getFeed()
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
			return this.dungeon.getAuthor().getName();
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
			this.player.setHp(hp);
		}

		@Override
		public int getHp() {
			return this.player.getHp();
		}

		@Override
		public void turn(FightInfo fight, Consumer<Float> callback) {
			new ChoiceDialog(fight.getContext(), Assets.FIGHT_STATUS_PREPARED.generate(fight), choice -> {

				if (choice == 0) {
					callback.accept(
						Combat.hitTurn(this, fight.getEnemy(), 0, fight, Math.round(this.player.getLevel() * .1f)));

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
	}

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
	private long reputationMark;
	private int health = -1;
	@Nonnull
	private RegionDatabase lastRegionBossDefeated = RegionDatabase.SEWERS;
	@Nonnull
	private PlayerStatistics statistics = new PlayerStatistics();
	private Enemy fightCurrentEnemy;
	private int fightGuard;
	private int lastEncounter;
	private int step;
	private int levelMark;

	public Player(long gold, long xp, @Nonnull ArmorItem armor, @Nonnull WeaponItem weapon) {
		this.gold = gold;
		this.xp = xp;
		this.armor = armor;
		this.weapon = weapon;
		this.reputationMark = xp;
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

	public int getHp() {
		return this.health;
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

	public long getReputation() {
		return this.getXp() - this.reputationMark;
	}

	@Nonnull
	public WeaponItem getWeapon() {
		return this.weapon;
	}

	public long getXp() {
		return this.xp;
	}

	public void resetReputation() {
		this.reputationMark = this.getXp();
	}

	public void setArmor(@Nonnull ArmorItem armor) {
		this.armor = armor;
	}

	public void setGold(long gold) {
		this.gold = gold;
	}

	public void setHp(int health) {
		this.health = Utilities.capAt(health, getMaxHp());
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

	public boolean isInDungeon() {
		return getHp() != -1;
	}

	public void setNotInDungeon() {
		this.health = -1;
		this.setLastRegionBoss(RegionDatabase.SEWERS);
		this.resetReputation();
		this.setFightGuard(0);
		this.setFightCurrentEnemy(null);
		this.resetDungeonStep();
	}

	public void setReputation(long reputation) {
		this.reputationMark = this.xp - reputation;
	}

	@Nonnull
	public RegionDatabase getLastRegionBoss() {
		return this.lastRegionBossDefeated;
	}

	public void setLastRegionBoss(@Nonnull RegionDatabase region) {
		this.lastRegionBossDefeated = region;
	}

	@Nonnegative
	public int getDungeonStep() {
		return this.step;
	}

	@Nonnegative
	public int addDungeonStep() {
		return this.step++;
	}

	@Nonnegative
	public void resetDungeonStep() {
		this.step = 0;
	}

	@Nonnegative
	public int getLastEncounter() {
		return this.lastEncounter;
	}

	@Nonnegative
	public void resetLastEncounter() {
		this.lastEncounter = getDungeonStep();
	}

	@Nonnull
	public PlayerStatistics getStatistics() {
		return this.statistics;
	}

	public Enemy getFightCurrentEnemy() {
		return this.fightCurrentEnemy;
	}

	public int getFightGuard() {
		return this.fightGuard;
	}

	public void setFightCurrentEnemy(Enemy fightCurrentEnemy) {
		this.fightCurrentEnemy = fightCurrentEnemy;
	}

	public void setFightGuard(int fightGuard) {
		this.fightGuard = fightGuard;
	}

	public RegionDatabase getRegion() {
		return Utilities.GET_REGION.apply(this.getReputation());
	}

	public int getLevelMark() {
		return this.levelMark;
	}

	public void setLevelMark() {
		this.levelMark = getLevel();
	}

}