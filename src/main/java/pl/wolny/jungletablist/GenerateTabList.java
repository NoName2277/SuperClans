package pl.wolny.jungletablist;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class GenerateTabList {

    private final JungleTabList tabList;

    public GenerateTabList(JungleTabList tabList){
        this.tabList = tabList;
    }
    public void gen(Player p, boolean trueping, String skinvalue, String skinsignature, PacketPlayOutPlayerInfo.EnumPlayerInfoAction type) {
        if (p == null) {
            Bukkit.getLogger().warning("GenerateTabList.gen - przekazany gracz jest null!");
            return;
        }

        File f = new File("plugins/JungleTabList/playerdata.yml");
        if (!f.exists()) {
            Bukkit.getLogger().warning("GenerateTabList.gen - brak pliku playerdata.yml!");
            return;
        }

        YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(f);
        List<String> YamlUsers = yamlFile.getStringList("users");
        if (YamlUsers == null || YamlUsers.isEmpty()) {
            Bukkit.getLogger().warning("GenerateTabList.gen - sekcja 'users' w playerdata.yml jest pusta lub nie istnieje!");
            return;
        }

        TablistMenager menager = new TablistMenager();

        menager.modifyTablist(p, "§a§lGracze §f   " + Bukkit.getOnlinePlayers().size() + "§8/§7" + Bukkit.getMaxPlayers(), "00", 9999, skinvalue, skinsignature, type);
        menager.modifyTablist(p, " ", "01", 9999, skinvalue, skinsignature, type);

        int i = 2;
        int outoflimit = 0;
        boolean isOutOfLimit = false;

        List<PlayerObject> Queue = new ArrayList<>();
        List<PlayerObject> PlayerObjectList = new ArrayList<>();

        for (String str : YamlUsers) {
            if (i > 38) {
                isOutOfLimit = true;
                outoflimit++;
                continue;
            }

            Player target = Bukkit.getPlayer(str);
            if (target != null && target.isOnline()) {
                target.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&', tabList.getConfig().getString("brand")));
                GameProfile playerProfile = ((CraftPlayer) target).getHandle().getProfile();
                Collection<Property> textures = playerProfile.getProperties().get("textures");

                String signature = "";
                String value = "";
                if (textures != null && !textures.isEmpty()) {
                    for (Property property : textures) {
                        signature = property.getSignature();
                        value = property.getValue();
                        break;
                    }
                } else {
                    Bukkit.getLogger().warning("GenerateTabList.gen - brak tekstur dla gracza " + str);
                }

                int ping = trueping ? ((CraftPlayer) target).getHandle().ping : 9999;

                PlayerObjectList.add(new PlayerObject(0,
                        "§a" + str,
                        "0", 0, ping, new String[]{value, signature}));
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(str);
                if (offlinePlayer != null) {
                    PlayerObject po = new PlayerObject(0,
                            "§7" + str,
                            "0", 0, 0, new String[]{"", ""});
                    Queue.add(po);
                } else {
                    Bukkit.getLogger().warning("GenerateTabList.gen - OfflinePlayer nie znaleziony w cache dla " + str);
                }
            }
        }

        PlayerObjectList.sort(Comparator.comparing(PlayerObject::getName));
        for (PlayerObject object : PlayerObjectList) {
            if (i > 38) break;
            String slot = i < 10 ? "0" + i : String.valueOf(i);
            menager.modifyTablist(p, object.getName(), slot, object.getPing(), object.getskin()[0], object.getskin()[1], type);
            i++;
        }

        Queue.sort(Comparator.comparing(PlayerObject::getName));
        for (PlayerObject str : Queue) {
            if (i > 38) {
                isOutOfLimit = true;
                outoflimit++;
                continue;
            }
            String slot = i < 10 ? "0" + i : String.valueOf(i);
            menager.modifyTablist(p, str.getName(), slot, 9999, skinvalue, skinsignature, type);
            i++;
        }

        if (isOutOfLimit) {
            menager.modifyTablist(p, "... I " + outoflimit + " jeszcze ...", String.valueOf(i), 9999, skinvalue, skinsignature, type);
            i++;
        }

        while (i < 60) {
            menager.modifyTablist(p, " ", String.valueOf(i), 9999, skinvalue, skinsignature, type);
            i++;
        }

        menager.modifyTablist(p, "§a§lŚmierci", String.valueOf(i), 9999, skinvalue, skinsignature, type);
        i++;
        menager.modifyTablist(p, " ", String.valueOf(i), 9999, skinvalue, skinsignature, type);
        i++;

        List<PlayerObject> deathList = new ArrayList<>();
        for (String str : YamlUsers) {
            Player target = Bukkit.getPlayer(str);
            if (target != null && target.isOnline()) {
                deathList.add(new PlayerObject(0, "§3" + target.getStatistic(Statistic.DEATHS) + " §7" + str,
                        String.valueOf(i), target.getStatistic(Statistic.DEATHS), 9999, new String[]{skinvalue, skinsignature}));
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(str);
                if (offlinePlayer != null) {
                    deathList.add(new PlayerObject(0, "§3" + offlinePlayer.getStatistic(Statistic.DEATHS) + " §7" + str,
                            String.valueOf(i), offlinePlayer.getStatistic(Statistic.DEATHS), 9999, new String[]{skinvalue, skinsignature}));
                }
            }
        }

        deathList.sort(Comparator.comparingInt(PlayerObject::getDeaths).reversed());
        for (PlayerObject object : deathList) {
            if (i > 79) {
                break;
            }
            menager.modifyTablist(p, object.getName(), String.valueOf(i), 9999, object.getskin()[0], object.getskin()[1], type);
            i++;
        }

        while (i != 80) {
            menager.modifyTablist(p, " ", String.valueOf(i), 9999, skinvalue, skinsignature, type);
            i++;
        }
    }
}
