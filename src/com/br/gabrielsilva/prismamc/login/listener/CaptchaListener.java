package com.br.gabrielsilva.prismamc.login.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;


import com.br.gabrielsilva.prismamc.login.Login;

public class CaptchaListener implements Listener {
	
	 @EventHandler
	 public void onClick(InventoryClickEvent event) {
		 if ((event.getClickedInventory() != null) && (event.getCurrentItem() != null) && (event.getCurrentItem().hasItemMeta())) {
		      Player player = (Player) event.getWhoClicked();
		      if (event.getClickedInventory().getTitle().equalsIgnoreCase("Clique no olho do fim")) {
		    	  event.setCancelled(true);
		    	  
		    	  ItemStack item = event.getCurrentItem();
		    	  if (item.getType() != Material.EYE_OF_ENDER) {
		    		  player.kickPlayer("§cDetectamos que você errou em nosso teste CAPTCHA\n§cvocê foi removido por segurança!");
		    	  } else {
		    		  Login.getManager().getGamer(player).setCaptchaConcluido(true);
		    	  }
		      }
		 }
	 }
	 
	 @EventHandler
	 public void realMove(PlayerMoveEvent event) {
		 if (event.getFrom().getBlockY() != event.getTo().getBlockY()) {
			 return;
		 }
		 if (!Login.getManager().getGamer(event.getPlayer()).isLogado()) {
			 event.getPlayer().teleport(event.getFrom());
		 }
	 }
	 
	 @EventHandler
	 public void commandProcessEvent(PlayerCommandPreprocessEvent event) {
		 Player player = event.getPlayer();
		 if (Login.getManager().getGamer(player).isLogado()) {
		 	 return;
		 }
		 String msg = event.getMessage().toLowerCase();
		 if (msg.startsWith("/logar") || (msg.startsWith("/registrar") || (msg.startsWith("/login") || (msg.startsWith("/register"))))) {
			 event.setCancelled(false);
		 } else {
		    event.setCancelled(true);
		    player.sendMessage("§cPara usar algum comando você precisa estar logado!");
		 }
	 }
}