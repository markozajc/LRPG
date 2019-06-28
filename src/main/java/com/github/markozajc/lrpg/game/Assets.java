package com.github.markozajc.lrpg.game;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.BotUtils;
import com.github.markozajc.lithium.utilities.Counter;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.MessageDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;
import com.github.markozajc.lrpg.game.Enemies.RegionDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.ArmorItem;
import com.github.markozajc.lrpg.game.Items.BattleItem;
import com.github.markozajc.lrpg.game.Items.BattleItemDatabase;
import com.github.markozajc.lrpg.game.Items.DungeonItem;
import com.github.markozajc.lrpg.game.Items.GearItem;
import com.github.markozajc.lrpg.game.Items.HealingItem;
import com.github.markozajc.lrpg.game.Items.HealingItemDatabase;
import com.github.markozajc.lrpg.game.Items.Item;
import com.github.markozajc.lrpg.game.Items.ItemDatabase;
import com.github.markozajc.lrpg.game.Items.ItemRarityPack;
import com.github.markozajc.lrpg.game.Items.ItemStack;
import com.github.markozajc.lrpg.game.Items.UsableItem;
import com.github.markozajc.lrpg.game.Items.UsableItemDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponItem;
import com.github.markozajc.lrpg.game.LRpgExposed.ObjectWithRarity;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValue;
import com.github.markozajc.lrpg.game.LRpgExposed.RangedValueObject;
import com.github.markozajc.lrpg.game.Statuses.DungeonInfo;
import com.github.markozajc.lrpg.game.Statuses.FightInfo;
import com.github.markozajc.lrpg.game.Statuses.GameInfo;

import net.dv8tion.jda.core.EmbedBuilder;

public class Assets {

	private Assets() {}

	//////////////////////////////////////////////////////////////////////////////////////
	// EMOTES "EMOTE"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final String GO_EMOTE = "<:Go:497476518214369280>";
	public static final String BACK_EMOTE = "<:Back:497476517832687627>";
	public static final String GOLD_EMOTE = "<:Gold:495591594863624194>";
	public static final String INVENTORY_EMOTE = "<:Inventory:496689425191796754>";
	public static final String QUESTION_EMOTE = "<:Info:497476518130352152>";
	public static final String GUARD_EMOTE = "<:Guard:496688778870652959>";
	public static final String UP_EMOTE = "<:Up:503637463294803992>";
	public static final String TELEPORT_EMOTE = "<:Teleport:503656704194379793>";
	public static final String BOSS_EMOTE = "<:Boss:508243647825838081>";
	public static final String BOOK_EMOTE = "<:bluebook:590644094892507167>";
	public static final String CHEST_EMOTE = "<:chest:590644295472644292>";
	public static final String AMULET_EMOTE = "<:amulet:590644655134212126>";
	public static final String TOMB_EMOTE = "<:tomb:590644431145926681>";
	public static final String MANUAL_EMOTE = "<:manual:591337287695204374>";
	public static final String BONES_EMOTE = "<:bones:591367558486294539>";
	public static final String RESURRECTION_DEVICE_EMOTE = "<:resurrection:591564040778547201>";

	//////////////////////////////////////////////////////////////////////////////////////
	// IMAGES "IMAGE"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final String CASTLE_IMAGE = "https://i.postimg.cc/yYPMfQnq/Castle.png";
	public static final String SCROLLBOOK_IMAGE = "https://i.postimg.cc/pLr7q6cr/Scrollbook.png";
	public static final String MERCHANT_IMAGE = "https://i.postimg.cc/Xvrm2ng0/Merchant.png";
	public static final String CLOSED_SHOP_IMAGE = "https://i.postimg.cc/d0gzmQgV/Closed-Shop.png";
	public static final String LOCKED_CHEST_IMAGE = "https://i.postimg.cc/X7c90jJJ/Chest.png";
	public static final String BACKPACK_IMAGE = "https://i.postimg.cc/Bn1VGw1b/Backpack.png";
	public static final String RESURRECTION_DEVICE_IMAGE = "https://i.postimg.cc/76R20kXH/resurrection.png";

