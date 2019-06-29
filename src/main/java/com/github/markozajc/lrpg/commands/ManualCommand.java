package com.github.markozajc.lrpg.commands;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.CommandCategory;
import com.github.markozajc.lithium.commands.utils.Commands;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.Counter;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.waiter.ChoiceDialog;
import com.github.markozajc.lrpg.bot.Categories;
import com.github.markozajc.lrpg.game.Assets;
import com.github.markozajc.lrpg.game.Items.ArmorDatabase;
import com.github.markozajc.lrpg.game.Items.UsableItemDatabase;
import com.github.markozajc.lrpg.game.Items.WeaponDatabase;

import net.dv8tion.jda.core.EmbedBuilder;

public class ManualCommand extends Command {

	@Override
	public void execute(CommandContext context, Parameters params) throws Throwable {
		displayManual(context);
	}

	private static void displayManual(CommandContext context) {
		new ChoiceDialog(context, MANUAL_HOME.generate(context), choice -> {
			if (choice == 8) {
				Assets.BYE_MESSAGE.display(context.getChannel());
			} else {
				new ChoiceDialog(context, MANUAL[choice].getDialog().generate(context), c -> displayManual(context),
						"b").display(context.getChannel());
			}
		}, "1", "2", "3", "4", "5", "6", "7", "8", "exit").display(context.getChannel());
	}

