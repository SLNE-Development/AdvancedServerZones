package info.preva1l.advancedserverzones.borders;

import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.config.Config;
import info.preva1l.advancedserverzones.config.Servers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Resolves the {@link BorderBounds} for <em>this</em> server from the live
 * configuration.
 * <p>
 * Picks legacy (square, {@code border.size} radius around {@code center-x/z}) or
 * grid (rectangular, derived from {@code border.grid} + {@code server.yml grid})
 * geometry depending on {@code border.grid.enabled}, validates the grid config
 * and falls back to the legacy border if it is unusable.
 * <p>
 * The result is cached and only recomputed after a config reload. This keeps the
 * 4x/second border ticker cheap and prevents validation warnings from spamming
 * the console. {@link #invalidate()} is called from the config reload hooks.
 */
public final class BorderBoundsProvider {
    private static volatile BorderBounds cached;

    private BorderBoundsProvider() {
    }

    /** The current border bounds for this server (cached until config reload). */
    public static BorderBounds get() {
        BorderBounds bounds = cached;
        if (bounds == null) {
            bounds = compute();
            cached = bounds;
        }
        return bounds;
    }

    /** Drop the cached bounds so the next {@link #get()} recomputes from disk. */
    public static void invalidate() {
        cached = null;
    }

    private static BorderBounds compute() {
        Config.Border border = Config.i().getBorder();
        Servers.Border serverBorder = Servers.i().getBorder();

        BorderBounds legacy = BorderBounds.legacy(
                serverBorder.getCenterX(),
                serverBorder.getCenterZ(),
                border.getSize()
        );

        Config.Border.Grid grid = border.getGrid();
        if (grid == null || !grid.isEnabled()) return legacy;

        int defaultWidth = grid.getColumns().getDefaultWidth();
        int defaultHeight = grid.getRows().getDefaultHeight();

        if (defaultWidth <= 0 || defaultHeight <= 0) {
            AdvancedServerZones.instance.getLogger().severe(
                    "border.grid is enabled but default-width (" + defaultWidth + ") and default-height ("
                            + defaultHeight + ") must both be > 0. Falling back to the legacy square border.");
            return legacy;
        }

        try {
            return BorderBounds.grid(
                    Servers.i().getGrid().getColumn(),
                    Servers.i().getGrid().getRow(),
                    defaultWidth,
                    widths(grid),
                    defaultHeight,
                    heights(grid),
                    msg -> AdvancedServerZones.instance.getLogger().warning(msg)
            );
        } catch (Exception ex) {
            AdvancedServerZones.instance.getLogger().log(Level.SEVERE,
                    "Failed to compute grid border bounds, falling back to the legacy square border.", ex);
            return legacy;
        }
    }

    private static Map<Integer, Integer> widths(Config.Border.Grid grid) {
        Map<Integer, Integer> map = new HashMap<>();
        grid.getColumns().getOverrides().forEach((index, override) -> map.put(index, override.getWidth()));
        return map;
    }

    private static Map<Integer, Integer> heights(Config.Border.Grid grid) {
        Map<Integer, Integer> map = new HashMap<>();
        grid.getRows().getOverrides().forEach((index, override) -> map.put(index, override.getHeight()));
        return map;
    }
}
