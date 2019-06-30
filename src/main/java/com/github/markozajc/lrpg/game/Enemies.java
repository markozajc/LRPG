package com.github.markozajc.lrpg.game;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.markozajc.lrpg.game.Combat.FightingCharacter;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.Items.ItemRarityPack;
import com.github.markozajc.lrpg.game.LRpgExposed.AttackDefenseCharacter;
import com.github.markozajc.lrpg.game.LRpgExposed.IdentifiableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.NamedObject;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithReputation;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithSpeed;
import com.github.markozajc.lrpg.game.LRpgExposed.PicturableObject;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValue;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValueObject;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
public class Enemies {

	private static final String IMGROOT = "https://raw.githubusercontent.com/FallenNationDev/spd-enemies-animated/master";

	private Enemies() {}

	public static interface EnemyInformation extends NamedObject, PicturableObject, ObjectWithReputation,
			ObjectWithSpeed, AttackDefenseCharacter, IdentifiableObject {

		public int getGoldDrop();

		public int getMaxHp();

		@Nullable
		public ItemRarityPack getItemDrops();

		public int getXpDrop();

		public boolean isBoss();

	}

	public static class Enemy extends FightingCharacter {

		protected EnemyInformation info;
		protected int hp;

		public Enemy(EnemyInformation info) {
			this.hp = info.getMaxHp();
			this.info = info;
		}

		public EnemyInformation getInfo() {
			return this.info;
		}

		@Override
		public void turn(FightInfo fight, Consumer<Float> callback) {
			callback.accept(Combat.hitTurn(this, fight.getPlayerFighter(), fight.getPlayerFight().getGuard(), fight));
		}

		@Override
		public String getName() {
			return this.info.getName();
		}

		@Override
		public int getHp() {
			return this.hp;
		}

		@Override
		public void setHp(int hp) {
			this.hp = hp;
		}

		@Override
		public RangedValueObject getDefense() {
			return this.info.getDefense();
		}

		@Override
		public RangedValueObject getAttack() {
			return this.info.getAttack();
		}

		@Override
		public float getSpeed() {
			return this.info.getSpeed();
		}

	}

	public enum BossInformationDatabase implements EnemyInformation {
		// @formatter:off
		GOO(   "The Goo",             -1, -1, 1f,  5,  10, 0, 30,   100, 50,   null,                 IMGROOT + "/goo.gif"),
		TENGU( "Tengu",               5,  12, .5f, 5,  10, 0, 120,  120, 100,  null,                 IMGROOT + "/tengu.gif"),
		DM300( "DM-300",              18, 23, 2f,  15, 20, 0, 230,  180, 250,  null,                 IMGROOT + "/dm300.gif"),
		KING(  "The King of Dwarves", 15, 20, 1f,  10, 10, 0, 700,  200, 500,  null,                 IMGROOT + "/king.gif"),
		YOG(   "Yog-Dzewa",           35, 40, 1f,  25, 35, 0, 1000, 500, 1000, Assets.YOG_DROP_PACK, IMGROOT + "/yog.gif");
		// @formatter:on

		@Nonnull
		private final String name;
		@Nonnull
		private final RangedValueObject attack;
		@Nonnull
		private final RangedValueObject defense;
		@Nonnegative
		private final float attackSpeed;
		@Nonnegative
		private final long reputation;
		@Nonnegative
		private final int maxHp;
		@Nonnegative
		private final int xp;
		@Nonnegative
		private final int goldDrop;
		@Nullable
		private final Supplier<ItemRarityPack> drops;
		@Nonnull
		private final String imageUrl;

