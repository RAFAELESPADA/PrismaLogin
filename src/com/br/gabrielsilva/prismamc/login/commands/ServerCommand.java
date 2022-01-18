package com.br.gabrielsilva.prismamc.login.commands;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.PlayerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.title.TitleAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.PluginConfig;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;
import com.br.gabrielsilva.prismamc.login.Login;
import com.br.gabrielsilva.prismamc.login.manager.gamer.Gamer;

public class ServerCommand implements CommandClass {

	public static ArrayList<UUID> autorizados = new ArrayList<>();
	
	@Command(name = "login", aliases= {"logar"})
	public void login(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		if (args.length != 1) {
			commandSender.sendMessage("§cUse: /logar <Senha>");
			return;
		}
		Player player = commandSender.getPlayer();
		if (!BukkitMain.getManager().getDataManager().hasBukkitPlayer(player.getUniqueId())) {
			player.kickPlayer("§cOcorreu um erro, tente novamente.");
			return;
		}
		
		Gamer gamer = Login.getManager().getGamer(player);
		
		if (!gamer.isCaptchaConcluido()) {
			player.sendMessage("§cVocê nao completou o captcha.");
			return;
		}
		
		DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
		final String senhaAccount = dataHandler.getString(DataType.REGISTRO_SENHA);
		
		boolean registrado = false;
		if (senhaAccount == null) {
			registrado = false;
		} else {
			if (senhaAccount.equalsIgnoreCase("") || (senhaAccount.isEmpty()) || (senhaAccount.equalsIgnoreCase(" "))) {
				registrado = false;
			} else {
				registrado = true;
			}
		}
		
		if (!registrado) {
			player.sendMessage("§aVocê nao possue um registro!");
			return;
		}
		if (gamer.isLogado()) {
			player.sendMessage("§aVocê ja esta logado.");
			return;
		}
		
		String senha = args[0];
		if (!format(senha).equalsIgnoreCase(senhaAccount)) {
			gamer.setTentativasFalhas(gamer.getTentativasFalhas() + 1);
			
			if (gamer.getTentativasFalhas() == 3) {
				player.kickPlayer("§cVocê errou a senha 3 vezes.");
			} else {
				player.sendMessage("§cSenha incorreta!");
			}
			return;
		}
		
		TitleAPI.enviarTitulos(player, "§aAutenticado", "§fcom sucesso!", 1, 4, 6);
		
		player.sendMessage("§aAutenticado com sucesso!");
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
		
		gamer.setLogado(true);
		aplicarUltimoLogin(player);
	}
	
	@Command(name = "register", aliases= {"registrar"})
	public void register(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
	
		if (args.length != 2) {
			commandSender.sendMessage("§cUse: /registrar <Senha> <Senha>");
			return;
		}
		Player player = commandSender.getPlayer();
		if (!BukkitMain.getManager().getDataManager().hasBukkitPlayer(player.getUniqueId())) {
			player.kickPlayer("§cOcorreu um erro, tente novamente.");
			return;
		}
		
		Gamer gamer = Login.getManager().getGamer(player);
		
		if (!gamer.isCaptchaConcluido()) {
			player.sendMessage("§cVocê não completou o captcha.");
			return;
		}
		DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
		
		final String senhaAccount = dataHandler.getString(DataType.REGISTRO_SENHA);
		boolean registrado = false;
		if (senhaAccount == null) {
			registrado = false;
		} else {
			if (senhaAccount.equalsIgnoreCase("") || (senhaAccount.isEmpty()) || (senhaAccount.equalsIgnoreCase(" "))) {
				registrado = false;
			} else {
				registrado = true;
			}
		}
		
		if (registrado) {
			player.sendMessage("§cVocê ja possue um registro!");
			return;
		}
		if (gamer.isLogado()) {
			player.sendMessage("§cVocê já está registrado.");
			return;
		}
		String senha = args[0], senha1 = args[1];
		if (senha.length() < 4) {
			player.sendMessage("§cSenha muito pequena.");
			return;
		}
		if (senha.length() > 20) {
			player.sendMessage("§cSenha muito grande.");
			return;
		}
		if (!senha.equals(senha1)) {
			player.sendMessage("§cSenhas nao sao iguais.");
			return;
		}
		String data = DateUtils.getCalendario().replace("] ", "").replace("[", "");
		dataHandler.getData(DataType.REGISTRO_SENHA).setValue(format(senha));
		dataHandler.getData(DataType.REGISTRO_DATA).setValue(data);
		
		TitleAPI.enviarTitulos(player, "§aRegistrado", "§fcom sucesso!", 1, 4, 6);
		
		player.sendMessage("§aRegistrado com sucesso!");
		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
		
		gamer.setLogado(true);
		registrar(player, format(senha), data);
	}
	
