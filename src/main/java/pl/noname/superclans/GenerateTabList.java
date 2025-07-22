package pl.noname.superclans;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import pl.noname.superclans.clan.Clan;

import java.io.File;
import java.util.*;

public class GenerateTabList {

    private final SuperTab tabList;
    private final Clan clan;

    public GenerateTabList(SuperTab tabList, Clan clan) {
        this.tabList = tabList;
        this.clan = clan;
    }

    public void gen(Player p, boolean trueping, String skinvalue, String skinsignature, PacketPlayOutPlayerInfo.EnumPlayerInfoAction type) {
        if (p == null) return;

        File f = new File("plugins/SuperClans/playerdata.yml");
        if (!f.exists()) return;

        YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(f);
        List<String> YamlUsers = yamlFile.getStringList("users");
        if (YamlUsers == null || YamlUsers.isEmpty()) return;

        TablistMenager menager = new TablistMenager();
        List<PlayerObject> zieloniList = new ArrayList<>();
        List<PlayerObject> czerwoniList = new ArrayList<>();

        for (String str : YamlUsers) {
            Player target = Bukkit.getPlayer(str);
            if (target != null && target.isOnline()) {
                Team team = target.getScoreboard().getEntryTeam(target.getName());
                String teamName = team != null ? team.getName() : "";

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
                }

                int ping = trueping ? ((CraftPlayer) target).getHandle().ping : 9999;

                String nick = "§a" + target.getName();
                String rawPrefix = tabList.getConfig().getString("tabname-prefix", "");
                String rawSuffix = tabList.getConfig().getString("tabname-suffix", "");
                String prefix = PlaceholderAPI.setPlaceholders(target, rawPrefix);
                String suffix = PlaceholderAPI.setPlaceholders(target, rawSuffix);
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);

                String finalName = prefix + nick + suffix;

                PlayerObject po = new PlayerObject(0, finalName, "0", 0, ping, new String[]{value, signature});

                if ("tab_zieloni".equalsIgnoreCase(teamName)) {
                    zieloniList.add(po);
                } else if ("tab_czerwoni".equalsIgnoreCase(teamName)) {
                    czerwoniList.add(po);
                }
            }
        }

        zieloniList.sort(Comparator.comparing(PlayerObject::getName));
        czerwoniList.sort(Comparator.comparing(PlayerObject::getName));

        menager.modifyTablist(p, "§a§lZIELONI", "00", 9999, skinvalue, skinsignature, type);
        menager.modifyTablist(p, "§7Punkty: §f§l" + clan.getGreenPoint(), "01", 9999, skinvalue, skinsignature, type);

        int slot = 2;
        for (int i = 0; i < zieloniList.size() && slot <= 19; i++, slot++) {
            PlayerObject zielony = zieloniList.get(i);
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, zielony.getName(), slotStr, zielony.getPing(), zielony.getskin()[0], zielony.getskin()[1], type);
        }
        while (slot <= 19) {
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, " ", slotStr, 9999, skinvalue, skinsignature, type);
            slot++;
        }

        slot = 20;
        for (int i = 19; i < zieloniList.size() && slot <= 39; i++, slot++) {
            PlayerObject zielony = zieloniList.get(i);
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, zielony.getName(), slotStr, zielony.getPing(), zielony.getskin()[0], zielony.getskin()[1], type);
        }
        while (slot <= 39) {
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, " ", slotStr, 9999, skinvalue, skinsignature, type);
            slot++;
        }

        menager.modifyTablist(p, "§c§lCZERWONI     ", "40", 9999, skinvalue, skinsignature, type);
        menager.modifyTablist(p, "§7Punkty: §f§l" + clan.getRedPoint(), "41", 9999, skinvalue, skinsignature, type);

        slot = 42;
        for (int i = 0; i < czerwoniList.size() && slot <= 59; i++, slot++) {
            PlayerObject czerwony = czerwoniList.get(i);
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, czerwony.getName(), slotStr, czerwony.getPing(), czerwony.getskin()[0], czerwony.getskin()[1], type);
        }
        while (slot <= 59) {
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, " ", slotStr, 9999, skinvalue, skinsignature, type);
            slot++;
        }

        slot = 60;
        for (int i = 59; i < czerwoniList.size() && slot <= 79; i++, slot++) {
            PlayerObject czerwony = czerwoniList.get(i);
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, czerwony.getName(), slotStr, czerwony.getPing(), czerwony.getskin()[0], czerwony.getskin()[1], type);
        }
        while (slot <= 79) {
            String slotStr = slot < 10 ? "0" + slot : String.valueOf(slot);
            menager.modifyTablist(p, " ", slotStr, 9999, skinvalue, skinsignature, type);
            slot++;
        }

        List<String> footerList = tabList.getConfig().getStringList("footer");
        if (footerList != null && !footerList.isEmpty()) {
            String footerRaw = String.join("\n", footerList);
            String footerParsed = PlaceholderAPI.setPlaceholders(p, footerRaw);
            footerParsed = ChatColor.translateAlternateColorCodes('&', footerParsed);
            p.setPlayerListFooter(footerParsed);
        }
    }
}
