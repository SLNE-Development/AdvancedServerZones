package info.preva1l.advancedserverzones.borders;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Immutable rectangular region bounds.
 * <p>
 * This is the single source of truth for border geometry. It replaces the old
 * "one radius applied to both axes" assumption with explicit, independent X and
 * Z extents so regions no longer have to be square.
 * <p>
 * Coordinates are stored as {@code double} (matching the legacy ticker which
 * compared against {@code center +/- size}); floored {@code int} accessors are
 * provided for the block based checks (particles / interaction radius).
 * <p>
 * This type intentionally has <strong>no Bukkit dependencies</strong> so the
 * coordinate math can be unit tested without a running server. Wiring it to the
 * live config lives in {@link BorderBoundsProvider}.
 *
 * @param minX minimum X (inclusive)
 * @param minZ minimum Z (inclusive)
 * @param maxX maximum X (exclusive where possible)
 * @param maxZ maximum Z (exclusive where possible)
 */
public record BorderBounds(double minX, double minZ, double maxX, double maxZ) {
    public double centerX() {
        return (minX + maxX) / 2.0;
    }

    public double centerZ() {
        return (minZ + maxZ) / 2.0;
    }

    public double width() {
        return maxX - minX;
    }

    public double height() {
        return maxZ - minZ;
    }

    public int flooredMinX() {
        return (int) Math.floor(minX);
    }

    public int flooredMaxX() {
        return (int) Math.floor(maxX);
    }

    public int flooredMinZ() {
        return (int) Math.floor(minZ);
    }

    public int flooredMaxZ() {
        return (int) Math.floor(maxZ);
    }

    /**
     * Legacy square border: a single radius ({@code size}) around a center.
     * This reproduces the historical behaviour exactly.
     *
     * @param centerX configured border center X
     * @param centerZ configured border center Z
     * @param size    radius in blocks (the old {@code border.size})
     */
    public static BorderBounds legacy(double centerX, double centerZ, int size) {
        return new BorderBounds(centerX - size, centerZ - size, centerX + size, centerZ + size);
    }

    /**
     * Grid border: rectangular bounds derived from a column (X) and row (Z).
     * <p>
     * The center column and center row are centered on world origin (0). Higher
     * column indexes stack towards +X, higher row indexes stack towards +Z.
     * Widths/heights are summed cumulatively so neighbouring regions always
     * share an edge (no gaps, no overlaps).
     *
     * @param column          this server's column index
     * @param row             this server's row index
     * @param defaultWidth    default column width (must already be validated &gt; 0)
     * @param widthOverrides  per column width overrides (column index -&gt; width)
     * @param defaultHeight   default row height (must already be validated &gt; 0)
     * @param heightOverrides per row height overrides (row index -&gt; height)
     * @param warn            consumer invoked with a human readable message when an
     *                        override value is invalid and the default is used instead
     */
    public static BorderBounds grid(
            int column,
            int row,
            int defaultWidth,
            Map<Integer, Integer> widthOverrides,
            int defaultHeight,
            Map<Integer, Integer> heightOverrides,
            Consumer<String> warn
    ) {
        double[] x = axis(column, defaultWidth, widthOverrides, warn, "column", "width");
        double[] z = axis(row, defaultHeight, heightOverrides, warn, "row", "height");
        return new BorderBounds(x[0], z[0], x[1], z[1]);
    }

    /**
     * Computes the [min, max] extent of a single grid cell on one axis by
     * cumulatively stacking cell sizes outwards from the centered cell 0.
     */
    private static double[] axis(
            int index,
            int defaultSize,
            Map<Integer, Integer> overrides,
            Consumer<String> warn,
            String indexName,
            String sizeName
    ) {
        double half = sizeAt(0, defaultSize, overrides, warn, indexName, sizeName) / 2.0;
        double min = -half;
        double max = half;

        // Stack towards positive (east / +Z): each cell starts where the previous ended.
        for (int i = 1; i <= index; i++) {
            min = max;
            max = min + sizeAt(i, defaultSize, overrides, warn, indexName, sizeName);
        }
        // Stack towards negative (west / -Z): each cell ends where the previous started.
        for (int i = -1; i >= index; i--) {
            max = min;
            min = max - sizeAt(i, defaultSize, overrides, warn, indexName, sizeName);
        }

        return new double[]{min, max};
    }

    private static int sizeAt(
            int index,
            int defaultSize,
            Map<Integer, Integer> overrides,
            Consumer<String> warn,
            String indexName,
            String sizeName
    ) {
        Integer override = overrides.get(index);
        if (override == null) return defaultSize;
        if (override > 0) return override;

        warn.accept("Ignoring invalid grid " + sizeName + " override " + override
                + " for " + indexName + " " + index + " (must be > 0), using default " + defaultSize + ".");
        return defaultSize;
    }
}
