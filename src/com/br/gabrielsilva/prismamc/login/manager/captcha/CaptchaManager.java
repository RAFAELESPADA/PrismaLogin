package com.br.gabrielsilva.prismamc.login.manager.captcha;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.actionbar.ActionBar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.title.TitleAPI;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.login.Login;
import com.br.gabrielsilva.prismamc.login.commands.ServerCommand;

public class CaptchaManager {

	private ItemStack enderPearl, olhoDoFim;
	
	public void init() {
		enderPearl = new ItemBuilder().material(Material.ENDER_PEARL).name("§7-").build();
		olhoDoFim = new ItemBuilder().material(Material.EYE_OF_ENDER).name("§aClique aqui").build();
	}
	
	public void createCaptcha(Player player) {
		if (!player.isOnline()) {
			return;
		}
		Inventory inventory = player.getServer().createInventory(null, 3 * 9, "Clique no olho do Fim");
		
		int randomSlot = new Random().nextInt(26);
		inventory.setItem(randomSlot, olhoDoFim);
		
		for (int i = 0; i < 27; i++) {
			 if (i == randomSlot) {
				 continue;
			 }
			 inventory.setItem(i, enderPearl);
		}
		
		player.openInventory(inventory);
		startTaskCaptcha(player, inventory);
	}
	
	private void startTaskKick(Player player) {
		new BukkitRunnable() {
			int segundos = 0;
			public void run() {
				if (!player.isOnline()) {
					cancel();
					return;
				}
				if (segundos == 25) {
					ServerCommand.kickPlayer(player, "§cVocê demorou muito para entrar no Lobby.");
					return;
				}
				segundos++;
			}
		}.runTaskTimer(Login.getInstance(), 20L, 20L);
	}
	
	private void startTaskLogin(Player player) {
		final DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
		
		final String senha = dataHandler.getString(DataType.REGISTRO_SENHA);
		boolean registrado = false;
		if (senha == null) {
			registrado = false;
		} else {
			if (senha.equalsIgnoreCase("") || (senha.isEmpty()) || (senha.equalsIgnoreCase(" "))) {
				registrado = false;
			} else {
				registrado = true;
			}
		}
		
		final boolean registradoFinal = registrado;
		new BukkitRunnable() {
		int segundos = 0;
		public void run() {
			if (!player.isOnline()) {
				cancel();
				return;
			}
			if (segundos == 20) {
				player.kickPlayer("§cVocê demorou muito para " + (registradoFinal == true ? "logar" : "se registrar"));
				cancel();
				return;
			}
			if (Login.getManager().getGamers().get(player.getUniqueId()).isLogado()) {
				cancel();
				startTaskKick(player);
				return;
			}
    		if (segundos % 2 == 0) {
		    	player.sendMessage("§aUtilize o comando: §f" + 
    		               (registradoFinal == true ? "/logar <Senha> §fpara §a§lLOGAR!" : "/registrar <Senha> <Senha> §fpara §a§lREGISTRAR!"));
    		}
    		if (segundos % 5 == 0) {
    			if (registradoFinal) {
					TitleAPI.enviarTitulos(player, "§aUse /logar <Sua Senha>", "§fpara fazer o login", 2, 4, 6);
    			} else {
    				TitleAPI.enviarTitulos(player, "§aUse /registrar <Senha> <Senha>", "§fpara se registrar", 2, 4, 6);
    			}
    		}
			segundos++;
		}
		}.runTaskTimer(Login.getInstance(), 20L, 20L);
	}

	private void startTaskCaptcha(Player player, Inventory inventory) {
		new BukkitRunnable() {
			int tickets = 0, segundos = 0;
			public void run() {
				if (!player.isOnline()) {
					cancel();
					return;
				}
				tickets++;
				if (Login.getManager().getGamers().get(player.getUniqueId()).isCaptchaConcluido()) {
					cancel();
					player.closeInventory();
					player.sendMessage("§6§lCAPTCHA §fConcluído com sucesso!");
					ActionBar.sendActionBar(player, "§6§lCAPTCHA §fconcluído com sucesso");
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
					
					TitleAPI.enviarTitulos(player, "§6§lCAPTCHA", "§fConcluído com sucesso", 1, 2, 3);
					
					startTaskLogin(player);
					return;
				}
				if (segundos == 7) {
					player.kickPlayer("§cVocê não concluiu o captcha.");
					cancel();
					return;
				}
				if (tickets == 4) {
					segundos++;
					tickets = 0;
				}
				if (inventory.getViewers().size() == 0) {
					player.openInventory(inventory);
				}
			}
		}.runTaskTimer(Login.getInstance(), 5L, 5L);
	}
}