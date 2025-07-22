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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.noname.superclans.SuperTab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Clan implements CommandExecutor, TabCompleter {

    private final SuperTab plugin;

    public Clan(SuperTab plugin) {
        this.plugin = plugin;
    }

    private File clan;
    private FileConfiguration clanConfiguration;

    public void setup() throws IOException {
        clan = new File(plugin.getDataFolder(), "clans.yml");

        if (!clan.exists()) {
            clan.createNewFile();
        }
        clanConfiguration = YamlConfiguration.loadConfiguration(clan);
        if (!clanConfiguration.contains("Tab_czerwoni")) {
            clanConfiguration.set("Tab_czerwoni.points", 0);
        }

        if (!clanConfiguration.contains("Tab_zieloni")) {
            clanConfiguration.set("Tab_zieloni.points", 0);
        }
        clanConfiguration.save(clan);

        setupTeams();
    }

    public void setupTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team czerwoni = scoreboard.getTeam("tab_czerwoni");
        if (czerwoni == null) {
            czerwoni = scoreboard.registerNewTeam("tab_czerwoni");
            czerwoni.setPrefix(ChatColor.RED.toString());
        }

        Team zieloni = scoreboard.getTeam("tab_zieloni");
        if (zieloni == null) {
            zieloni = scoreboard.registerNewTeam("tab_zieloni");
            zieloni.setPrefix(ChatColor.GREEN.toString());
        }
    }

    public FileConfiguration get(){
        return clanConfiguration;
    }

    public int getRedPoint(){
        return get().getInt("Tab_czerwoni.points");
    }

    public int getGreenPoint(){
        return get().getInt("Tab_zieloni.points");
    }

    public void setRedPoints(int amount){
        get().set("Tab_czerwoni.points", amount);
        save();
        reload();
    }

    public void setGreenPoints(int amount){
        get().set("Tab_zieloni.points", amount);
        save();
        reload();
    }

    public void addRedPoints(int amount){
        setRedPoints(getRedPoint() + amount);
        save();
        reload();
    }

    public void removeRedPoints(int amount){
        setRedPoints(getRedPoint() - amount);
        save();
        reload();
    }

    public void addGreenPoints(int amount){
        setGreenPoints(getGreenPoint() + amount);
        save();
        reload();
    }

    public void removeGreenPoints(int amount){
        setGreenPoints(getGreenPoint() - amount);
        save();
        reload();
    }

    public void save(){
        try {
            clanConfiguration.save(clan);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reload(){
        clanConfiguration = YamlConfiguration.loadConfiguration(clan);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Poprawne użycie: /points <czerwoni/zieloni> <add/remove/set> <liczba>");
            return true;
        }

        String team = args[0];
        String action = args[1];
        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + args[2] + " nie jest liczbą!");
            return true;
        }

        switch (team.toLowerCase()) {
            case "czerwoni":
                switch (action.toLowerCase()) {
                    case "dodaj":
                        addRedPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Dodano " + amount + " punktów do czerwonych.");
                        break;
                    case "usuń":
                        removeRedPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Usunięto " + amount + " punktów z czerwonych.");
                        break;
                    case "ustaw":
                        setRedPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Ustawiono punkty czerwonych na " + amount + ".");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Nieznana akcja: " + action);
                        break;
                }
                break;

            case "zieloni":
                switch (action.toLowerCase()) {
                    case "dodaj":
                        addGreenPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Dodano " + amount + " punktów do zielonych.");
                        break;
                    case "usuń":
                        removeGreenPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Usunięto " + amount + " punktów z zielonych.");
                        break;
                    case "ustaw":
                        setGreenPoints(amount);
                        sender.sendMessage(ChatColor.GREEN + "Ustawiono punkty zielonych na " + amount + ".");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Nieznana akcja: " + action);
                        break;
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Nieznany team: " + team);
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> teams = new ArrayList<>();
            teams.add("czerwoni");
            teams.add("zieloni");
            return teams;
        } else if (args.length == 2) {
            List<String> action = new ArrayList<>();
            action.add("dodaj");
            action.add("usuń");
            action.add("ustaw");
            return action;
        } else if (args.length == 3) {
            List<String> liczba = new ArrayList<>();
            liczba.add("[ilość]");
        }
        return null;
    }
}