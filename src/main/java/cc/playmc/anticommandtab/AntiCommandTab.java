package cc.playmc.anticommandtab;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class AntiCommandTab extends JavaPlugin implements Listener {

	ProtocolManager protocolManager;

	FileConfiguration config;

	List<String> plugins = new ArrayList<>();
	List<String> version = new ArrayList<>();
	List<String> about = new ArrayList<>();
	List<String> question = new ArrayList<>();

	boolean blockPlugins, blockVersion, blockAbout, blockQuestionMark;

	String pluginsDeny, versionDeny, aboutDeny, qmDeny;

	public void onEnable() {
		config = getConfig();

		saveDefaultConfig();

		plugins.add("pl");
		plugins.add("bukkit:pl");
		plugins.add("plugins");
		plugins.add("bukkit:plugins");
		version.add("ver");
		plugins.add("bukkit:ver");
		version.add("version");
		plugins.add("bukkit:version");
		about.add("about");
		plugins.add("bukkit:about");
		question.add("?");
		plugins.add("bukkit:?");

		blockPlugins = config.getBoolean("BlockPlugins");
		blockVersion = config.getBoolean("BlockVersion");
		blockAbout = config.getBoolean("BlockAbout");
		blockQuestionMark = config.getBoolean("BlockQuestionMark");

		pluginsDeny = config.getString("Plugins").replaceAll("&", "§");
		versionDeny = config.getString("Version").replaceAll("&", "§");
		aboutDeny = config.getString("About").replaceAll("&", "§");
		qmDeny = config.getString("QuestionMark").replaceAll("&", "§");

		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		this.protocolManager = ProtocolLibrary.getProtocolManager();
		this.protocolManager.addPacketListener(new PacketAdapter(this,
				ListenerPriority.NORMAL, PacketType.Play.Client.TAB_COMPLETE) {
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE)
					try {
						if (event.getPlayer().hasPermission(
								"lib.commandtab.bypass"))
							return;
						PacketContainer packet = event.getPacket();
						String message = (String) packet
								.getSpecificModifier(String.class).read(0)
								.toLowerCase();
						/**
						 * A space is added to chat for every time you press tab
						 * when we check for 1 space, we check for /[TAB], and
						 * when we check for 2 spaces, we listen for /<CMD>
						 * [TAB]. This way we can effectively cancel /ver [TAB]
						 * as well.
						 */

						if ((message.startsWith("/") && !message.contains(" "))
								|| (message.startsWith("/" + plugins) && !message.contains(" "))
								|| (message.startsWith("/" + version) && !message.contains(" "))
								|| (message.startsWith("/" + about) && !message.contains(" "))
								|| (message.startsWith("/" + question) && !message.contains(" "))){
							event.setCancelled(true);
						}
					} catch (FieldAccessException e) {
						getLogger().severe("Couldn't access field.");
					}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {

		Player player = event.getPlayer();

		String[] msg = event.getMessage().split(" ");

		if (!player.hasPermission("lib.commandtab.bypass")) {

			if (blockPlugins) {
				for (String Loop : plugins) {
					if (msg[0].equalsIgnoreCase("/" + Loop)) {
						player.sendMessage(pluginsDeny.replaceAll("%player",
								player.getName()));
						event.setCancelled(true);
					}
				}
			}

			if (blockVersion) {
				for (String Loop : version) {
					if (msg[0].equalsIgnoreCase("/" + Loop)) {
						player.sendMessage(versionDeny.replaceAll("%player",
								player.getName()));
						event.setCancelled(true);
					}
				}
			}

			if (blockAbout) {
				for (String Loop : about) {
					if (msg[0].equalsIgnoreCase("/" + Loop)) {
						player.sendMessage(aboutDeny.replaceAll("%player",
								player.getName()));
						event.setCancelled(true);
					}
				}
			}

			if (blockQuestionMark) {
				for (String Loop : question) {
					if (msg[0].equalsIgnoreCase("/" + Loop)) {
						player.sendMessage(qmDeny.replaceAll("%player",
								player.getName()));
						event.setCancelled(true);
					}
				}
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("act")) {
			if (sender.hasPermission("act.reload")) {
				sender.sendMessage("§4[§bAntiCommandTab§4] §cReloaded Configuration File");
				reloadConfig();
			} else {
				sender.sendMessage("§4[§bAntiCommandTab§4] §cNo Permission");
			}
		}
		return false;
	}
}
