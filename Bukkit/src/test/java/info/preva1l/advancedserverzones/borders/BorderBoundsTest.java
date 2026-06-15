package info.preva1l.advancedserverzones.borders;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coordinate maths for {@link BorderBounds}. These cover the legacy square
 * border and every grid case the feature spec requires "must work".
 */
class BorderBoundsTest {
    private static final double EPS = 1e-9;
    private static final Consumer<String> NO_WARN = s -> {};

    private static void assertBounds(BorderBounds b,
                                     double minX, double minZ, double maxX, double maxZ,
                                     double width, double height,
                                     double centerX, double centerZ) {
        assertEquals(minX, b.minX(), EPS, "minX");
        assertEquals(minZ, b.minZ(), EPS, "minZ");
        assertEquals(maxX, b.maxX(), EPS, "maxX");
        assertEquals(maxZ, b.maxZ(), EPS, "maxZ");
        assertEquals(width, b.width(), EPS, "width");
        assertEquals(height, b.height(), EPS, "height");
        assertEquals(centerX, b.centerX(), EPS, "centerX");
        assertEquals(centerZ, b.centerZ(), EPS, "centerZ");
    }

    @Test
    void legacySize500CenteredOnOrigin() {
        BorderBounds b = BorderBounds.legacy(0, 0, 500);
        assertBounds(b, -500, -500, 500, 500, 1000, 1000, 0, 0);
    }

    @Test
    void legacyHonoursConfiguredCenter() {
        BorderBounds b = BorderBounds.legacy(100, -50, 500);
        assertBounds(b, -400, -550, 600, 450, 1000, 1000, 100, -50);
    }

    @Test
    void gridDefault1000x1000() {
        BorderBounds b = BorderBounds.grid(0, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN);
        assertBounds(b, -500, -500, 500, 500, 1000, 1000, 0, 0);
    }

    @Test
    void gridRow0HeightOverride300() {
        Map<Integer, Integer> rows = Map.of(0, 300);
        BorderBounds b = BorderBounds.grid(0, 0, 1000, Map.of(), 1000, rows, NO_WARN);
        assertBounds(b, -500, -150, 500, 150, 1000, 300, 0, 0);
    }

    @Test
    void gridColumn0WidthOverride300() {
        Map<Integer, Integer> cols = Map.of(0, 300);
        BorderBounds b = BorderBounds.grid(0, 0, 1000, cols, 1000, Map.of(), NO_WARN);
        assertBounds(b, -150, -500, 150, 500, 300, 1000, 0, 0);
    }

    @Test
    void concreteExampleCenterNorthSouthWithRow0Override300() {
        Map<Integer, Integer> rows = Map.of(0, 300);

        // center: column 0, row 0
        assertBounds(BorderBounds.grid(0, 0, 1000, Map.of(), 1000, rows, NO_WARN),
                -500, -150, 500, 150, 1000, 300, 0, 0);

        // south neighbour: column 0, row 1 (+Z)
        assertBounds(BorderBounds.grid(0, 1, 1000, Map.of(), 1000, rows, NO_WARN),
                -500, 150, 500, 1150, 1000, 1000, 0, 650);

        // north neighbour: column 0, row -1 (-Z)
        assertBounds(BorderBounds.grid(0, -1, 1000, Map.of(), 1000, rows, NO_WARN),
                -500, -1150, 500, -150, 1000, 1000, 0, -650);
    }

    @Test
    void positiveColumnsStackEast() {
        assertEquals(500, BorderBounds.grid(1, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).minX(), EPS);
        assertEquals(1500, BorderBounds.grid(1, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).maxX(), EPS);
        assertEquals(1500, BorderBounds.grid(2, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).minX(), EPS);
        assertEquals(2500, BorderBounds.grid(2, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).maxX(), EPS);
    }

    @Test
    void negativeColumnsStackWest() {
        assertEquals(-1500, BorderBounds.grid(-1, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).minX(), EPS);
        assertEquals(-500, BorderBounds.grid(-1, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).maxX(), EPS);
        assertEquals(-2500, BorderBounds.grid(-2, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).minX(), EPS);
        assertEquals(-1500, BorderBounds.grid(-2, 0, 1000, Map.of(), 1000, Map.of(), NO_WARN).maxX(), EPS);
    }

    @Test
    void positiveRowsStackWithRow0Override300() {
        Map<Integer, Integer> rows = Map.of(0, 300);
        assertEquals(150, BorderBounds.grid(0, 1, 1000, Map.of(), 1000, rows, NO_WARN).minZ(), EPS);
        assertEquals(1150, BorderBounds.grid(0, 1, 1000, Map.of(), 1000, rows, NO_WARN).maxZ(), EPS);
        assertEquals(1150, BorderBounds.grid(0, 2, 1000, Map.of(), 1000, rows, NO_WARN).minZ(), EPS);
        assertEquals(2150, BorderBounds.grid(0, 2, 1000, Map.of(), 1000, rows, NO_WARN).maxZ(), EPS);
    }

    @Test
    void negativeRowsStackWithRow0Override300() {
        Map<Integer, Integer> rows = Map.of(0, 300);
        assertEquals(-1150, BorderBounds.grid(0, -1, 1000, Map.of(), 1000, rows, NO_WARN).minZ(), EPS);
        assertEquals(-150, BorderBounds.grid(0, -1, 1000, Map.of(), 1000, rows, NO_WARN).maxZ(), EPS);
        assertEquals(-2150, BorderBounds.grid(0, -2, 1000, Map.of(), 1000, rows, NO_WARN).minZ(), EPS);
        assertEquals(-1150, BorderBounds.grid(0, -2, 1000, Map.of(), 1000, rows, NO_WARN).maxZ(), EPS);
    }

    @Test
    void neighbouringCellsShareAnEdgeNoGapsNoOverlap() {
        // varied widths: column 0 = 300, column 1 = 1000 (default)
        Map<Integer, Integer> cols = Map.of(0, 300);
        double centerMaxX = BorderBounds.grid(0, 0, 1000, cols, 1000, Map.of(), NO_WARN).maxX();
        double eastMinX = BorderBounds.grid(1, 0, 1000, cols, 1000, Map.of(), NO_WARN).minX();
        assertEquals(centerMaxX, eastMinX, EPS, "east neighbour must start exactly where this region ends");
    }

    @Test
    void invalidOverrideFallsBackToDefaultAndWarns() {
        List<String> warnings = new ArrayList<>();
        Map<Integer, Integer> rows = Map.of(0, -5);
        BorderBounds b = BorderBounds.grid(0, 0, 1000, Map.of(), 1000, rows, warnings::add);

        // invalid height (-5) ignored -> default 1000 used
        assertBounds(b, -500, -500, 500, 500, 1000, 1000, 0, 0);
        assertTrue(warnings.stream().anyMatch(w -> w.contains("row 0")),
                "a warning should be emitted for the invalid override");
    }

    @Test
    void oddWidthProducesHalfBlockBounds() {
        BorderBounds b = BorderBounds.grid(0, 0, 301, Map.of(), 1000, Map.of(), NO_WARN);
        assertEquals(-150.5, b.minX(), EPS);
        assertEquals(150.5, b.maxX(), EPS);
        assertEquals(301, b.width(), EPS);
        assertEquals(0, b.centerX(), EPS);
    }
}
