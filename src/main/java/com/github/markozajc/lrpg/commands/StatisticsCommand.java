package com.github.markozajc.lrpg.commands;

import java.util.function.Predicate;

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
import net.dv8tion.jda.core.entities.User;

public class StatisticsCommand extends Command {

	private static final PreparedDialog<CommandContext> STATISTICS_DIALOG = new PreparedEmbedDialog<>(context -> {
		EmbedBuilder builder = EmbedDialog.setFooterUser(new EmbedBuilder(), context.getUser())
				.setColor(Constants.LITHIUM)
				.setThumbnail(context.getJDA().getSelfUser().getEffectiveAvatarUrl())
				.setTitle(context.getLithium().getConfiguration().getName() + " statistics")
				.setDescription(context.getLithium().getConfiguration().getName() + " currently resides in **"
						+ context.getJDA().getGuildCache().size() + "** servers, counting **"
						+ context.getJDA().getUserCache().stream().filter(Predicate.not(User::isBot)).count()
						+ "** unique users.\nThere");
		long sessions = LRpgCommand.getSessions(context).count();

		if (sessions == 1) {
			builder.appendDescription(" is currently **1** session");
		} else {
			builder.appendDescription(" are currently **" + sessions + "** sessions");
		}
		builder.appendDescription(" of LRPG running.");

		return builder.build();
	});

	@Override
	public void execute(CommandContext context, Parameters params) throws Throwable {
		STATISTICS_DIALOG.generate(context).display(context.getChannel());
	}

	@SuppressWarnings("null")
	@Override
	public CommandCategory getCategory() {
		return Categories.GENERAL;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public String getName() {
		return "Statistics";
	}

	@Override
	public String[] getAliases() {
		return Commands.toArray("stats", "sessions");
	}

}
