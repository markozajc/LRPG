package com.github.markozajc.lrpg.game;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.markozajc.lithium.utilities.dialogs.waiter.BooleanDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.GearItem;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;

class Castle {

	private Castle() {}

	public static void displayCastle(GameInfo game) {
		if (game.getPlayer().getPlayerDungeon() == null) {
			new ChoiceDialog(game.getContext(), Assets.CASTLE_STATUS_PREPARED.generate(game), choice -> {
				if (choice == 0) {
					// Dungeon
					new BooleanDialog(game.getContext(), Assets.DESCENT_MESSAGE, decision -> {
						if (decision) {
							Dungeon.displayDungeon(new DungeonInfo(game));
						} else {
							displayCastle(game);
						}
					}).display(game.getChannel());

				} else if (choice == 1) {
					// Inventory
					Items.openInventoryRepeating(game, () -> displayCastle(game));

				} else if (choice == 2) {
					// Unequip all
					ArmorItem armor = null;
					WeaponItem weapon = null;

					if (!game.getPlayer().getArmor().getType().equals(ArmorDatabase.NAKED)) {
						armor = game.getPlayer().getArmor();
						game.getPlayer().getInventory().addItem(armor, 1);
						game.getPlayer().setArmor(new ArmorItem(ArmorDatabase.NAKED, 0));
					}

					if (!game.getPlayer().getWeapon().getType().equals(WeaponDatabase.FISTS)) {
						weapon = game.getPlayer().getWeapon();
						game.getPlayer().getInventory().addItem(weapon, 1);
						game.getPlayer().setWeapon(new WeaponItem(WeaponDatabase.FISTS, 0));
					}

					List<GearItem<?>> unequipped = Arrays.asList(armor, weapon)
							.stream()
							.filter(Objects::nonNull)
							.collect(Collectors.toList());

					if (unequipped.isEmpty()) {
						Assets.NOTHING_UNEQUIPPED_MESSAGE.display(game.getChannel());

					} else {
						Assets.UNEQUIP_ALL_PREPARED.generate(unequipped).display(game.getChannel());
					}

					Utilities.sleep(1500);
					displayCastle(game);

				} else if (choice == 3) {
					Utilities.confirmExit(game.getContext(), () -> displayCastle(game));
				}

			}, "p", "i", "u", "exit").display(game.getChannel());

		} else {
			Dungeon.displayDungeon(new DungeonInfo(game));
		}
	}
}