	//////////////////////////////////////////////////////////////////////////////////////
	// ARRAYS (private)
	//////////////////////////////////////////////////////////////////////////////////////
	private static final String[] MYSTERIOUS_BOOK_QUOTES = {
			"the Prince of the Undead realm",
			"the Blue Robot",
			"the hungry black slime",
			"the neverending halls",
			"the poisonous flower",
			"the red ghost that haunts the underworld",
			"the usability of potions",
			"the usability of scrolls",
			"the green potion and how to make one",
			"the enemies of the dungeon",
			"the annoying crabs",
			"Yog and his tyranny",
			"Yog-Dzewa and how to defeat him",
			"the pixels of the Shattered Dungeon",
			"Watabou and his creation",
			"Evan and his creation"
	};

	public static String getRandomBookQuote() {
		return Utilities.getRandomElement(MYSTERIOUS_BOOK_QUOTES);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// TEXT "TEXT"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final String ITEM_HEALING_USE_TEXT = "**This item can only be used in the dungeon.**\n";
	public static final String ITEM_BATTLE_USE_TEXT = "**This item can only be used during a battle.**\n";
	public static final String ITEM_DUNGEON_USE_TEXT = "**This item can only be used in the dungeon.**\n";
	public static final String ITEM_GEAR_USE_TEXT = "**This item can't be equipped while in the dungeon.**\n";
	public static final String ITEM_USABLE_USE_TEXT = "**This item can be used anytime.**\n";
	public static final String ITEM_ITEM_USE_TEXT = "**This item has no apparent use. You may be able to use it in a combination with something else**\n";
	public static final String GOO_PUMP_TEXT = "- Goo is preparing a powerful attack. \uD83D\uDEE1 Guard yourself while you can!\n";
	public static final String SCROLL_UPGRADE_DESCRIPTION_TEXT = "This scroll allows the reader to upgrade one piece of their gear to a higher level - "
			+ "the higher the level, the better the gear. The highest level you can upgrade your gear to is **"
			+ GearItem.MAX_GEAR_LEVEL + "**";
	public static final String POTION_EXPERIENCE_DESCRIPTION_TEXT = "Drinking this potion will instantly raise your experience level.";
	public static final String SCROLL_WIPEOUT_DESCRIPTION_TEXT = "Reading this scroll will instantly kill most enemies. "
			+ "More powerful enemies (such as bosses) are, however, unaffected.";

	//////////////////////////////////////////////////////////////////////////////////////
	// DIALOGS "MESSAGE"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final MessageDialog EXIT_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed(QUESTION_EMOTE + " Exit?",
				"Are you sure you want to exit LRPG? **Your progress will be saved automatically**.", Constants.NONE));
	public static final MessageDialog INVENTORY_INVALID_CHOICE_MESSAGE = new EmbedDialog(EmbedDialog.generateEmbed(
		"Invalid choice. Please choose between **U**/**I** + item number or **EXIT**.", Constants.NONE));
	public static final MessageDialog DESCENT_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("Are you sure you want to descend into the dungeon?", Constants.LITHIUM));
	public static final MessageDialog SURRENDER_MESSAGE = new EmbedDialog(EmbedDialog.generateEmbed(
		BONES_EMOTE + " Surrender?",
		"Are you sure you want to surrender? You'll **lose your reputation and equipped items** and **retain all non-equipped items, gold and experience**. "
				+ "Choose wisely.",
		Constants.RED));
	public static final MessageDialog INVENTORY_CANT_USE_MESSAGE = new EmbedDialog(EmbedDialog.generateEmbed(
		"This item can't be used right now. Check out what it does with `I <item number>`.", Constants.NONE));
	public static final MessageDialog INVENTORY_DONT_HAVE_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You don't have that item.", Constants.RED));
	public static final MessageDialog MAX_GUARD_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You are already at your max guard!", Constants.RED));
	public static final MessageDialog GEAR_CANT_EQUIP_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You can't change your gear while in the dungeon.", Constants.RED));
	public static final MessageDialog NOT_ENOUGH_GOLD_MESSAGE = new EmbedDialog(EmbedDialog.generateEmbed(
		"You don't have enough gold " + Assets.GOLD_EMOTE + " to purchase that item.", Constants.RED));
	public static final MessageDialog RESURRECTION_CONFIRM_MESSAGE = new EmbedDialog(EmbedDialog.generateEmbed(
		QUESTION_EMOTE + " Are you sure you want to do this?",
		"Resurrection of ancient beings is a __very__ dangerous thing - the being you resurrect will attack you, no questions asked. "
				+ "Are you sure you want to proceed with the resurrection?",
		Constants.LITHIUM));
	public static final MessageDialog NOTHING_UNEQUIPPED_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You don't have any gear equipped.", Constants.RED));
	public static final MessageDialog RETURN_TO_CASTLE_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed(QUESTION_EMOTE + " Are you sure?",
				"Are you sure you want to teleport back to your castle? **You'll loose all your current reputation, "
						+ "but you'll get to keep your items, gold and XP**.",
				Constants.LITHIUM));
	public static final MessageDialog SCROLLBOOK_MESSAGE = new EmbedDialog(new EmbedBuilder()
			.setThumbnail(Assets.SCROLLBOOK_IMAGE)
			.setTitle(Assets.BOOK_EMOTE + " You found a Mysterious Book!")
			.setColor(Constants.NONE)
			.appendDescription(
				"By reading from this mysterious book, you may be able to recover a useful scroll or ancient knowledge from it."
						+ " **However,** the mysterious book is unstable and reading it may easily have unwanted consequences."
						+ "\nDo you want to read it?")
			.build());
	public static final MessageDialog ITEM_ONLY_DUNGEON_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You can only use this item when in dungeon!", Constants.RED));
	public static final MessageDialog ITEM_ONLY_DUNGEON_FIGHT_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You can only use this item in a fight or while in dungeon!", Constants.RED));
	public static final MessageDialog ITEM_ONLY_FIGHT_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You can only use this item in a fight!", Constants.RED));
	public static final MessageDialog HEALIE_NOGAIN_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("You are already at maximum health.", Constants.NONE));
	public static final MessageDialog BYE_MESSAGE = new EmbedDialog(
			EmbedDialog.generateEmbed("**Bye!**", Constants.LITHIUM));

	private static final EmbedDialog[] RESURRECTION_MESSAGES = new EmbedDialog[] {
			new EmbedDialog(EmbedDialog.generateEmbed("You insert the ankh into the hole on top of the hourglass...",
				Constants.NONE)),
			new EmbedDialog(EmbedDialog.generateEmbed("You invert the bulbs...", Constants.NONE)),
			new EmbedDialog(EmbedDialog.generateEmbed("You feel the urge to run away, but you can't move your legs...",
				Constants.RED)),
			new EmbedDialog(EmbedDialog.generateEmbed("A bright flash of red light envelopes you...", Constants.RED)),
			new EmbedDialog(EmbedDialog.generateEmbed("An unknown voice says \"Hope is an illusion.\"", Constants.RED))
	};

	public static MessageDialog[] getResurrectionMessages() {
		return RESURRECTION_MESSAGES.clone();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PREPARED DIALOGS "PREPARED"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final PreparedDialog<GameInfo> CASTLE_STATUS_PREPARED = new PreparedEmbedDialog<>(
			g -> new EmbedBuilder().setColor(Constants.LITHIUM)
					.setThumbnail(Assets.CASTLE_IMAGE)
					.setAuthor(g.getAuthor().getName() + "'s castle", null, g.getAuthor().getAvatarUrl())
					.addField("XP", Utilities.getXpBar(g.getPlayer()), false)
					.addField(Utilities.getGearField(g))
					.addField("Stash", "You have **" + g.getPlayer().getGold() + " gold " + Assets.GOLD_EMOTE + "**",
						true)
					.addField("Actions",
						g.getPlayer().getWeapon().getEmote() + " **[P]**lay\n" + Assets.INVENTORY_EMOTE
								+ " **[I]**nventory\n" + ArmorDatabase.NAKED.getEmote() + " **[U]**nequip all gear\n"
								+ Assets.BACK_EMOTE + " **Exit**",
						false)
					.build());
	public static final PreparedDialog<List<GearItem<?>>> UNEQUIP_ALL_PREPARED = new PreparedEmbedDialog<>(
			unequipped -> BotUtils.buildEmbed("You unequip your " + unequipped.stream()
					.map(t -> "**" + t.getNameWithEmote() + "**")
					.collect(Collectors.joining(" and ")) + ".",
				Constants.GREEN));
	public static final PreparedDialog<DungeonInfo> DEATH_PREPARED = new PreparedEmbedDialog<>(d -> EmbedDialog
			.setFooterUser(new EmbedBuilder(), d.getAuthor())
			.setColor(Constants.RED)
			.setTitle(Assets.TOMB_EMOTE + " You died..")
			.addField("Statistics",
				Assets.BONES_EMOTE + " **" + d.getPlayer().getStatistics().getEnemiesSlain() + "** enemies slain,\n"
						+ Assets.UP_EMOTE + " **" + d.getPlayer().getStatistics().getHealiesConsumed()
						+ "** healing items consumed,\n" + Assets.CHEST_EMOTE + " **"
						+ d.getPlayer().getStatistics().getChestsOpened() + "** chests opened,\n" + Assets.BOOK_EMOTE
						+ " **" + d.getPlayer().getStatistics().getMysteriousBooksRead() + "** mysterious books read,\n"
						+ Assets.GOLD_EMOTE + " **" + d.getPlayer().getStatistics().getItemsPurchased()
						+ "** items purchased and\n" + Assets.AMULET_EMOTE + " **" + d.getPlayer().getReputation()
						+ "** reputation earned.",
				false)
			.addField("Gear lost",
				d.getPlayer().getWeapon().getNameWithEmote() + " and\n" + d.getPlayer().getArmor().getNameWithEmote(),
				false)
			.build());
	public static final PreparedDialog<RegionDatabase> NEXT_REGION_PREPARED = new PreparedEmbedDialog<>(r -> EmbedDialog
			.generateEmbed(Assets.UP_EMOTE + " Welcome to the **" + r.getName() + "**!", Constants.GREEN));
	public static final PreparedDialog<DungeonInfo> DUNGEON_STATUS_PREPARED = new PreparedEmbedDialog<>(d -> {
		RegionDatabase region = d.getPlayer().getRegion();
		EmbedBuilder builder = EmbedDialog.setFooterUser(new EmbedBuilder(), d.getAuthor())
				.setTitle("You are in the " + region.getName())
				.setColor(Constants.LITHIUM)
				.setThumbnail(region.getImageUrl())
				.addField("Stats",
					"XP: " + Utilities.getXpBar(d.getPlayer()) + "\nHealth: "
							+ Utilities.displayProgress(d.getPlayer().getHp(), d.getPlayer().getMaxHp(), 16) + " "
							+ d.getPlayer().getHp() + "/" + d.getPlayer().getMaxHp() + "\nReputation: **"
							+ d.getPlayer().getReputation() + "**\nGold: **" + d.getPlayer().getGold() + " "
							+ Assets.GOLD_EMOTE + "**",
					true)
				.addField(Utilities.getGearField(d));

		if (Dungeon.getBoss(d) != null) {
			builder.addField("Actions",
				Assets.BOSS_EMOTE + " **[E]**nter the boss chamber\n" + Assets.INVENTORY_EMOTE + " **[I]**nventory\n"
						+ Assets.TELEPORT_EMOTE + " **[R]**eturn to castle\n" + Assets.BACK_EMOTE + " **Exit**",
				false);
		} else {
			builder.addField("Actions",
				Assets.GO_EMOTE + " **[E]**xplore\n" + Assets.INVENTORY_EMOTE + " **[I]**nventory\n"
						+ Assets.TELEPORT_EMOTE + "**[R]**eturn to castle\n" + Assets.BACK_EMOTE + " **Exit**",
				false);
		}

		return builder.build();
	});
	public static final PreparedDialog<DungeonInfo> ABANDONED_SHOP_PREPARED = new PreparedEmbedDialog<>(d -> EmbedDialog
			.setFooterUser(new EmbedBuilder(), d.getAuthor())
			.setThumbnail(Assets.CLOSED_SHOP_IMAGE)
			.setTitle(Assets.BONES_EMOTE + " You stumble across an **abandoned shop**!")
			.setColor(Constants.NONE)
			.setDescription(
				"This shop has been abandoned - either because the merchant overlooking it has found another job "
						+ "or because they have died. The shop might contain dangerous hazards, "
						+ "but it might also contain leftover gold " + Assets.GOLD_EMOTE + ". Do you want to rob it?")
			.build());
	public static final PreparedDialog<Integer> NEXT_LEVEL_PREPARED = new PreparedEmbedDialog<>(lvl -> EmbedDialog
			.generateEmbed(Assets.UP_EMOTE + " **Level up!** Welcome to level **" + lvl + "**!", Constants.GREEN));
	public static final PreparedDialog<Integer> CHEST_PREPARED = new PreparedEmbedDialog<>(keys -> new EmbedBuilder()
			.setThumbnail(Assets.LOCKED_CHEST_IMAGE)
			.setTitle(Assets.CHEST_EMOTE + " You stumble across a **locked chest**!")
			.setColor(Constants.NONE)
			.setDescription(
				"This chest appears to be locked, but it could contain some really good loot. To you want to open it, you need a "
						+ ItemDatabase.KEY.getNameWithEmote()
						+ ". Do you want to open it? (opening will consume a key)\n\nYou have **" + keys + " "
						+ Utilities.plural("key", keys) + ItemDatabase.KEY.getEmote() + "**")
			.build());
	public static final PreparedDialog<Integer> RESURRECTON_PREPARED = new PreparedEmbedDialog<>(
			ankhs -> new EmbedBuilder().setThumbnail(Assets.RESURRECTION_DEVICE_IMAGE)
					.setTitle(Assets.RESURRECTION_DEVICE_EMOTE + " You found a resurrection device!")
					.setColor(Constants.NONE)
					.appendDescription(
						"This hourglass-like device appears to be from the era of the War between the Demons. "
								+ "You might be able to use it to resurrect an ancient being by plugging an **"
								+ ItemDatabase.ANKH.getNameWithEmote()
								+ "** into dedicated hole and inverting the bulbs. Do you want to do that?\n\nYou have **"
								+ ankhs + " " + Utilities.plural("ankh", ankhs) + ItemDatabase.ANKH.getEmote() + "**")
					.build());
	public static final PreparedDialog<Player> UPGRADE_PREPARED = new PreparedEmbedDialog<>(
			p -> EmbedDialog.generateEmbed(
				"What to upgrade?\n" + p.getWeapon().getEmote() + " **[W]**eapon\n" + p.getArmor().getEmote()
						+ " **[A]**rmor\n" + Assets.BACK_EMOTE + " **Exit** upgrade nothing, don't consume the scroll",
				Constants.NONE));
	public static final PreparedDialog<Integer> POTION_EXPERIENCE_GAIN_PREPARED = new PreparedEmbedDialog<>(
			xpgain -> EmbedDialog.generateEmbed(UP_EMOTE + " You gained **" + xpgain + "** XP!", Constants.GREEN));
	public static final PreparedDialog<Integer> HEALIE_GAIN_PREPARED = new PreparedEmbedDialog<>(
			hpgain -> EmbedDialog.generateEmbed(UP_EMOTE + " You restored **" + hpgain + " HP**.", Constants.GREEN));
	public static final PreparedDialog<GameInfo> INVENTORY_STATUS_PREPARED = new PreparedEmbedDialog<>(g -> {
		EmbedBuilder builder = new EmbedBuilder().setColor(Constants.LITHIUM)
				.setThumbnail(Assets.BACKPACK_IMAGE)
				.setTitle(g.getAuthor().getName() + "'s inventory");

		if (g instanceof DungeonInfo)
			builder.addField("Health", Utilities.displayProgress(g.getPlayer().getHp(), g.getPlayer().getMaxHp(), 20)
					+ " " + g.getPlayer().getHp() + "/" + g.getPlayer().getMaxHp(),
				false);
		// Add health if player is in dungeon

		Counter c = new Counter(0);
		builder.addField("Inventory", g.getPlayer().getInventory().getItems().stream().map(is -> {
			c.count();
			return "**" + c.getCount() + "** - " + is.getQuantity() + "x " + is.getItem().getNameWithEmote();
		}).collect(Collectors.joining("\n")), false);
		// Add a list of items

		builder.addField("Actions",
			Assets.GO_EMOTE + " **[U]**se\n" + Assets.QUESTION_EMOTE + " **[I]**nfo\n+ Item number\n--OR--\n"
					+ Assets.BACK_EMOTE + " **Exit**\n_(type in the desired action, "
					+ "for example `U 1` will use the first item on the list)_",
			false);
		// Add a list of actions

		return builder.build();
	});
	public static final PreparedDialog<Integer> INVENTORY_INDEX_NOT_FOUND = new PreparedEmbedDialog<>(
			index -> EmbedDialog.generateEmbed(
				"Item number **" + (index + 1) + "** has not been found in your inventory.", Constants.RED));
	public static final PreparedDialog<ItemStack> ITEM_INFO_PREPARED = new PreparedEmbedDialog<>(is -> {
		EmbedBuilder builder = new EmbedBuilder().setColor(Constants.NONE)
				.setTitle(is.getItem().getNameWithEmote())
				.appendDescription("_" + is.getItem().getDescription() + "_")
				.setFooter("You have [" + is.getQuantity() + "] of this item.", null);

		if (is.getItem() instanceof HealingItem) {
			builder.appendDescription(Assets.ITEM_HEALING_USE_TEXT);

		} else if (is.getItem() instanceof BattleItem) {
			builder.appendDescription(Assets.ITEM_BATTLE_USE_TEXT);

		} else if (is.getItem() instanceof DungeonItem) {
			builder.appendDescription(Assets.ITEM_DUNGEON_USE_TEXT);

		} else if (is.getItem() instanceof GearItem) {
			builder.appendDescription(Assets.ITEM_GEAR_USE_TEXT);

		} else if (is.getItem() instanceof UsableItem) {
			builder.appendDescription(Assets.ITEM_USABLE_USE_TEXT);

		} else {
			builder.appendDescription(Assets.ITEM_ITEM_USE_TEXT);
		}

		return builder.build();
	});
	public static final PreparedDialog<Item> ITEM_CANT_USE_PREPARED = new PreparedEmbedDialog<>(item -> EmbedDialog
			.generateEmbed("You can not use the **" + item.getNameWithEmote() + "** right now.", Constants.NONE));
	public static final PreparedDialog<FightInfo> FIGHT_STATUS_PREPARED = new PreparedEmbedDialog<>(f -> {
		EmbedBuilder builder = EmbedDialog.setFooterUser(new EmbedBuilder(), f.getAuthor())
				.setColor(Constants.NONE)
				.setThumbnail(f.getEnemy().getInfo().getImageUrl());

		if (f.getEnemy().getInfo().isBoss()) {
			builder.setTitle(Assets.BOSS_EMOTE + f.getEnemy().getInfo().getName() + " has awoken!");
		} else {
			builder.setTitle("A " + f.getEnemy().getInfo().getName() + " has attacked");
		}
		// Adds the appropriate title

		String feedString = Combat.trimFeed(f.getFeed(), 6);
		if (feedString.length() > 0)
			builder.setDescription("```diff\n" + feedString + "```");
		// Appends the feed if it exists

		builder.addField("HP",
			"You:  " + Utilities.displayProgress(f.getPlayer().getHp(), f.getPlayer().getMaxHp()) + " "
					+ f.getPlayer().getHp() + "/" + f.getPlayer().getMaxHp() + "\nEnemy: "
					+ Utilities.displayProgress(f.getEnemy().getHp(), f.getEnemy().getInfo().getMaxHp()) + " "
					+ f.getEnemy().getHp() + "/" + f.getEnemy().getInfo().getMaxHp(),
			true)
				.addField(Utilities.getGearField(f))
				.addField("Actions",
					f.getPlayer().getWeapon().getEmote() + " **[H]**it\n" + Assets.GUARD_EMOTE + " **[G]**uard\n"
							+ Assets.INVENTORY_EMOTE + " **[I]**nventory\n" + Assets.BONES_EMOTE + "**[S]**urrender\n"
							+ Assets.BACK_EMOTE + " **Exit**",
					false);

		return builder.build();
	});
	public static final PreparedDialog<CommandContext> FIRST_LAUNCH_PREPARED = new PreparedEmbedDialog<>(c -> {

		return EmbedDialog.setAuthorUser(new EmbedBuilder(), c.getUser())
				.setTitle("Welcome to **LRPG**!")
				.setThumbnail(RegionDatabase.SEWERS.getImageUrl())
				.setFooter("Press [C] to continue...", null)
				.setDescription(
					"LRPG is a feature-rich Discord dungeon crawler. It features plenty of areas, bosses, enemies, items and unique encounters. "
							+ "If you ever get stuck or need help, you can always refer to the **" + Assets.MANUAL_EMOTE
							+ " manual** by running `" + c.getLithium().getConfiguration().getDefaultPrefix()
							+ "manual` or ask in the [official support server](https://discord.gg/asDUrbR). Because you're new to the game, you get a fistful of "
							+ Assets.GOLD_EMOTE
							+ " gold and a bunch of items, so be sure to check that out before you descend into the dungeon."
							+ "\nEnjoy!")
				.build();

	});

	//////////////////////////////////////////////////////////////////////////////////////
	// EXCEPTIONS "EXCEPTION"
	//////////////////////////////////////////////////////////////////////////////////////
	public static final IllegalStateException UTILITIES_EMPTY_CONTAINER_EXCEPTION = new IllegalStateException(
			"This method does not support empty containers.");
	public static final IllegalStateException ITEMRARITYPACK_NO_RARITY_EXCEPTION = new IllegalStateException(
			"This ItemRarityPack does not have a common rarity. It can be used as a fallback pack or to manually get an item, "
					+ "but it can not be used in eg. Utilities#getRandomItem");
	public static final IllegalArgumentException INVENTORY_NO_HAS_EXCEPTION = new IllegalArgumentException(
			"This inventory doesn't have that item.");

	//////////////////////////////////////////////////////////////////////////////////////
	// MISC
	//////////////////////////////////////////////////////////////////////////////////////
	public static final int GOO_PUMP_REQUIRED = 4;
	public static final float BASE_ENCOUNTER_CHANCE = .2f;
	public static final RangedValueObject GOO_ATTACK_NORMAL = new RangedValue(10, 15);
	public static final RangedValueObject GOO_ATTACK_PUMP = new RangedValue(45, 50);
	public static final RangedValueObject GOO_ATTACK_PUMP_FAIL = new RangedValue(0, 0);
	public static final Comparator<ObjectWithRarity> OBJECTWITHRARITY_COMPARATOR = (owr1, owr2) -> Float
			.compare(owr2.getRarity(), owr1.getRarity());
	public static final float HEALIE_RARITY = .3f;

	//////////////////////////////////////////////////////////////////////////////////////
	// ITEMRARITYPACKS "PACK"
	// (suppliers are used for mutable items (eg. gear))
	/////////////////////////////////////////////////////////////////////////////////////
	public static final Supplier<ItemRarityPack> WEAPONS_PACK = () -> new ItemRarityPack(GearItem.RARITY,
			Utilities.gearDatabaseToGearItems(WeaponDatabase.values()));
	public static final Supplier<ItemRarityPack> ARMOR_PACK = () -> new ItemRarityPack(GearItem.RARITY,
			Utilities.gearDatabaseToGearItems(ArmorDatabase.values()));
	public static final ItemRarityPack HEALIES_PACK = new ItemRarityPack(HEALIE_RARITY, HealingItemDatabase.values());
	public static final ItemRarityPack ITEMS_PACK = new ItemRarityPack(1f, ItemDatabase.values());
	public static final ItemRarityPack BATTLE_ITEMS_PACK = new ItemRarityPack(-1f, BattleItemDatabase.values());
	public static final ItemRarityPack USABLE_ITEMS_PACK = new ItemRarityPack(-1f, UsableItemDatabase.values());
	public static final ItemRarityPack ALL_NO_RARITY_PACK = ItemRarityPack.combine(-1f, ITEMS_PACK, BATTLE_ITEMS_PACK,
		USABLE_ITEMS_PACK);
	public static final Supplier<ItemRarityPack> ENEMY_DROP_PACK = () -> ItemRarityPack.combine(.05f,
		WEAPONS_PACK.get(), ARMOR_PACK.get(), HEALIES_PACK);
	public static final Supplier<ItemRarityPack> YOG_DROP_PACK = () -> new ItemRarityPack(1f,
			new WeaponItem(WeaponDatabase.HSWORD, 0), new ArmorItem(ArmorDatabase.HERO, 0));

}
