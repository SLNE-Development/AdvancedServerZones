package info.preva1l.advancedserverzones.config;

import de.exlll.configlib.*;
import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.borders.BorderBoundsProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Created on 26/04/2025
 *
 * @author Preva1l
 */
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public final class Servers {
    private static Servers instance;

    private static final String CONFIG_HEADER = """
            #########################################
            #          AdvancedServerZones          #
            #         Servers Configuration         #
            #########################################
            """;

    private String current = "survival-zone-03";
    private String north = "survival-zone-01";
    private String south = "survival-zone-06";
    private String east = "survival-spawn-01";
    private String west = "";

    @Comment({
            "Grid position of this server.",
            "",
            "Only used when:",
            "  border.grid.enabled: true",
            "",
            "The grid position defines which column and row this server belongs to.",
            "",
            "Directions follow Minecraft's coordinate system:",
            "  north = -Z, south = +Z, east = +X, west = -X",
            "",
            "Column (X axis):",
            "  0  = center column",
            "  1  = one column east of center  (+X)",
            "  -1 = one column west of center  (-X)",
            "",
            "Row (Z axis):",
            "  0  = center row",
            "  1  = one row south of center  (+Z)",
            "  -1 = one row north of center  (-Z)",
            "",
            "Tip: the north/south/east/west fields above are the servers a player is",
            "sent to when they cross that edge, so point them at the neighbours in",
            "those directions (e.g. the server at row -1 is your 'north').",
            "",
            "Example:",
            "  column: 0",
            "  row: 0",
            "means this server is the center server."
    })
    private Grid grid = new Grid();

    private Border border = new Border();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Grid {
        private int column = 0;
        private int row = 0;
    }

    @Configuration
    public static class Border {
        @Getter
        private double centerX, centerZ = 0;

        @Ignore
        private Integer flooredCenterX = null;
        @Ignore
        private Integer flooredCenterZ = null;

        public int flooredCenterX() {
            if (flooredCenterX == null) flooredCenterX = NumberConversions.floor(centerX);

            return flooredCenterX;
        }

        public int flooredCenterZ() {
            if (flooredCenterZ == null) flooredCenterZ = NumberConversions.floor(centerZ);

            return flooredCenterZ;
        }
    }

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    public static void reload() {
        instance = YamlConfigurations.load(new File(AdvancedServerZones.instance.getDataFolder(), "server.yml").toPath(), Servers.class, PROPERTIES);
        BorderBoundsProvider.invalidate();
        AdvancedServerZones.instance.getLogger().info("Servers configuration automatically reloaded from disk.");
    }

    public static Servers i() {
        if (instance == null) {
            instance = YamlConfigurations.update(new File(AdvancedServerZones.instance.getDataFolder(), "server.yml").toPath(), Servers.class, PROPERTIES);
            AutoReload.watch(AdvancedServerZones.instance.getDataFolder().toPath(), "server.yml", Servers::reload);
        }

        return instance;
    }
}
