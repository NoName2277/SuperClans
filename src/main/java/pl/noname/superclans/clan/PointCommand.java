package pl.noname.superclans.clan;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class PointCommand implements CommandExecutor, TabCompleter {

    private final Clan clan;


    public PointCommand(Clan clan) {
        this.clan = clan;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Użycie: /points <klan> <dodaj/usuń/ustaw> <liczba>");
            return true;
        }

        String clanName = args[0];
        String action = args[1];
        int amount;

        String key = "tab_" + clanName.toLowerCase();
        if (!clan.clanConfig.contains(key)) {
            sender.sendMessage(ChatColor.RED + "Klan " + clanName + " nie istnieje!");
            return true;
        }

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Podana liczba nie jest poprawna!");
            return true;
        }

        switch (action.toLowerCase()) {
            case "dodaj":
                clan.addPoints(clanName, amount);
                sender.sendMessage(ChatColor.GREEN + "Dodano " + amount + " punktów do klanu " + clanName + ".");
                break;
            case "usuń":
                clan.removePoints(clanName, amount);
                sender.sendMessage(ChatColor.GREEN + "Usunięto " + amount + " punktów z klanu " + clanName + ".");
                break;
            case "ustaw":
                clan.setPoints(clanName, amount);
                sender.sendMessage(ChatColor.GREEN + "Ustawiono punkty klanu " + clanName + " na " + amount + ".");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Nieznana akcja: " + action);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length==2){
            ArrayList<String> action = new ArrayList<>();
            action.add("dodaj");
            action.add("usuń");
            action.add("ustaw");
            return action;
        } else if (args.length==1) {
            return clan.getClanNames();
        } else if (args.length==3) {
            ArrayList<String> amount = new ArrayList<>();
            amount.add("[ilość]");
            return amount;
        }
        return null;
    }
}
