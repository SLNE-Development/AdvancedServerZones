package info.preva1l.advancedserverzones.config;

import de.exlll.configlib.*;
import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.borders.BorderBoundsProvider;
import info.preva1l.advancedserverzones.chat.ChatSyncService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("FieldMayBeFinal")
public final class Config {
    private static Config instance;

    private static final String CONFIG_HEADER = """
            #########################################
            #          AdvancedServerZones          #
            #########################################
            """;

    private Border border = new Border();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Border {
        @Comment({
                "Legacy border size.",
                "",
                "This is used when \"grid.enabled\" is false.",
                "",
                "The value is the radius from the configured border center.",
                "Example:",
                "  size: 500",
                "means the server area is 1000 x 1000 blocks:",
                "  X: center-x - 500 to center-x + 500",
                "  Z: center-z - 500 to center-z + 500"
        })
        private int size = 20;
        @Comment({
                "How many blocks from the borders should we prevent world modification.",
                "",
                "Recommended: 48 blocks.",
                "48 blocks = 3 chunks."
        })
        private int interactionRadius = 48;

        @Comment({
                "Grid-based border system.",
                "",
                "This allows non-square server regions while still keeping all neighbouring",
                "regions aligned correctly.",
                "",
                "Important concept:",
                "  - Column width controls the X size of a region.",
                "  - Row height controls the Z size of a region.",
                "  - A server region is always one column x one row.",
                "",
                "This means:",
                "  - If you make a row smaller, every region in that row becomes smaller on Z.",
                "  - If you make a column smaller, every region in that column becomes smaller on X.",
                "",
                "This prevents gaps, overlaps and broken transitions between neighbouring servers."
        })
        private Grid grid = new Grid();

        @Comment({
                "Particle color for borders that connect to another server.",
                "",
                "This is used for every border direction where a neighbour server",
                "is configured in server.yml.",
                "",
                "Example:",
                "  north: civilisation-north",
                "means the north border uses this color."
        })
        private List<Integer> color = List.of(184, 50, 172);

        @Comment({
                "Particle color for borders that do not connect to another server.",
                "",
                "This is used for every border direction where the neighbour server",
                "in server.yml is empty.",
                "",
                "Example:",
                "  east: ''",
                "  west: ''",
                "means the east and west borders use this edge color."
        })
        private List<Integer> edgeColor = List.of(255, 60, 60);

        private Rainbow rainbow = new Rainbow();

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Rainbow {
            private boolean enabled = false;
            @Comment("1-10")
            private int harshness = 5;
        }

        @Getter
        @Configuration
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Grid {
            @Comment({
                    "Enables the grid-based border system.",
                    "",
                    "false = use the old square border system with \"border.size\".",
                    "true  = use columns and rows from this grid section."
            })
            private boolean enabled = false;

            private Columns columns = new Columns();
            private Rows rows = new Rows();

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Columns {
                @Comment({
                        "Default width of every column in blocks.",
                        "",
                        "This controls the X size of regions.",
                        "",
                        "Example:",
                        "  default-width: 1000",
                        "means every normal region is 1000 blocks wide on the X axis."
                })
                private int defaultWidth = 1000;

                @Comment({
                        "Custom widths for specific columns.",
                        "",
                        "The key is the column index.",
                        "The value configures the width of that column in blocks.",
                        "",
                        "Column index examples (Minecraft: east = +X, west = -X):",
                        "  0  = center column",
                        "  1  = one column east of center  (+X)",
                        "  -1 = one column west of center  (-X)",
                        "",
                        "Example:",
                        "  overrides:",
                        "    0:",
                        "      width: 300",
                        "",
                        "This would make the center column 300 blocks wide."
                })
                private Map<Integer, ColumnOverride> overrides = new LinkedHashMap<>();
            }

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class ColumnOverride {
                @Comment("Width of this column in blocks (must be > 0).")
                private int width = 1000;
            }

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class Rows {
                @Comment({
                        "Default height of every row in blocks.",
                        "",
                        "This controls the Z size of regions.",
                        "",
                        "Example:",
                        "  default-height: 1000",
                        "means every normal region is 1000 blocks high on the Z axis."
                })
                private int defaultHeight = 1000;

                @Comment({
                        "Custom heights for specific rows.",
                        "",
                        "The key is the row index.",
                        "The value configures the height of that row in blocks.",
                        "",
                        "Row index examples (Minecraft: north = -Z, south = +Z):",
                        "  0  = center row",
                        "  1  = one row south of center  (+Z)",
                        "  -1 = one row north of center  (-Z)",
                        "",
                        "Example:",
                        "  overrides:",
                        "    0:",
                        "      height: 300",
                        "",
                        "This makes the center row only 300 blocks high.",
                        "Every region in that row will then be 300 blocks high."
                })
                private Map<Integer, RowOverride> overrides = new LinkedHashMap<>();
            }

            @Getter
            @Configuration
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static class RowOverride {
                @Comment("Height of this row in blocks (must be > 0).")
                private int height = 1000;
            }
        }
    }

    private ChatSyncConfig chatsync = new ChatSyncConfig();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChatSyncConfig {
        private boolean enabled = false;
        @Comment("Supported: VANILLA, CUSTOM, API")
        private ChatSyncService.ChatSyncMode mode = ChatSyncService.ChatSyncMode.VANILLA;
        private String customFormat = "<white>[<#FF0000>%server%<white>] <reset>%prefix%<reset> <white>%player% <dark_gray>> <gray>%message%";
    }

    private Redis redis = new Redis();

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Redis {
        private String host = "127.0.0.1";
        private int port = 6379;
        private String username = "";
        private String password = "abc123";
        private String channel = "advancedserverzones:chat";
    }

    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
            .charset(StandardCharsets.UTF_8)
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .header(CONFIG_HEADER).build();

    public static void reload() {
        instance = YamlConfigurations.load(new File(AdvancedServerZones.instance.getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
        BorderBoundsProvider.invalidate();
        AdvancedServerZones.instance.getLogger().info("Configuration automatically reloaded from disk.");
    }

    public static Config i() {
        if (instance == null) {
            instance = YamlConfigurations.update(new File(AdvancedServerZones.instance.getDataFolder(), "config.yml").toPath(), Config.class, PROPERTIES);
            AutoReload.watch(AdvancedServerZones.instance.getDataFolder().toPath(), "config.yml", Config::reload);
        }

        return instance;
    }
}
