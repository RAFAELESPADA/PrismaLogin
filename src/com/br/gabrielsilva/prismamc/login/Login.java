package com.br.gabrielsilva.prismamc.login;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandFramework;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.PluginConfig;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.CommandLoader;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.ListenerLoader;
import com.br.gabrielsilva.prismamc.login.listener.GeneralListeners;
import com.br.gabrielsilva.prismamc.login.manager.Manager;

import lombok.Getter;
import lombok.Setter;

public class Login extends JavaPlugin {

	@Getter @Setter
	private static Login instance;
	
	@Getter @Setter
	public static Location spawn;
	
	@Getter @Setter
	private static Manager manager;
	
	public void onEnable() {
		setInstance(this);
		
		setManager(new Manager());
		getManager().init();
		
		PluginConfig.createLoc(getInstance(), "spawn");
		setSpawn(PluginConfig.getNewLoc(getInstance(), "spawn"));
		
		GeneralListeners.createInventory();
		
		ListenerLoader.loadListenersBukkit(getInstance(), "com.br.gabrielsilva.prismamc.login.listener");

		new CommandLoader(new BukkitCommandFramework(getInstance())).
		loadCommandsFromPackage("com.br.gabrielsilva.prismamc.login.commands");
	}
	
	public void onDisable() {
		getManager().removeGamers();
	}
	
	public static void console(String msg) {
		Bukkit.getConsoleSender().sendMessage("[Login] " + msg);
	}
	
	public static void runAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(getInstance(), runnable);	
	}
	
	public static void runLater(Runnable runnable) {
		Bukkit.getScheduler().runTaskLater(getInstance(), runnable, 5L);	
	}
	
	public static void runLater(Runnable runnable, long ticks) {
		Bukkit.getScheduler().runTaskLater(getInstance(), runnable, ticks);	
	}

	public static Manager getManager() {
		return getManager();
	}
	public static Login getInstance() {
		return getInstance();
	}
}