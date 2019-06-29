package com.github.markozajc.lrpg.bot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.Lithium.BotConfiguration;
import com.github.markozajc.lithium.Lithium.Handlers;
import com.github.markozajc.lithium.Lithium.PersistentDataConfiguration;
import com.github.markozajc.lithium.data.source.impl.FileDataSource;
import com.github.markozajc.lithium.handlers.CommandHandler;
import com.github.markozajc.lithium.handlers.ExceptionHandler;
import com.github.markozajc.lithium.processes.ProcessManager;
import com.github.markozajc.lrpg.commands.LRpgCommand;
import com.github.markozajc.lrpg.commands.ManualCommand;
import com.github.markozajc.lrpg.provider.LRpgProvider;

import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;

public class Launcher {

	@SuppressWarnings({
			"unused", "null"
	})
	public static void main(String[] args) throws IOException, LoginException {
		if (args.length == 0)
			throw new IllegalArgumentException(
					"Incorrect number of arguments. The first argument must be the path to the configuration file");

		try (InputStream is = new FileInputStream(new File(args[0]))) {
			Properties props = new Properties();
			props.load(is);

			BotConfiguration config = new BotConfiguration(Long.parseLong(props.getProperty("owner")),
					props.getProperty("prefix"), Arrays.asList(new LRpgCommand(), new ManualCommand()),
					props.getProperty("name"));
			JDABuilder builder = new JDABuilder(props.getProperty("token"));
			PersistentDataConfiguration data = new PersistentDataConfiguration(
					new FileDataSource(new File(props.getProperty("data"))), Arrays.asList(new LRpgProvider()));

			new Lithium(config, data, new Handlers(new CommandHandler(), new ExceptionHandler()), builder,
					Arrays.asList((e, l) -> e.getJDA()
							.getPresence()
							.setPresence(OnlineStatus.ONLINE,
								Game.of(GameType.DEFAULT,
									l.getConfiguration().getDefaultPrefix() + "lrpg | Adventure awaits!"))),
					Collections.emptyList(),
					new ProcessManager(Executors.newFixedThreadPool(Integer.parseInt(props.getProperty("poolsize")))));
		}
	}

}
