package com.github.markozajc.lrpg.provider;

import java.util.Map;
import java.util.function.LongPredicate;

import javax.annotation.Nonnull;

import com.github.markozajc.lithium.data.providers.SnowflakeProvider;
import com.github.markozajc.lrpg.game.LRpgExposed;
import com.github.markozajc.lrpg.game.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.dv8tion.jda.core.entities.User;

public class LRpgProvider extends SnowflakeProvider<Player> {

	private static final TypeToken<Map<Long, Player>> TYPE_TOKEN = new TypeToken<>() {};
	private static final LongPredicate FILTER = id -> false;
	private static final Gson GSON = LRpgExposed.registerGsonTypeAdapters(new GsonBuilder()).create();

	@Nonnull
	public Player createPlayer(User user) {
		Player player = LRpgExposed.getStarterPlayer();
		this.data.put(user.getIdLong(), player);
		return player;
	}

	public Player getPlayer(User user) {
		return this.data.get(user.getIdLong());
	}

	@Override
	public String getDataKey() {
		return "lrpg";
	}

	@Override
	public TypeToken<Map<Long, Player>> getTypeToken() {
		return TYPE_TOKEN;
	}

	@Override
	protected LongPredicate getSnowflakeObsoleteFilter() {
		return FILTER;
	}

	@Override
	public Gson getGson() {
		return GSON;
	}

}