	@Command(name = "setloc", groupsToUse= {Groups.DONO})
	public void setloc(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		if (args.length == 0) {
			commandSender.sendMessage("§cUse: /setloc spawn");
			return;
		}
		Player player = commandSender.getPlayer();
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("spawn")) {
				PluginConfig.putNewLoc(Login.getInstance(), "spawn", player);
				Login.setSpawn(PluginConfig.getNewLoc(Login.getInstance(), "spawn"));
				commandSender.sendMessage("§aSpawn setado!");
			} else {
				commandSender.sendMessage("§cUse: /setloc spawn");
			}
		} else {
			commandSender.sendMessage("§cUse: /setloc spawn");
		}
	}
	
	@Command(name = "build", aliases= {"b"}, groupsToUse= {Groups.ADMIN})
	public void build(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		if (autorizados.contains(player.getUniqueId())) {
			autorizados.remove(player.getUniqueId());
			player.sendMessage("§aVocê desativou o modo construção.");
			player.setGameMode(GameMode.ADVENTURE);
		} else {
			autorizados.add(player.getUniqueId());
			player.sendMessage("§aVocê ativou o modo construção.");
			player.setGameMode(GameMode.CREATIVE);
		}
	}
	
	private void registrar(Player player, String senha, String dataRegistro) {
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		
		if (!bukkitPlayer.getDataHandler().isCategoryLoaded(DataCategory.PRISMA_PLAYER)) {
			bukkitPlayer.getDataHandler().load(DataCategory.PRISMA_PLAYER);
		}
		
		final String address = PlayerAPI.getAddress(player);
		
		BukkitClient.sendPacket(player, new PacketBungeeUpdateField(bukkitPlayer.getNick(), "ProxyPlayer", "AdicionarSessao", address));
		
		Login.runAsync(() -> {
			long started = 0;
			
			if (ServerOptions.isDebug()) {
				Login.console("Criando nova conta... " + player.getName() + " (MySQL)");
				started = System.currentTimeMillis();
			}
			
			bukkitPlayer.getDataHandler().getData(DataType.LAST_IP).setValue(address);
			bukkitPlayer.getDataHandler().saveCategory(DataCategory.PRISMA_PLAYER);
			
			try {
				PreparedStatement insert = Core.getMySQL().getConexão().prepareStatement(
				"INSERT INTO registros(nick, senha, registrado) VALUES (?, ?, ?)");
				
				insert.setString(1, player.getName());
				insert.setString(2, senha);
				insert.setString(3, dataRegistro);
				
				insert.executeUpdate();
				insert.close();
				
				if (player.isOnline()) {
					player.getInventory().setItem(0, 
							new ItemBuilder().material(Material.COMPASS).name("§aClique para conectar-se ao Lobby").glow().build());
				}
				
				MySQLManager.atualizarStatus("accounts", "last_ip", player.getName(), address);
				
				if (ServerOptions.isDebug()) {
					Login.console("Conta " + player.getName() +
							" criada em -> " + DateUtils.getElapsed(started));
				}
			} catch (SQLException ex) {
				kickPlayer(player, "§cOcorreu um erro ao atualizar suas informações.");
				Login.console("Ocorreu um erro ao tentar registrar um jogador -> " + ex.getLocalizedMessage());
			}
		});
	}
	
	public static void kickPlayer(Player player, String mensagem) {
		BukkitMain.runLater(() -> {player.kickPlayer(mensagem);});
	}
	
	public void aplicarUltimoLogin(Player player) {
		final String IP = PlayerAPI.getAddress(player);
		
		BukkitClient.sendPacket(player, new PacketBungeeUpdateField(player.getName(), "ProxyPlayer", "AdicionarSessao", IP));
		
		Login.runAsync(() -> {
			MySQLManager.atualizarStatus("accounts", "last_ip", player.getName(), IP);
			
			player.getInventory().setItem(0, 
					new ItemBuilder().material(Material.COMPASS).name("§aClique para conectar-se ao Lobby").glow().build());
		});
	}
	
	public static String format(String string) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(string.getBytes());
		    StringBuffer sb = new StringBuffer();
		    for (int i = 0; i < array.length; i++) {
		         sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
		    }
		    return sb.toString();
		} catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {}
		return null;
	}
}