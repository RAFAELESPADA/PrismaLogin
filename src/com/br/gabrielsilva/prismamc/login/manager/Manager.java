package com.br.gabrielsilva.prismamc.login.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.login.Login;
import com.br.gabrielsilva.prismamc.login.manager.captcha.CaptchaManager;
import com.br.gabrielsilva.prismamc.login.manager.gamer.Gamer;

import lombok.Getter;

@Getter
public class Manager {

	private HashMap<UUID, Gamer> gamers;
	private CaptchaManager captchaManager;
	
	public void init() {
		this.gamers = new HashMap<>();
		
		this.captchaManager = new CaptchaManager();
		this.captchaManager.init();
	}
	
	public Gamer getGamer(Player player) {
		return gamers.get(player.getUniqueId());
	}
	
	public void addGamer(Player player) {
		if (this.gamers.containsKey(player.getUniqueId())) {
			this.gamers.get(player.getUniqueId()).setPlayer(player);
			return;
		}
		this.gamers.put(player.getUniqueId(), new Gamer(player, player.getName()));
	}

	public void removeGamers() {
		List<UUID> toRemove = new ArrayList<>();
		for (Gamer gamers : Login.getManager().getGamers().values()) {
			 if (System.currentTimeMillis() > gamers.getTimestamp() + TimeUnit.MINUTES.toMillis(10)) {
				 if (!gamers.getPlayer().isOnline()) {
					 toRemove.add(gamers.getUUID());
				 }
			 }
		}
		
		if (toRemove.size() != 0) {
			Login.console("Removendo Gamers...");
			
			int naoRegistrados = 0;
			
			for (UUID uuid : toRemove) {
				 String nick = getGamers().get(uuid).getNick();
				 getGamers().remove(uuid);
				 
				 if (MySQLManager.contains("premium_map", "nick", nick)) {
					 if (!MySQLManager.contains("accounts", "nick", nick)) {
						 naoRegistrados++;
						 Login.runAsync(() -> {
							 MySQLManager.executeUpdateAsync("DELETE FROM premium_map WHERE nick='" + nick + "';");
						 });
					 }
				 }
			}
			
			Login.console("Removido " + toRemove.size() + " gamers offline.");
			
			if (naoRegistrados != 0) {
				Login.console("Foi removido " + naoRegistrados + " premium maps de jogadores nao registrados.");
			}
		}
		toRemove.clear();
		toRemove = null;
	}
}