		private BossInformationDatabase(@Nonnull String name, @Nonnegative int minAttack, @Nonnegative int maxAttack, // NOSONAR
				@Nonnegative float attackSpeed, @Nonnegative int minDefense, @Nonnegative int maxDefense,
				@Nonnegative long reputation, @Nonnegative int xp, @Nonnegative int maxHp, @Nonnegative int goldDrop,
				@Nullable Supplier<ItemRarityPack> drops, @Nonnull String imageUrl) {
			this.name = name;
			this.attack = new RangedValue(minAttack, maxAttack);
			this.defense = new RangedValue(minDefense, maxDefense);
			this.attackSpeed = attackSpeed;
			this.reputation = reputation;
			this.maxHp = maxHp;
			this.xp = xp;
			this.goldDrop = goldDrop;
			this.drops = drops;
			this.imageUrl = imageUrl;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getImageUrl() {
			return this.imageUrl;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Override
		public RangedValueObject getAttack() {
			return this.attack;
		}

		@Override
		public int getGoldDrop() {
			return this.goldDrop;
		}

		@Override
		public int getMaxHp() {
			return this.maxHp;
		}

		@Override
		public ItemRarityPack getItemDrops() {
			Supplier<ItemRarityPack> itemDrops = this.drops;
			if (itemDrops != null)
				return itemDrops.get();

			return null;
		}

		@Override
		public int getXpDrop() {
			return this.xp;
		}

		@Override
		public float getSpeed() {
			return this.attackSpeed;
		}

		@Override
		public boolean isBoss() {
			return true;
		}

		@Override
		public RangedValueObject getDefense() {
			return this.defense;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public String getDatabaseToken() {
			return "B";
		}

	}

	public enum BossDatabase {
		GOO(() -> new Enemy(BossInformationDatabase.GOO) {

			private int pump = 0;
			private FightInfo fight = null;

			@SuppressWarnings("null")
			@Override
			public RangedValueObject getAttack() {
				if (this.pump == Assets.GOO_PUMP_REQUIRED + 1 && this.fight != null) {
					this.pump = 0;

					if (this.fight.getPlayerFight().getGuard() >= Assets.GOO_PUMP_REQUIRED - 1)
						return Assets.GOO_ATTACK_PUMP_FAIL;

					return Assets.GOO_ATTACK_PUMP;
				}

				return Assets.GOO_ATTACK_NORMAL;
			}

			@Override
			public void turn(FightInfo fight, Consumer<Float> callback) {

				if (this.fight == null)
					this.fight = fight;

				this.pump++;

				if (this.pump == Assets.GOO_PUMP_REQUIRED) {
					fight.getPlayerFight().getFeed().append(Assets.GOO_PUMP_TEXT);
					this.pump++;
					callback.accept(3f);
				} else {
					super.turn(fight, callback);
				}

			}

		}),
		TENGU(BossInformationDatabase.TENGU),
		DM300(BossInformationDatabase.DM300),
		KING(BossInformationDatabase.KING),
		YOG(BossInformationDatabase.YOG);

		@Nonnull
		private final Supplier<Enemy> enemySupplier;

		private BossDatabase(@Nonnull EnemyInformation info) {
			this.enemySupplier = () -> new Enemy(info);
		}

		private BossDatabase(@Nonnull Supplier<Enemy> enemySupplier) {
			this.enemySupplier = enemySupplier;
		}

		@SuppressWarnings("null")
		@Nonnull
		public Enemy getEnemy() {
			return this.enemySupplier.get();
		}

	}

	public enum RegionDatabase implements PicturableObject, NamedObject, ObjectWithReputation {
		// @formatter:off
		SEWERS(    "Sewers",       0,     BossDatabase.GOO,   "https://i.postimg.cc/VkMFjJdM/Sewers.png"),
		PRISON(    "Prison",       100,   BossDatabase.TENGU, "https://i.postimg.cc/FH6nq2L6/Dungeon.png"),
		CAVES( 	   "Caves",        1100,  BossDatabase.DM300, "https://i.postimg.cc/v8qXMRJ1/Caves.png"),
		LOST_CITY( "Lost city",    6100,  BossDatabase.KING,  "https://i.postimg.cc/gkxs2Spg/LostCity.png"),
		HELL(      "Helltropolis", 16100, BossDatabase.YOG,   "https://i.postimg.cc/HsF2cwsd/Hell.png");
		// @formatter:on

		@Nonnull
		private final String name;
		@Nonnegative
		private final int reputation;
		@Nonnull
		private final BossDatabase boss;
		@Nonnull
		private final String imageUrl;

		private RegionDatabase(@Nonnull String name, @Nonnegative int reputation, @Nonnull BossDatabase boss,
				@Nonnull String imageUrl) {
			this.name = name;
			this.reputation = reputation;
			this.boss = boss;
			this.imageUrl = imageUrl;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Nonnull
		public BossDatabase getBoss() {
			return this.boss;
		}

		@Override
		public String getImageUrl() {
			return this.imageUrl;
		}

	}

	public enum EnemyDatabase implements EnemyInformation {
		//@formatter:off//////////////////////////////////////////////////////////////////////
		// ENEMIES
		//////////////////////////////////////////////////////////////////////////////////////
		RAT(		   "Rat",          1, 4, 1f, RegionDatabase.SEWERS,   0,    2,   8,   2,  IMGROOT+"/rat.gif"),
		ALBINO_RAT(    "White rat",    2, 5, 1f, RegionDatabase.SEWERS,   10,   4,   10,  3,  IMGROOT+"/rat_albino.gif"),
		GNOLL(		   "Gnoll",        3, 7, 1f, RegionDatabase.SEWERS,   20,   5,   15,  6,  IMGROOT+"/gnoll.gif"),
		CRAB(		   "Crab",         5, 5, .5f,RegionDatabase.SEWERS,   35,   7,   25,  7,  IMGROOT+"/crab.gif"),
		PLAGUE_RAT(    "Plague rat",   1, 7, 1f, RegionDatabase.SEWERS,   45,   8,   20,  8,  IMGROOT+"/rat_plague.gif"),
		GNOLL_HUNTER(  "Gnoll hunter", 3, 9, 1f, RegionDatabase.SEWERS,   65,   10,  25,  12, IMGROOT+"/gnoll_hunter.gif"),
		MUTANT_FLY(    "Mutant fly",   1, 3, .5f,RegionDatabase.PRISON,   0,    10,  20,  5,  IMGROOT+"/swarm.gif"),
		SKELETON(      "Skeleton",     3, 8, 1,  RegionDatabase.PRISON,   40,   30,  30,  6,  IMGROOT+"/skeleton.gif"),
		THIEF(		   "Thief",        3, 4, .5f,RegionDatabase.PRISON,   100,  10,  40,  12, IMGROOT+"/thief.gif"),
		GNOLL_SHAMAN(  "Gnoll shaman", 6, 12,2f, RegionDatabase.PRISON,   300,  75,  40,  10, IMGROOT+"/gnoll_shaman.gif"),
		GUARD(         "Guard",        8, 10,2f, RegionDatabase.PRISON,   400,  80,  55,  20, IMGROOT+"/guard.gif"),
		BAT(   		   "Bat",          3, 4, .3f,RegionDatabase.CAVES,    0,    50,  30,  15, IMGROOT+"/bat.gif"),
		GNOLL_BRUTE(   "Gnoll brute",  4, 7, 1f, RegionDatabase.CAVES,    400,  100, 50,  30, IMGROOT+"/gnoll_brute.gif"),
		SPIDER(		   "Cave spider",  3, 5, 1f, RegionDatabase.CAVES,    1500, 150, 40,  10, IMGROOT+"/spider.gif"),
		DWARF_MONK(    "Dwarf monk",   5, 6, .5f,RegionDatabase.LOST_CITY,0,    200, 70,  20, IMGROOT+"/monk.gif"),
		DWARF_WARLOCK( "Dwarf mage",   7, 10,2f, RegionDatabase.LOST_CITY,700,  300, 60,  25, IMGROOT+"/warlock.gif"),
		FIRE_ELEMENTAL("Fireball",     6, 9, 1,  RegionDatabase.LOST_CITY,1000, 500, 80,  10, IMGROOT+"/elemental.gif"),
		GOLEM(         "Golem",        9, 10,2f, RegionDatabase.LOST_CITY,3000, 600, 110, 40, IMGROOT+"/golem.gif"),
		SUCCUBUS(      "Succubus",     6, 8, 1f, RegionDatabase.HELL,     0,    700, 80,  80, IMGROOT+"/succubus.gif"),
		EVIL_EYE(      "Malicious eye",30,40,3f, RegionDatabase.HELL,     2800, 750, 120, 60, IMGROOT+"/eye.gif"),
		SCORPIO(       "Scorpio",      8, 13,1f, RegionDatabase.HELL,     5000, 800, 90,  50, IMGROOT+"/scorpio.gif"),
		IMP(           "Demon",        10,15,.5f,RegionDatabase.HELL,     10000,1000,200, 200,IMGROOT+"/demon.gif");
		// @formatter:on

		@Nonnull
		private final String name;
		@Nonnull
		private final RangedValueObject attack;
		private final float attackSpeed;
		private final long reputation;
		private final int maxHp;
		private final int xp;
		private final int goldDrop;
		@Nonnull
		private final String imageUrl;
		@Nonnull
		private final RangedValueObject defense;

		private EnemyDatabase(@Nonnull String name, @Nonnegative int minAttack, @Nonnegative int maxAttack, // NOSONAR
				@Nonnegative float attackSpeed, @Nonnull RegionDatabase region, @Nonnegative long reputationOffset,
				@Nonnegative int xp, @Nonnegative int maxHp, @Nonnegative int goldDrop, @Nonnull String imageUrl) {
			this.name = name;
			this.attack = new RangedValue(minAttack, maxAttack);
			this.attackSpeed = attackSpeed;
			this.reputation = region.getReputation() + reputationOffset;
			this.maxHp = maxHp;
			this.xp = xp;
			this.goldDrop = goldDrop;
			this.imageUrl = imageUrl;
			this.defense = new RangedValue(Math.round(maxHp * .06f), Math.round(maxHp * .1f));
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getImageUrl() {
			return this.imageUrl;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		@Override
		public RangedValueObject getAttack() {
			return this.attack;
		}

		@Override
		public int getGoldDrop() {
			return this.goldDrop;
		}

		@Override
		public int getMaxHp() {
			return this.maxHp;
		}

		@Override
		public ItemRarityPack getItemDrops() {
			return Assets.ENEMY_DROP_PACK.get();
		}

		@Override
		public int getXpDrop() {
			return this.xp;
		}

		@Override
		public float getSpeed() {
			return this.attackSpeed;
		}

		@Override
		public boolean isBoss() {
			return false;
		}

		@Override
		public RangedValueObject getDefense() {
			return this.defense;
		}

		@SuppressWarnings("null")
		@Override
		public String getIdentification() {
			return this.name();
		}

		@Override
		public String getDatabaseToken() {
			return "E";
		}

	}

	@SuppressWarnings("null")
	public static void fightEnemy(@Nonnull FightInfo fight) {
		Combat.fightEnemy(fight, (win, feed) -> {
			if (win) {
				Item item = null;
				ItemRarityPack drop = fight.getPlayerFight().getEnemy().getInfo().getItemDrops();
				if (drop != null && Utilities.getChance(drop.getRarity()))
					item = drop.maybeGetItem(fight.getPlayerDungeon().getReputation(fight.getPlayer().getXp()));

				if (item != null)
					fight.getPlayer().getInventory().addItem(item, 1);

				fight.getPlayerDungeon().getStatistics().enemySlain();
				fight.getPlayer()
						.setGold(
							fight.getPlayer().getGold() + fight.getPlayerFight().getEnemy().getInfo().getGoldDrop());
				fight.getPlayer()
						.setXp(fight.getPlayer().getXp() + fight.getPlayerFight().getEnemy().getInfo().getXpDrop());

				fight.getChannel()
						.sendMessage(Combat.getVictoryStatus(fight.getPlayerFight().getEnemy(), feed,
							item == null ? null : item))
						.queue();

				if (fight.getPlayerFight().getEnemy().getInfo().isBoss()) {
					RegionDatabase region = fight.getPlayerDungeon().getRegion(fight.getPlayer().getXp());
					fight.getPlayerDungeon().setLastRegionBoss(region);
					Assets.NEXT_REGION_PREPARED.generate(region).display(fight.getChannel());
				}

				Utilities.sleep(1000);
				fight.getPlayerDungeon()
						.setHp(fight.getPlayerDungeon().getHp() + Dungeon.TURN_HEAL, fight.getPlayer().getMaxHp());

			} else {
				fight.getPlayerDungeon().setHp(0, fight.getPlayer().getMaxHp());
			}

			fight.getPlayerDungeon().removePlayerFight();
			fight.getPlayerDungeon().addStep();
			Dungeon.displayDungeon(new DungeonInfo(fight));
		});
	}

}
