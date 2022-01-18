package com.br.gabrielsilva.prismamc.login.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.commons.bukkit.utils.BungeeUtils;
import com.br.gabrielsilva.prismamc.login.Login;
import com.br.gabrielsilva.prismamc.login.commands.ServerCommand;

public class GeneralListeners implements Listener {

	private static Inventory inventory;
	private int minutos = 0;
	
	public static void createInventory() {
		inventory = Bukkit.getServer().createInventory(null, 9, "Clique para se conectar");
		
		inventory.setItem(0, new ItemBuilder().material(Material.DIAMOND).name("§aLobby").build());
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.MINUTO) {
			return;
		}
		
		if (minutos == 10) {
			Login.getManager().removeGamers();
			minutos=0;
			return;
		}
		minutos++;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		Login.getManager().addGamer(player);
		
		player.teleport(Login.getSpawn());
		
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.updateInventory();
		
		if (!player.getGameMode().equals(GameMode.ADVENTURE)) {
		    player.setGameMode(GameMode.ADVENTURE);
		}
		
		BukkitMain.runLater(() -> {
			Login.getManager().getCaptchaManager().createCaptcha(player);
		}, 30L);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
	
		Login.getManager().getGamers().get(uuid).refresh();
		
		if (ServerCommand.autorizados.contains(uuid)) {
			ServerCommand.autorizados.remove(uuid);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.PHYSICAL) {
			if (event.getPlayer().getItemInHand().getType().equals(Material.COMPASS)) {
				event.getPlayer().openInventory(inventory);
			}
		}
	}
	
	@EventHandler
	public void onInventory(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			if (event.getInventory().getTitle().equals(inventory.getTitle())) {
				if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
					event.setCancelled(true);
					Player player = (Player)event.getWhoClicked();
					player.closeInventory();
					player.sendMessage("§aConectando...");
					BungeeUtils.redirecionar(player, "Lobby");
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		if (event.getMessage().startsWith("/")) {
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void spread(BlockSpreadEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onFood(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onChuva(WeatherChangeEvent event) {
		if (event.toWeatherState()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		event.setCancelled(true);
		if (event.getCause().equals(DamageCause.VOID)) {
			event.getEntity().teleport(Login.getSpawn());
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void ignite(BlockIgniteEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void spawn(ItemSpawnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		if ((event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (ServerCommand.autorizados.contains(event.getPlayer().getUniqueId()))) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if ((event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (ServerCommand.autorizados.contains(event.getPlayer().getUniqueId()))) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void Interact(PlayerInteractEvent event) {
		if ((event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && (ServerCommand.autorizados.contains(event.getPlayer().getUniqueId()))) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
	}
}