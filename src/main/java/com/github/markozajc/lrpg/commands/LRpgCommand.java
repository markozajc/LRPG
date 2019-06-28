package com.github.markozajc.lrpg.commands;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.CommandCategory;
import com.github.markozajc.lithium.commands.exceptions.CommandException;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.LithiumProcess;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.EventWaiter.Waiter;
import com.github.markozajc.lrpg.bot.Categories;
import com.github.markozajc.lrpg.game.LRpgExposed;

import net.dv8tion.jda.core.entities.TextChannel;

public class LRpgCommand extends Command {

	@Override
	public void execute(CommandContext context, Parameters params) throws Throwable {

		context.getLithium().getEventWaiter().cleanWaiters();
		TextChannel channel = Stream
				.concat(
					context.getLithium()
							.getEventWaiter()
							.getWaiters()
							.values()
							.stream()
							.flatMap(Set::stream)
							.map(Waiter::getParentContext),
					context.getLithium().getProcessManager().getProcesses().stream().map(LithiumProcess::getContext))
				.filter(c -> c instanceof CommandContext)
				.map(c -> (CommandContext) c)
				.filter(c -> c.getUser().getIdLong() == context.getUser().getIdLong())
				.filter(c -> c.getCommand() instanceof LRpgCommand)
				.filter(c -> !Objects.equals(c, context))
				.map(CommandContext::getChannel)
				.findFirst()
				.orElse(null);

		if (channel == null) {
			LRpgExposed.startGame(context);

		} else {
			throw new CommandException("You already have a game of LRPG running in " + channel.getAsMention()
					+ ". Please exit out of before opening a new session.", Constants.NONE, false);
		}
	}

	@SuppressWarnings("null")
	@Override
	public CommandCategory getCategory() {
		return Categories.LRPG;
	}

	@Override
	public String getInfo() {
		return "";
	}

	@Override
	public String getName() {
		return "LRPG";
	}

}