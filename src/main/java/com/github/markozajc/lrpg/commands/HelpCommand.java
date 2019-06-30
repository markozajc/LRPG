package com.github.markozajc.lrpg.commands;

import java.util.stream.Collectors;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.CommandCategory;
import com.github.markozajc.lithium.commands.utils.Commands;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;
import com.github.markozajc.lrpg.bot.Categories;

import net.dv8tion.jda.core.EmbedBuilder;

public class HelpCommand extends Command {

	private static final PreparedDialog<CommandContext> COMMANDS_DIALOG = new PreparedEmbedDialog<>(
			context -> EmbedDialog.setFooterUser(new EmbedBuilder(), context.getUser())
					.setColor(Constants.LITHIUM)
					.setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
					.setTitle(context.getLithium().getConfiguration().getName() + "'s command list")
					.setDescription(
						context.getLithium()
								.getCommands()
								.getRegisteredCommands()
								.stream()
								.map(c -> c.getName().toLowerCase())
								.collect(Collectors.joining(
									"\n" + context.getLithium().getConfiguration().getDefaultPrefix(),
									context.getLithium().getConfiguration().getDefaultPrefix(), "")))
					.build());

	@Override
	public void execute(CommandContext context, Parameters params) throws Throwable {
		COMMANDS_DIALOG.generate(context).display(context.getChannel());
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
		return "Help";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("commands");
	}

}
