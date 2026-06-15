package info.preva1l.advancedserverzones.borders;

import com.destroystokyo.paper.ParticleBuilder;
import info.preva1l.advancedserverzones.config.Config;
import info.preva1l.advancedserverzones.util.Cuboid;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class BorderParticles {
    public static void sendBorderParticles(Player p) {
        Vector from = p.getLocation().toVector();
        for (BorderDirection direction : BorderDirection.values()) {
            Cuboid visible = direction.getVisibleBorder(from);
            if (visible == null) continue;

            for (int[] point : visible.getAllPoints()) {
                new ParticleBuilder(Objects.requireNonNull(Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft("dust"))))
                        .color(getColor(point), 2)
                        .location(new Location(p.getWorld(), point[0], point[1], point[2]))
                        .receivers(p)
                        .spawn();
            }
        }
    }

    private static @NonNull Color getColor(int[] point) {
        Color color;
        if (Config.i().getBorder().getRainbow().isEnabled()) {
            float hue = Math.abs(point[1]) / 100f;
            hue -= (float) Math.floor(hue);
            int rgb = java.awt.Color.HSBtoRGB(hue * Config.i().getBorder().getRainbow().getHarshness(), 1f, 1f);
            int r = (rgb >>> 16) & 0xFF;
            int g = (rgb >>> 8) & 0xFF;
            int b = rgb & 0xFF;
            color = Color.fromRGB(r, g, b);
        } else {
            color = Color.fromRGB(Config.i().getBorder().getColor().getFirst(),
                    Config.i().getBorder().getColor().get(1),
                    Config.i().getBorder().getColor().getLast());
        }
        return color;
    }

    private enum BorderDirection {
        NORTH {
            @Override
            public Cuboid getVisibleBorder(Vector from) {
                BorderBounds bounds = BorderBoundsProvider.get();
                if (Math.abs(bounds.flooredMinZ() - from.getBlockZ()) > 16)
                    return null;

                return new Cuboid(
                        Math.max(from.getBlockX() - 10, bounds.flooredMinX()),
                        from.getBlockY() - 1,
                        bounds.flooredMinZ(),
                        Math.min(from.getBlockX() + 10, bounds.flooredMaxX()),
                        from.getBlockY() + 5,
                        bounds.flooredMinZ()
                );
            }
        },
        EAST {
            @Override
            public Cuboid getVisibleBorder(Vector from) {
                BorderBounds bounds = BorderBoundsProvider.get();
                if (Math.abs(bounds.flooredMaxX() - from.getBlockX()) > 16)
                    return null;

                return new Cuboid(
                        bounds.flooredMaxX(),
                        from.getBlockY() - 1,
                        Math.max(from.getBlockZ() - 10, bounds.flooredMinZ()),
                        bounds.flooredMaxX(),
                        from.getBlockY() + 5,
                        Math.min(from.getBlockZ() + 10, bounds.flooredMaxZ())
                );
            }
        },
        SOUTH {
            @Override
            public Cuboid getVisibleBorder(Vector from) {
                BorderBounds bounds = BorderBoundsProvider.get();
                if (Math.abs(bounds.flooredMaxZ() - from.getBlockZ()) > 16)
                    return null;

                return new Cuboid(
                        Math.max(from.getBlockX() - 10, bounds.flooredMinX()),
                        from.getBlockY() - 1,
                        bounds.flooredMaxZ(),
                        Math.min(from.getBlockX() + 10, bounds.flooredMaxX()),
                        from.getBlockY() + 5,
                        bounds.flooredMaxZ()
                );
            }
        },
        WEST {
            @Override
            public Cuboid getVisibleBorder(Vector from) {
                BorderBounds bounds = BorderBoundsProvider.get();
                if (Math.abs(bounds.flooredMinX() - from.getBlockX()) > 16)
                    return null;

                return new Cuboid(
                        bounds.flooredMinX(),
                        from.getBlockY() - 1,
                        Math.max(from.getBlockZ() - 10, bounds.flooredMinZ()),
                        bounds.flooredMinX(),
                        from.getBlockY() + 5,
                        Math.min(from.getBlockZ() + 10, bounds.flooredMaxZ())
                );
            }
        };

        public abstract Cuboid getVisibleBorder(Vector from);
    }
}