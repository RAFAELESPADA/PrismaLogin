package com.br.gabrielsilva.prismamc.login.manager.gamer;

import java.util.UUID;
import org.bukkit.entity.Player;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Gamer {

	private Player player;
	private String nick;
	private UUID UUID;
	private boolean captchaConcluido, logado;
	private int tentativasFalhas;
	private Long timestamp;
	
	public Gamer(Player player, String nick) {
		this.player = player;
		this.nick = nick;
		this.UUID = player.getUniqueId();
		this.captchaConcluido = false;
		this.logado = false;
		this.tentativasFalhas = 0;
		this.timestamp = System.currentTimeMillis();
	}

	public void refresh() {
		this.captchaConcluido = false;
		this.logado = false;
		this.tentativasFalhas = 0;
	}
}