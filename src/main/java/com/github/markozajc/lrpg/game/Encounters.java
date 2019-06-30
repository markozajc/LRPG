package com.github.markozajc.lrpg.game;

import java.util.Arrays;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.utilities.BotUtils;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.BooleanDialog;
import com.github.markozajc.lrpg.game.Enemies.Enemy;
import com.github.markozajc.lrpg.game.Enemies.EnemyDatabase;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.BattleItemDatabase;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.Items.ItemDatabase;
import com.github.markozajc.lrpg.game.Items.UsableItemDatabase;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithReputation;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;

import net.dv8tion.jda.core.EmbedBuilder;

class Encounters {

	private Encounters() {}

	public enum EncounterType implements ObjectWithReputation {
		MERCHANT(dungeon -> {
			Item item = Utilities.getRandomItem(dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp()),
				1.5f, Assets.ARMOR_PACK.get(), Assets.WEAPONS_PACK.get(), Assets.HEALIES_PACK);

			if (item != null) {
				long price = Utilities.HALF_UP_OR_DOWN_RANDOMIZE.applyAsLong(
					item.getBasePrice() + dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp()) * 5);

				MessageDialog dialog = new EmbedDialog(new EmbedBuilder().setColor(Constants.LITHIUM)
						.setThumbnail(Assets.MERCHANT_IMAGE)
						.setTitle(Assets.GOLD_EMOTE + " You stumble across a **small shop**!")
						.setColor(Constants.NONE)
						.setDescription("The merchant is offering you a **" + item.getNameWithEmote() + "** for **"
								+ price + "** gold " + Assets.GOLD_EMOTE
								+ ". Do you want to purchase this item?\n\nYou have **" + dungeon.getPlayer().getGold()
								+ " gold **" + Assets.GOLD_EMOTE)
						.build());

				new BooleanDialog(dungeon.getContext(), dialog, d -> {
					if (d) {
						if (dungeon.getPlayer().getGold() < price) {
							Assets.NOT_ENOUGH_GOLD_MESSAGE.display(dungeon.getChannel());
						} else {
							dungeon.getPlayerDungeon().getStatistics().itemPurchased();
							dungeon.getPlayer().getInventory().addItem(item, 1);
							dungeon.getPlayer().setGold(dungeon.getPlayer().getGold() - price);
							dungeon.getChannel()
									.sendMessage(BotUtils.buildEmbed(
										"You purchase the **" + item.getNameWithEmote() + "** and leave the shop.",
										Constants.GREEN))
									.queue();
						}
					}
					Dungeon.displayDungeon(dungeon);
				}).display(dungeon.getChannel());

			} else {
				new BooleanDialog(dungeon.getContext(), Assets.ABANDONED_SHOP_PREPARED.generate(dungeon), d -> {
					if (d) {
						if (BotUtils.getRandom().nextBoolean()) {
							int gold = Utilities.getRandomGold(dungeon.getPlayer(), 0.2f);
							dungeon.getChannel()
									.sendMessage(BotUtils.buildEmbed(
										"Luckily there was nothing dangerous in there. You also found **" + gold
												+ " gold " + Assets.GOLD_EMOTE + " **.",
										Constants.GREEN))
									.queue();
							dungeon.getPlayer().setGold(dungeon.getPlayer().getGold() + gold);

						} else {
							dungeon.getChannel()
									.sendMessage(BotUtils.buildEmbed("Immediately after breaking into the shop, "
											+ "an evil spirit protecting it kicked you out and took away **one half** of your HP.",
										Constants.RED))
									.queue();
							dungeon.getPlayerDungeon()
									.setHp(dungeon.getPlayerDungeon().getHp() / 2, dungeon.getPlayer().getMaxHp());
						}
					} else {
						dungeon.getChannel()
								.sendMessage(
									BotUtils.buildEmbed("You let the shop be and continue exploring.", Constants.NONE))
								.queue();
					}
					Dungeon.displayDungeon(dungeon);
				}).display(dungeon.getChannel());
			}
		}),

		CHEST(dungeon -> {
			int keys = dungeon.getPlayer().getInventory().getQuantity(ItemDatabase.KEY);

			new BooleanDialog(dungeon.getContext(), Assets.CHEST_PREPARED.generate(keys), open -> {
				if (open) {
					// If the player says yes

					if (dungeon.getPlayer().getInventory().removeItem(ItemDatabase.KEY, 1)) {
						// If the player has a key
						dungeon.getPlayerDungeon().getStatistics().chestOpened();

						Item item = Utilities.getRandomItemWithFallback(
							dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp()), 2f,
							Assets.ALL_NO_RARITY_PACK, Assets.WEAPONS_PACK.get(), Assets.ARMOR_PACK.get(),
							Assets.HEALIES_PACK);

						if (item != null) {
							// If the chest has an item

							dungeon.getPlayer().getInventory().addItem(item, 1);
							dungeon.getChannel()
									.sendMessage(BotUtils.buildEmbed(
										"You insert the key into the keyhole and open the chest. There's a **"
												+ item.getNameWithEmote() + "** inside.",
										Constants.GREEN))
									.queue();

						} else {
							// If the chest has gold (doesn't have an item)

							int gold = Utilities.getRandomGold(dungeon.getPlayer(), 0.4f);
							dungeon.getPlayer().setGold(dungeon.getPlayer().getGold() + gold);

							dungeon.getChannel()
									.sendMessage(BotUtils
											.buildEmbed(
												"You insert the key into the keyhole and open the chest. There's **"
														+ gold + " gold" + Assets.GOLD_EMOTE + "** inside.",
												Constants.GREEN))
									.queue();
						}

					} else {
						// If the player doesn't have a key
						dungeon.getChannel()
								.sendMessage(BotUtils.buildEmbed(
									"You don't have a suitable key, so you leave the chest and continue exploring.",
									Constants.NONE))
								.queue();
					}
				}

				Dungeon.displayDungeon(dungeon);
			}).display(dungeon.getChannel());
		}),

		LOOT(dungeon -> {
			Item item = Utilities.getRandomItemWithFallback(
				dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp()), .8f, Assets.ITEMS_PACK,
				Assets.HEALIES_PACK);

			if (item != null) {
				// If found an item

				dungeon.getChannel()
						.sendMessage(BotUtils.buildEmbed(
							"You found a **" + item.getNameWithEmote() + "** while exploring.", Constants.GREEN))
						.queue();
				dungeon.getPlayer().getInventory().addItem(item, 1);

			} else {
				// If found gold

				int gold = Utilities.getRandomGold(dungeon.getPlayer(), 0.1f);
				dungeon.getChannel()
						.sendMessage(BotUtils.buildEmbed(
							"You found **" + gold + " gold " + Assets.GOLD_EMOTE + " ** while exploring.",
							Constants.GREEN))
						.queue();
				dungeon.getPlayer().setGold(dungeon.getPlayer().getGold() + gold);
			}

			Dungeon.displayDungeon(dungeon);
		}),

		SCROLLBOOK(dungeon -> new BooleanDialog(dungeon.getContext(), Assets.SCROLLBOOK_MESSAGE, read -> {
			if (read) {
				// If the player says yes
				dungeon.getPlayerDungeon().getStatistics().mysteriousBookRead();

				if (BotUtils.getRandom().nextBoolean()) {
					// If the book didn't hurt the player

					if (Utilities.getChance(0.2f)) {
						// If player was able to recover a scroll from the book

						Item item = null;
						if (Utilities.getChance(0.85f)) {
							// If the scroll is a wipeout one
							item = BattleItemDatabase.SCROLL_WIPEOUT;

						} else {
							// If the scroll is an upgrade one
							item = UsableItemDatabase.SCROLL_UPGRADE;
						}

						dungeon.getPlayer().getInventory().addItem(item, 1);
						dungeon.getChannel()
								.sendMessage(
									BotUtils.buildEmbed("After reading the book, you successfully recovered a **"
											+ item.getNameWithEmote() + "**.",
										Constants.GREEN))
								.queue();

					} else {
						// If the player got XP from reading
						int xp = Utilities.intRange(10, 50);
						dungeon.getPlayer().setXp(dungeon.getPlayer().getXp() + xp);
						dungeon.getChannel()
								.sendMessage(BotUtils.buildEmbed("The book was about " + Assets.getRandomBookQuote()
										+ ". You got **" + xp + " XP** from reading it.",
									Constants.GREEN))
								.queue();

					}

				} else {
					// If the book hurt the player
					dungeon.getPlayerDungeon()
							.setHp(dungeon.getPlayerDungeon().getHp() / 2, dungeon.getPlayer().getMaxHp());
					dungeon.getChannel()
							.sendMessage(BotUtils.buildEmbed(
								"Reading the contents of the book out loud summoned an evil spirit that took away **one half** of your current health.",
								Constants.RED))
							.queue();

				}

			} else {
				// If the player says no
				dungeon.getChannel()
						.sendMessage(BotUtils.buildEmbed(
							"You leave the mysterious book where it is and continue exploring.", Constants.NONE))
						.queue();
			}

			Dungeon.displayDungeon(dungeon);
		}).display(dungeon.getChannel())),

		RESURECTION(RegionDatabase.HELL.getReputation() + 15000, dungeon -> {
			int ankhs = dungeon.getPlayer().getInventory().getQuantity(ItemDatabase.ANKH);

			new BooleanDialog(dungeon.getContext(), Assets.RESURRECTON_PREPARED.generate(ankhs), use -> {

				if (use) {
					// If the player says yes

					if (dungeon.getPlayer().getInventory().removeItem(ItemDatabase.ANKH, 1)) {
						// If the player has an ankh

						new BooleanDialog(dungeon.getContext(), Assets.RESURRECTION_CONFIRM_MESSAGE, confirm -> {
							if (confirm) {
								for (MessageDialog message : Assets.getResurrectionMessages()) {
									message.display(dungeon.getChannel());
									Utilities.sleep(4000);
								}

								Enemies.fightEnemy(new FightInfo(dungeon, RegionDatabase.HELL.getBoss().getEnemy()));
								// Fights Yog
							} else {
								Dungeon.displayDungeon(dungeon);
							}
						}).display(dungeon.getChannel());

					} else {
						// If the player doesn't have an ankh
						dungeon.getChannel()
								.sendMessage(BotUtils.buildEmbed(
									"You don't have any ankhs, so you leave the device and continue exploring.",
									Constants.NONE))
								.queue();
						Dungeon.displayDungeon(dungeon);
					}
				} else {
					Dungeon.displayDungeon(dungeon);
				}
			}).display(dungeon.getChannel());
		});

		private final Consumer<DungeonInfo> action;
		private final long reputation;

		private EncounterType(Consumer<DungeonInfo> action) {
			this(0L, action);
		}

		private EncounterType(long reputation, Consumer<DungeonInfo> action) {
			this.action = action;
			this.reputation = reputation;
		}

		@Override
		public long getReputation() {
			return this.reputation;
		}

		public void display(DungeonInfo dungeon) {
			this.action.accept(dungeon);
			dungeon.getPlayerDungeon().addStep();
			dungeon.getPlayerDungeon()
					.setHp(dungeon.getPlayerDungeon().getHp() + Dungeon.TURN_HEAL, dungeon.getPlayer().getMaxHp());
		}

	}

	public static EncounterType getRandomEncounterType(long reputation) {
		return Utilities.getRandomElement(
			Utilities.getObjectsWithReputation(Arrays.asList(EncounterType.values()), reputation));
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// ENEMIES
	//////////////////////////////////////////////////////////////////////////////////////
	@Nonnull
	public static Enemy getRandomEnemy(DungeonInfo dungeon) {
		return new Enemy(Utilities.getRandomMustMatch(EnemyDatabase.values(), e -> {
			long reputation = dungeon.getPlayerDungeon().getReputation(dungeon.getPlayer().getXp());
			return e.getReputation() <= reputation
					&& Utilities.GET_REGION.apply(reputation).equals(Utilities.GET_REGION.apply(e.getReputation()));
		}));
	}

}
