package cc.playmc.anticommandtab;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
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

    private ProtocolManager protocolManager;

    private List<String> about = new ArrayList<>();
    private List<String> plugins = new ArrayList<>();
    private List<String> version = new ArrayList<>(); 
    private List<String> question = new ArrayList<>();

    private String pluginsDeny, versionDeny, aboutDeny, qmDeny;
    
    private boolean blockPlugins, blockVersion, blockAbout, blockQuestionMark;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

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

        FileConfiguration config = getConfig();
        blockPlugins = config.getBoolean("BlockPlugins");
        blockVersion = config.getBoolean("BlockVersion");
        blockAbout = config.getBoolean("BlockAbout");
        blockQuestionMark = config.getBoolean("BlockQuestionMark");

        pluginsDeny = colorize(config.getString("Plugins"));
        versionDeny = colorize(config.getString("Version"));
        aboutDeny = colorize(config.getString("About"));
        qmDeny = colorize(config.getString("QuestionMark"));

        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this,
                ListenerPriority.NORMAL, PacketType.Play.Client.TAB_COMPLETE) {
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
                    try {
                        if (event.getPlayer().hasPermission("lib.commandtab.bypass")) {
                            return;
                        }
  
                        PacketContainer packet = event.getPacket();
                        String message = (String) packet.getSpecificModifier(String.class).read(0).toLowerCase();
                        
                        /**
                         * A space is added to chat for every time you press tab
                         * when we check for 1 space, we check for /[TAB], and
                         * when we check for 2 spaces, we listen for /<CMD>
                         * [TAB]. This way we can effectively cancel /ver [TAB]
                         * as well.
                         */
                        if (message.startsWith("/") && !message.contains(" ")) {
                            event.setCancelled(true);
                        }
                    } catch (FieldAccessException e) {
                        getLogger().severe("Couldn't access field.");
                    }
                }       
            }
        });
    }
    
    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("lib.commandtab.bypass")) {
            return;
        }

        String name = player.getName();
        String[] msg = event.getMessage().split(" ");

        if (blockPlugins) {
            for (String loop : plugins) {
                if (msg[0].equalsIgnoreCase("/" + loop)) {
                    player.sendMessage(pluginsDeny.replace("%player", name));
                    event.setCancelled(true);
                }
            }
        }

        if (blockVersion) {
            for (String loop : version) {
                if (msg[0].equalsIgnoreCase("/" + loop)) {
                    player.sendMessage(versionDeny.replace("%player", name));
                    event.setCancelled(true);
                }
            }
        }

        if (blockAbout) {
            for (String loop : about) {
                if (msg[0].equalsIgnoreCase("/" + loop)) {
                    player.sendMessage(aboutDeny.replace("%player", name));
                    event.setCancelled(true);
                }
            }
        }

        if (blockQuestionMark) {
            for (String loop : question) {
                if (msg[0].equalsIgnoreCase("/" + loop)) {
                    player.sendMessage(qmDeny.replace("%player", name));
                    event.setCancelled(true);
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (cmd.getName().equalsIgnoreCase("act")) {
            if (sender.hasPermission("act.reload")) {
                sender.sendMessage(colorize("&4[&bAntiCommandTab&4] &cReloaded Configuration File"));
                reloadConfig();
            } else {
                sender.sendMessage(colorize("&4[&bAntiCommandTab&4] &cNo Permission"));
            }
        }
        return false;
    }
}