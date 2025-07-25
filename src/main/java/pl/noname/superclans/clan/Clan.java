package pl.noname.superclans.clan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.Player;
import pl.noname.superclans.GenerateTabList;
import pl.noname.superclans.SuperClans;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Clan implements CommandExecutor, TabCompleter {

    private final SuperClans plugin;
    private GenerateTabList generateTabList;

    public File clanFile;
    public FileConfiguration clanConfig;

    public Clan(SuperClans plugin) {
        this.plugin = plugin;
    }

    public void setGenerateTabList(GenerateTabList generateTabList) {
        this.generateTabList = generateTabList;
    }

    public void setup() throws IOException {
        clanFile = new File(plugin.getDataFolder(), "clans.yml");
        if (!clanFile.exists()) {
            clanFile.createNewFile();
        }
        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
        reload();
        loadTeamsFromFile();
    }

    public void createClan(String name, ChatColor color) {
        String key = "tab_" + name.toLowerCase();
        if (clanConfig.contains(key)) return;

        int id = assignClanId();
        if (id == -1) {
            plugin.getLogger().warning("Maksymalna liczba klanów (4) została osiągnięta. Nie można dodać klanu: " + name);
            return;
        }

        String displayName = name.toUpperCase();

        clanConfig.set(key + ".points", 0);
        clanConfig.set(key + ".color", color.name());
        clanConfig.set(key + ".displayName", displayName);
        clanConfig.set(key + ".id", id);
        save();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(key);
        if (team == null) {
            team = scoreboard.registerNewTeam(key);
        }

        team.setPrefix(color.toString());
        team.setDisplayName(displayName);

        refreshAllTablists();
    }

    private int assignClanId() {
        Set<String> keys = clanConfig.getKeys(false);
        List<Integer> takenIds = new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith("tab_")) {
                takenIds.add(clanConfig.getInt(key + ".id", 0));
            }
        }

        int[] order = {1, 3, 2, 4};
        for (int id : order) {
            if (!takenIds.contains(id)) {
                return id;
            }
        }
        return -1;
    }

    public void save() {
        try {
            clanConfig.save(clanFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        clanConfig = YamlConfiguration.loadConfiguration(clanFile);
    }

    public FileConfiguration get() {
        return clanConfig;
    }

    public int getPoints(String clanName) {
        return get().getInt("tab_" + clanName.toLowerCase() + ".points");
    }

    public void setPoints(String clanName, int amount) {
        get().set("tab_" + clanName.toLowerCase() + ".points", amount);
        save();
    }

    public void addPoints(String clanName, int amount) {
        setPoints(clanName, getPoints(clanName) + amount);
    }

    public void removePoints(String clanName, int amount) {
        setPoints(clanName, getPoints(clanName) - amount);
    }

    private void refreshAllTablists() {
        if (generateTabList == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            generateTabList.gen(player, true, "", "", null);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("createclan")) return false;
        String name = args[0].toLowerCase();
        String colorName = args[1].toUpperCase();
        ChatColor color;
        try {
            color = ChatColor.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Niepoprawny kolor!");
            return true;
        }
        createClan(name, color);
        sender.sendMessage(ChatColor.GREEN + "Klan " + name.toUpperCase() + " został utworzony w kolorze " + color + color.name());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 2) {
            List<String> kolory = Arrays.asList(
                    "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE",
                    "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE"
            );
            return kolory.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void loadTeamsFromFile() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String key : clanConfig.getKeys(false)) {
            if (!key.startsWith("tab_")) continue;

            String displayName = clanConfig.getString(key + ".displayName", key.replace("tab_", "").toUpperCase());
            String colorName = clanConfig.getString(key + ".color", "WHITE");

            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Niepoprawny kolor klanu " + key + ": " + colorName);
                color = ChatColor.WHITE;
            }

            Team team = scoreboard.getTeam(key);
            if (team == null) {
                team = scoreboard.registerNewTeam(key);
            }

            team.setDisplayName(displayName);
            team.setPrefix(color.toString());
        }

        refreshAllTablists();
    }


}
