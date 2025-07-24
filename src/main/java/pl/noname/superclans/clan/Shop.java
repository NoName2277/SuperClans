package pl.noname.superclans.clan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class Shop implements Listener, CommandExecutor {

    private final Plugin plugin;
    private final Clan clan;

    public Shop(Plugin plugin, Clan clan) {
        this.plugin = plugin;
        this.clan = clan;
    }
    public Inventory createShopGui() {
        List<String> keys = new ArrayList<>(plugin.getConfig().getConfigurationSection("shop.items").getKeys(false));
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Sklep Punktów");

        for (int i = 0; i < keys.size() && i < 9; i++) {
            String key = keys.get(i);
            String name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("shop.items." + key + ".display-name", key));
            Material material = Material.matchMaterial(plugin.getConfig().getString("shop.items." + key + ".material"));

            if (material == null) continue;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "LPM: Sprzedaj");

                int sellRequire = plugin.getConfig().getInt("shop.items." + key + ".sell.require", 1);
                int sellPoints = plugin.getConfig().getInt("shop.items." + key + ".sell.points", 0);
                lore.add(ChatColor.GRAY + "Potrzebujesz: " + sellRequire + " sztuk");
                lore.add(ChatColor.GRAY + "Otrzymasz: " + sellPoints + " punktów");

                int buyPrice = plugin.getConfig().getInt("shop.items." + key + ".buy.price", 0);
                if (buyPrice > 0) {
                    lore.add(ChatColor.GREEN + "PPM: Kup za " + buyPrice + " punktów");
                } else {
                    lore.add(ChatColor.RED + "Kupno niedostępne");
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inv.setItem(i, item);
        }

        return inv;
    }

    private boolean canSell(Player player, String key) {
        int required = plugin.getConfig().getInt("shop.items." + key + ".sell.require", 1);
        int sellPoints = plugin.getConfig().getInt("shop.items." + key + ".sell.points", 0);
        if (sellPoints <= 0) return false;

        Material material = Material.matchMaterial(plugin.getConfig().getString("shop.items." + key + ".material"));
        if (material == null) return false;

        ItemStack needed = new ItemStack(material, required);
        return player.getInventory().containsAtLeast(needed, required);
    }

    private void sell(Player player, String key) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("shop.items." + key + ".material"));
        int required = plugin.getConfig().getInt("shop.items." + key + ".sell.require", 1);
        int points = plugin.getConfig().getInt("shop.items." + key + ".sell.points", 0);
        if (material == null || points <= 0) return;

        ItemStack itemStack = new ItemStack(material, required);
        player.getInventory().removeItem(itemStack);

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team != null && team.getName().startsWith("tab_")) {
            String clanName = team.getName().substring(4);
            clan.addPoints(clanName, points);
            player.sendMessage(ChatColor.GREEN + "Sprzedano " + required + " " + material.name() + " – " + points + " punktów dla klanu " + clanName + "!");
        } else {
            player.sendMessage(ChatColor.RED + "Nie jesteś w żadnym klanie (teamie zaczynającym się od 'tab_')!");
        }
    }

    private void buy(Player player, String key) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("shop.items." + key + ".material"));
        int price = plugin.getConfig().getInt("shop.items." + key + ".buy.price", 0);
        int amount = plugin.getConfig().getInt("shop.items." + key + ".sell.require", 1);
        if (material == null || price <= 0) {
            player.sendMessage(ChatColor.RED + "Ten przedmiot nie jest dostępny do kupienia!");
            return;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null || !team.getName().startsWith("tab_")) {
            player.sendMessage(ChatColor.RED + "Nie jesteś w żadnym klanie (teamie zaczynającym się od 'tab_')!");
            return;
        }

        String clanName = team.getName().substring(4);
        int clanPoints = clan.getPointsName(clanName);

        if (clanPoints < price) {
            player.sendMessage(ChatColor.RED + "Twój klan nie ma wystarczająco punktów (" + clanPoints + " < " + price + ")!");
            return;
        }

        clan.addPoints(clanName, -price);

        ItemStack itemStack = new ItemStack(material, amount);
        player.getInventory().addItem(itemStack);

        player.sendMessage(ChatColor.GREEN + "Kupiono " + amount + " " + material.name() + " za " + price + " punktów klanowych!");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Sklep Punktów")) return;
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        List<String> keys = new ArrayList<>(plugin.getConfig().getConfigurationSection("shop.items").getKeys(false));
        if (slot < 0 || slot >= keys.size()) return;

        String key = keys.get(slot);

        if (event.isLeftClick()) {
            if (canSell(player, key)) {
                sell(player, key);
            } else {
                player.sendMessage(ChatColor.RED + "Nie masz wystarczająco " + plugin.getConfig().getString("shop.items." + key + ".material") + ", aby sprzedać!");
            }
        } else if (event.isRightClick()) {
            int price = plugin.getConfig().getInt("shop.items." + key + ".buy.price", 0);
            if (price > 0) {
                buy(player, key);
            } else {
                player.sendMessage(ChatColor.RED + "Ten przedmiot nie jest dostępny do kupienia!");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("Ta komenda jest tylko dla graczy!");
            return true;
        }
        Player player = (Player) commandSender;
        player.openInventory(createShopGui());
        return false;
    }
}
