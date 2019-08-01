package com.github.markozajc.lrpg.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.markozajc.lithium.utilities.dialogs.waiter.BooleanDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lrpg.game.Enemies.BossDatabase;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;

public class Dungeon {

	private Dungeon() {}

	public static final int TURN_HEAL = 1;

	public static void death(GameInfo game) {
		game.getPlayer().setArmor(new ArmorItem(ArmorDatabase.NAKED, 0));
		game.getPlayer().setWeapon(new WeaponItem(WeaponDatabase.FISTS, 0));
		game.getPlayer().removePlayerDungeon();
	}

	@Nullable
	public static BossDatabase getBoss(DungeonInfo dungeon) {
		RegionDatabase region = dungeon.getPlayerDungeon().getRegion(dungeon.getPlayer().getXp());
		if (!region.equals(dungeon.getPlayerDungeon().getLastRegionBoss())) {
			// Region is guaranteed to be at least PRISON at this point
			return RegionDatabase.values()[region.ordinal() - 1].getBoss();
		}
		return null;
	}

	public static void displayDungeon(@Nonnull DungeonInfo dungeon) {
		if (dungeon.getPlayerDungeon().getHp() == 0) {
			Assets.DEATH_PREPARED.generate(dungeon).display(dungeon.getChannel());
			death(dungeon);
			Utilities.sleep(4000);
			Castle.displayCastle(new GameInfo(dungeon));
			return;
		}
		// Death check

		int level = dungeon.getPlayer().getLevel();
		if (level > dungeon.getPlayerDungeon().getLevelMark()) {
			dungeon.getPlayerDungeon().setLevelMark(level);
			Assets.NEXT_LEVEL_PREPARED.generate(dungeon.getPlayerDungeon().getLevelMark())
					.display(dungeon.getChannel());
			float healthPercentage = dungeon.getPlayerDungeon().getHp()
					/ (float) Utilities.CALCULATE_MAX_HEALTH.applyAsInt(dungeon.getPlayerDungeon().getLevelMark() - 1);
			dungeon.getPlayerDungeon()
					.setHp(Math.round(dungeon.getPlayer().getMaxHp() * healthPercentage),
						dungeon.getPlayer().getMaxHp());
			Utilities.sleep(1500);
		}
		// Level-up check

		if (dungeon.getPlayerDungeon().getPlayerFight() == null) {
			// If player isn't currently fighting an enemy (resumed from old session)

			new ChoiceDialog(dungeon.getContext(), Assets.DUNGEON_STATUS_PREPARED.generate(dungeon), c -> {

				if (c == 0) {
					// Explore
					BossDatabase boss = getBoss(dungeon);
					if (boss != null) {
						// If boss
						Enemies.fightEnemy(new FightInfo(dungeon, boss.getEnemy()));

					} else if (Utilities.getChance(Assets.BASE_ENCOUNTER_CHANCE
							* (dungeon.getPlayerDungeon().getStep() - dungeon.getPlayerDungeon().getLastEncounter()))) {
						// If encounter
						dungeon.getPlayerDungeon().resetLastEncounter();
						Encounters
								.getRandomEncounterType(
									dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp()))
								.display(dungeon);

					} else {
						// If enemy
						Enemies.fightEnemy(new FightInfo(dungeon, Encounters.getRandomEnemy(dungeon)));
					}

				} else if (c == 1) {
					// Inventory
					Items.openInventoryRepeating(dungeon, Utilities.DUNGEON_PICK, () -> displayDungeon(dungeon));

				} else if (c == 2) {
					// Return to castle
					new BooleanDialog(dungeon.getContext(), Assets.RETURN_TO_CASTLE_MESSAGE, decision -> {
						if (decision) {
							dungeon.getPlayer().removePlayerDungeon();

							Castle.displayCastle(new GameInfo(dungeon));
						} else {
							displayDungeon(dungeon);
						}
					}).display(dungeon.getChannel());

				} else if (c == 3) {
					Utilities.confirmExit(dungeon.getContext(), () -> displayDungeon(dungeon));
				}

			}, "e", "i", "r", "exit").display(dungeon.getChannel());

		} else {
			// If player is in a fight (resumed from old session)
			Enemies.fightEnemy(new FightInfo(dungeon));
		}

	}
}
