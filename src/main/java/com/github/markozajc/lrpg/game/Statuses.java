package com.github.markozajc.lrpg.game;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.Lithium;
import com.github.markozajc.lithium.processes.context.CommandContext;
import com.github.markozajc.lrpg.game.Enemies.Enemy;
import com.github.markozajc.lrpg.game.Player.PlayerDungeon;
import com.github.markozajc.lrpg.game.Player.PlayerDungeon.PlayerFight;
import com.github.markozajc.lrpg.game.Player.PlayerFighter;
import com.github.markozajc.lrpg.provider.LRpgProvider;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Statuses {

	private Statuses() {}

	public static class GameInfo {

		@Nonnull
		private final CommandContext context;
		@Nonnull
		private final Player player;
		private final boolean firstTimeLaunch;

		public GameInfo(@Nonnull CommandContext context) {
			this.context = context;

			Player newPlayer = getLithium().getProviderManager()
					.getProvider(LRpgProvider.class)
					.getPlayer(getEvent().getAuthor());
			if (newPlayer == null) {
				this.firstTimeLaunch = true;
				newPlayer = getLithium().getProviderManager()
						.getProvider(LRpgProvider.class)
						.createPlayer(getEvent().getAuthor());

			} else {
				this.firstTimeLaunch = false;
			}

			this.player = newPlayer;
		}

		public GameInfo(@Nonnull GameInfo game) {
			this.context = game.context;
			this.player = game.player;
			this.firstTimeLaunch = game.firstTimeLaunch;
		}

		public boolean isFirstTimeLaunch() {
			return this.firstTimeLaunch;
		}

		@Nonnull
		public Lithium getLithium() {
			return this.context.getLithium();
		}

		@Nonnull
		public GuildMessageReceivedEvent getEvent() {
			return this.context.getEvent();
		}

		@Nonnull
		public Player getPlayer() {
			return this.player;
		}

		@SuppressWarnings("null")
		@Nonnull
		public TextChannel getChannel() {
			return this.getEvent().getChannel();
		}

		@SuppressWarnings("null")
		@Nonnull
		public User getAuthor() {
			return this.getEvent().getAuthor();
		}

		public CommandContext getContext() {
			return this.context;
		}

	}

	public static class DungeonInfo extends GameInfo {

		public DungeonInfo(@Nonnull GameInfo game) {
			super(game);
			this.getPlayer().createPlayerDungeon();
		}

		public DungeonInfo(@Nonnull DungeonInfo dungeon) {
			super(dungeon);
			this.getPlayer().createPlayerDungeon();
		}

		public DungeonInfo(@Nonnull FightInfo fight) {
			super(fight);
			this.getPlayer().createPlayerDungeon();
		}

		public PlayerDungeon getPlayerDungeon() {
			return this.getPlayer().getPlayerDungeon();
		}

	}

	public static class FightInfo extends DungeonInfo {

		@Nonnull
		private final PlayerFighter playerFighter;

		public FightInfo(@Nonnull DungeonInfo dungeon, @Nonnull Enemy enemy) {
			super(dungeon);
			this.getPlayerDungeon().createPlayerFight(enemy);
			this.playerFighter = new PlayerFighter(getPlayer(), dungeon);
		}

		public FightInfo(@Nonnull DungeonInfo dungeon) {
			super(dungeon);
			this.playerFighter = new PlayerFighter(getPlayer(), dungeon);
		}

		@Nonnull
		public PlayerFighter getPlayerFighter() {
			return this.playerFighter;
		}

		public PlayerFight getPlayerFight() {
			return this.getPlayer().getPlayerDungeon().getPlayerFight();
		}

	}

}
