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

    private final SuperClans superClans;
    private final Clan clan;

    public GenerateTabList(SuperClans superClans, Clan clan) {
        this.superClans = superClans;
        this.clan = clan;
    }

    private int[] getSegmentSlotsById(int id) {
        int start = 0;
        switch (id) {
            case 1: start = 0; break;
            case 2: start = 20; break;
            case 3: start = 40; break;
            case 4: start = 60; break;
            default: start = 0;
        }
        return new int[] {start, start + 19};
    }

    public void gen(Player p, boolean trueping, String skinvalue, String skinsignature, PacketPlayOutPlayerInfo.EnumPlayerInfoAction type) {
        if (p == null) return;

        File f = new File("plugins/SuperClans/playerdata.yml");
        if (!f.exists()) return;

        YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(f);
        List<String> YamlUsers = yamlFile.getStringList("users");
        if (YamlUsers == null) return;

        TablistMenager menager = new TablistMenager();

        Set<String> clanKeys = clan.get().getKeys(false);
        List<String> clans = new ArrayList<>();
        for (String key : clanKeys) {
            if (key.startsWith("tab_")) {
                clans.add(key);
            }
        }

        if (clans.isEmpty()) {
            for (int slot = 0; slot < 80; slot++) {
                menager.modifyTablist(p, " ", String.valueOf(slot), 9999, skinvalue, skinsignature, type);
            }
            return;
        }

        Map<String, List<PlayerObject>> clanPlayersMap = new HashMap<>();

        for (String clanKey : clans) {
            clanPlayersMap.put(clanKey, new ArrayList<>());
        }

        for (String playerName : YamlUsers) {
            Player target = Bukkit.getPlayer(playerName);
            if (target != null && target.isOnline()) {
                Team team = target.getScoreboard().getEntryTeam(target.getName());
                if (team == null) continue;
                String teamName = team.getName();
                if (!clanPlayersMap.containsKey(teamName)) continue;

                GameProfile profile = ((CraftPlayer) target).getHandle().getProfile();
                Collection<Property> textures = profile.getProperties().get("textures");
                String signature = "";
                String value = "";
                if (textures != null && !textures.isEmpty()) {
                    for (Property prop : textures) {
                        signature = prop.getSignature();
                        value = prop.getValue();
                        break;
                    }
                }

                int ping = trueping ? ((CraftPlayer) target).getHandle().ping : 9999;

                String nick = "§a" + target.getName();
                String prefix = PlaceholderAPI.setPlaceholders(target, superClans.getConfig().getString("tabname-prefix", ""));
                String suffix = PlaceholderAPI.setPlaceholders(target, superClans.getConfig().getString("tabname-suffix", ""));
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);

                String finalName = prefix + nick + suffix;

                PlayerObject po = new PlayerObject(0, finalName, "0", 0, ping, new String[]{value, signature});
                clanPlayersMap.get(teamName).add(po);
            }
        }

        for (List<PlayerObject> players : clanPlayersMap.values()) {
            players.sort(Comparator.comparing(PlayerObject::getName));
        }

        Map<Integer, String> idToClanKey = new HashMap<>();
        for (String clanKey : clans) {
            int id = clan.get().getInt(clanKey + ".id", 0);
            if (id >= 1 && id <= 4) {
                idToClanKey.put(id, clanKey);
            }
        }

        for (int id = 1; id <= 4; id++) {
            int[] segment = getSegmentSlotsById(id);
            int startSlot = segment[0];
            int endSlot = segment[1];

            if (!idToClanKey.containsKey(id)) {
                for (int slot = startSlot; slot <= endSlot; slot++) {
                    menager.modifyTablist(p, "                    ", String.valueOf(slot), 9999, skinvalue, skinsignature, type);
                }
                continue;
            }

            String clanKey = idToClanKey.get(id);
            List<PlayerObject> players = clanPlayersMap.getOrDefault(clanKey, Collections.emptyList());
            String displayName = clan.get().getString(clanKey + ".displayName", clanKey).toUpperCase();
            String colorName = clan.get().getString(clanKey + ".color", "WHITE");
            ChatColor color;
            try {
                color = ChatColor.valueOf(colorName);
            } catch (IllegalArgumentException e) {
                color = ChatColor.WHITE;
            }

            menager.modifyTablist(p, color + "§l" + displayName, String.valueOf(startSlot), 9999, skinvalue, skinsignature, type);
            menager.modifyTablist(p, "§7Punkty: §3" + clan.getPoints(clanKey), String.valueOf(startSlot + 1), 9999, skinvalue, skinsignature, type);
            menager.modifyTablist(p, "§7Saldo: §3" + clan.getTeamBalance(clanKey), String.valueOf(startSlot + 2), 9999, skinvalue, skinsignature, type);
            menager.modifyTablist(p, "", String.valueOf(startSlot + 3), 9999, skinvalue, skinsignature, type);

            int slot = startSlot + 4;
            for (int i = 0; i < players.size() && slot <= endSlot; i++, slot++) {
                PlayerObject po = players.get(i);
                menager.modifyTablist(p, po.getName(), String.valueOf(slot), po.getPing(), po.getskin()[0], po.getskin()[1], type);
            }

            while (slot <= endSlot) {
                menager.modifyTablist(p, " ", String.valueOf(slot), 9999, skinvalue, skinsignature, type);
                slot++;
            }
        }

        List<String> footerList = superClans.getConfig().getStringList("footer");
        if (footerList != null && !footerList.isEmpty()) {
            String footerRaw = String.join("\n", footerList);
            String footerParsed = PlaceholderAPI.setPlaceholders(p, footerRaw);
            footerParsed = ChatColor.translateAlternateColorCodes('&', footerParsed);
            p.setPlayerListFooter(footerParsed);
            p.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&', superClans.getConfig().getString("brand")));
        }
    }
}