	@SuppressWarnings("null")
	@Override
	public CommandCategory getCategory() {
		return Categories.HELP;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public String getName() {
		return "Manual";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("help", "advice", "guide");
	}

	private static class ManualPage {

		private final String name;
		private final PreparedDialog<CommandContext> dialog;

		public ManualPage(String name, Function<CommandContext, String> content) {
			this.name = name;
			this.dialog = new PreparedEmbedDialog<>(
					context -> EmbedDialog.setAuthorUser(new EmbedBuilder(), context.getUser())
							.setTitle(name)
							.setDescription(content.apply(context))
							.setColor(Constants.LITHIUM)
							.setFooter("Type in [B] to return to the page selection", null)
							.build());
		}

		public String getName() {
			return this.name;
		}

		public PreparedDialog<CommandContext> getDialog() {
			return this.dialog;
		}

	}

	private static final ManualPage[] MANUAL = new ManualPage[] {

			new ManualPage(Assets.MANUAL_EMOTE + " Getting started",
					context -> "Welcome to LRPG! To get started, go to another channel (so that you will still be able to use the manual) and type in `"
							+ context.getLithium().getConfiguration().getDefaultPrefix()
							+ "lrpg`. First-time players get some gold and items for an easier start, so be sure to check that out before you descend into the dungeon!"),
			new ManualPage(Assets.BACK_EMOTE + " Navigation",
					context -> "Navigating around LRPG is very easy - in fact you've already learned how to navigate the castle and manual dialogs by opening this manual page! "
							+ "Most dialogs of LRPG are pretty straightforward - "
							+ "you either have to enter a letter or click on an emoji to proceed. When, for example, you want to open your "
							+ Assets.INVENTORY_EMOTE
							+ " **[I]**nventory, you just have to send a message \"I\" (it's case-insensitive so \"i\" will also work!). "
							+ "When you are presented with a yes/no choice, you just have to pick your decision from the buttons below the message box (so either "
							+ Constants.ACCEPT_EMOJI + " or " + Constants.DENY_EMOJI + "."),
			new ManualPage(Assets.INVENTORY_EMOTE + " Inventory",
					context -> "Your inventory is where your collected items go. You can open it up with the letter \"I\" in most dialogs. "
							+ "Your inventory is presented as a list of numbered items - to use one (drink, eat, equip, read, ...), just type in \"U <item's number>\" (for example \"U 1\"). "
							+ "If you aren't sure what an item does, you can type in \"I <item number>\" (I here stands for \"information\")."),
			new ManualPage(ArmorDatabase.HERO.getEmote() + " Gear",
					context -> "Your gear will help you win battles and not die while exploring. "
							+ "You can have two pieces of gear equipped at a time - weapon and armor. "
							+ "Your weapon boosts your damage and helps you defeat enemies more easily. "
							+ "Your armor boosts your defense and helps you resist attacks. "
							+ "A piece of hear has a **tier** and a **level**. "
							+ "Gear's tier is as it is and can not be changed. "
							+ "Gear's level can be upgraded to up to level 10 using a **"
							+ UsableItemDatabase.SCROLL_UPGRADE.getEmote()
							+ " Scroll of Upgrade** - these scrolls are very rare so you'll have to invest them carefully.\n"
							+ "You can not change your gear in the dungeon due to heavy magic present there. "
							+ "This means that if you get a piece of gear that you want to wear, you'll have to either **"
							+ Assets.TELEPORT_EMOTE + " Return to your castle** or proceed with your current gear.\n"
							+ "You will also lose all of your gear if you die in the dungeon, so plan ahead."),
			new ManualPage(Assets.GOLD_EMOTE + " The dungeon",
					context -> "The dungeon is the place where the core gameplay of LRPG takes place at. There are several regions of the dungeon, "
							+ "each being guarded with its own unique " + Assets.BOSS_EMOTE + " boss. "
							+ "When you get enough reputation, "
							+ "you will be able to either fight it and proceed to the next region or return to your castle if you feel like you aren't prepared for the battle.\n"
							+ "	By exploring the dungeon, you will encounter various unique encounters that you can interact with - some harmful, some not. "
							+ "You will also bump into a lot of enemies and that's where your gear and combat skill comes in."),
			new ManualPage(WeaponDatabase.HSWORD.getEmote() + " Combat",
					context -> "LRPG has a turn-based combat mechanic, meaning that you and your foe take turns. "
							+ "The duration of your turn depends on the speed of your and your foe's weapon - the slower your/foe's weapon is, "
							+ "the less turns you/foe gets. If, for example, "
							+ "you have a heavy weapon and you combat an enemy with a regular weapon (such as a rat), "
							+ "your enemy will get two turns for each turn you take - that's because you move twice as slow as your enemy does.\n"
							+ "You can also spend your turn for stuff other than attacking - "
							+ "you can use items from your inventory to help you in the battle or raise your guard. "
							+ "Raising your guard will grant you more resistance to your foe's attacks, "
							+ "but you can only raise your guard to a certain amount. Your guard will deplete over time if you don't raise it for a while."),
			new ManualPage(Assets.BONES_EMOTE + " Defeat",
					context -> "LRPG is a partially roguelike game, so defeat is inevitable. Everytime you die (or surrender) in a battle, "
							+ "you lose all your equipped gear and you're placed at the start of the dungeon. "
							+ "But don't worry, your items and gold are safe and will not be lost when you die."),
			new ManualPage(Assets.AMULET_EMOTE + " Credits",
					context -> "All pictures and resources were kindly provided by [ShatteredPixelDungeon made by 00-Evan](https://github.com/00-Evan/shattered-pixel-dungeon/)"
							+ " and [PixelDungeon made by watabou](https://github.com/watabou/pixel-dungeon) (because I suck at pixelart). "
							+ "Both are amazing opensource games you should definitely check out. "
							+ "Also thanks to FallenNation#6720 for converting the spritesheets from the projects above into the graphics interchange format, "
							+ "all of which can be found [here](https://github.com/FallenNationDev/spd-enemies-animated). "
							+ "The code of the game was written entirely by me (Marko Zajc#2119) and can be found [here](https://github.com/markozajc/LRPG)."),

	};

	private static final PreparedDialog<CommandContext> MANUAL_HOME = new PreparedEmbedDialog<>(context -> {
		Counter c = new Counter();
		return EmbedDialog.setAuthorUser(new EmbedBuilder(), context.getUser())
				.setColor(Constants.LITHIUM)
				.setTitle(Assets.MANUAL_EMOTE + " LRPG Manual")
				.setThumbnail(Assets.SCROLLBOOK_IMAGE)
				.appendDescription("Welcome to the **LRPG Manual**! Choose a page.")
				.addField("Pages", Arrays.asList(MANUAL).stream().map(page -> {
					c.count();
					return "**#" + c.getCount() + "** " + page.getName();
				}).collect(Collectors.joining("\n")), false)
				.setFooter("Type in [EXIT] to exit", null)
				.build();
	});
}
