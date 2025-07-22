package pl.noname.superclans;

import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.noname.superclans.clan.Clan;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class SuperTab extends JavaPlugin implements Listener {
    private Clan clan;
    public static SuperTab main;
    public static Plugin getMain() {
        return main;
    }

    protected boolean trueping = false;

    private static String skinvalue = "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=";
    private static String skinsignature = "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw=";

    @Override
    public void onEnable() {
        main = this;
        clan = new Clan(this);
        getCommand("points").setExecutor(clan);
        getCommand("points").setTabCompleter(clan);
        saveDefaultConfig();
        trueping = getConfig().getBoolean("trueping");
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            clan.setup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws IOException {
        Player p = e.getPlayer();
        File f = new File("plugins/superclans/playerdata.yml");
        YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(f);
        List<String> YamlUsers = yamlFile.getStringList("users");

        if (!YamlUsers.contains(p.getName())) {
            YamlUsers.add(p.getName());
            yamlFile.set("users", YamlUsers);
            yamlFile.save(f);
        }
        GenerateTabList generateTabList = new GenerateTabList(this, clan);
        generateTabList.gen(p, trueping, skinvalue, skinsignature, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);

        new BukkitRunnable() {
            @Override
            public void run() {
                generateTabList.gen(p, trueping, skinvalue, skinsignature, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
            }
        }.runTaskTimerAsynchronously(this, 100, 100);
    }
}
