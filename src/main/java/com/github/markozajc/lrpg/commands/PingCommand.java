package com.github.markozajc.lrpg.commands;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.github.markozajc.lithium.Constants;
import com.github.markozajc.lithium.commands.Command;
import com.github.markozajc.lithium.commands.CommandCategory;
import com.github.markozajc.lithium.commands.utils.Parameters;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lithium.utilities.dialogs.message.EmbedDialog;
import com.github.markozajc.lithium.utilities.dialogs.message.prepared.PreparedEmbedDialog;
import com.github.markozajc.lrpg.bot.Categories;

public class PingCommand extends Command {

	private static final int PING_TIMEOUT = 5000;
	private static final InetSocketAddress ADDRESS = new InetSocketAddress("discordapp.com", 80);

	private static final PreparedEmbedDialog<Void> PING_MESSAGE = new PreparedEmbedDialog<>(v -> {
		try (Socket socket = new Socket()) {
			long start = System.currentTimeMillis();
			socket.connect(ADDRESS, PING_TIMEOUT);
			long end = System.currentTimeMillis();
			return EmbedDialog.generateEmbed("Pong! My current API ping is **" + (end - start) + "** ms.",
				Constants.GREEN);

		} catch (SocketTimeoutException e) {
			return EmbedDialog.generateEmbed("Discord didn't respond in time (ping > **." + PING_TIMEOUT + "** ms).",
				Constants.RED);

		} catch (IOException e) {
			return EmbedDialog.generateEmbed("Couldn't ping Discord's servers.", Constants.RED);
		}

	});

	@Override
	public void execute(CommandContext context, Parameters params) throws Exception {
		PING_MESSAGE.generate(null).display(context.getChannel());
	}

	@Override
	public String getInfo() {
		return "Responds with Pong! and bot's current API ping in milliseconds.";
	}

	@Override
	public String getName() {
		return "Ping";
	}

	@SuppressWarnings("null")
	@Override
	public CommandCategory getCategory() {
		return Categories.GENERAL;
	}
}
