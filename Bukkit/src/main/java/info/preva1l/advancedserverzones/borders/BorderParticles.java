package info.preva1l.advancedserverzones.borders;

import com.destroystokyo.paper.ParticleBuilder;
import info.preva1l.advancedserverzones.config.Config;
import info.preva1l.advancedserverzones.config.Servers;
import info.preva1l.advancedserverzones.util.Cuboid;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public final class BorderParticles {
    public static void sendBorderParticles(Player p) {
        Vector from = p.getLocation().toVector();
        for (BorderDirection direction : BorderDirection.values()) {
            Cuboid visible = direction.getVisibleBorder(from);
            if (visible == null) continue;

            for (int[] point : visible.getAllPoints()) {
                new ParticleBuilder(Objects.requireNonNull(Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft("dust"))))
                        .color(getColor(direction, point), 2)
                        .location(new Location(p.getWorld(), point[0], point[1], point[2]))
                        .receivers(p)
                        .spawn();
            }
        }
    }

    private static @NonNull Color getColor(BorderDirection direction, int[] point) {
        if (Config.i().getBorder().getRainbow().isEnabled()) {
            float hue = Math.abs(point[1]) / 100f;
            hue -= (float) Math.floor(hue);
            int rgb = java.awt.Color.HSBtoRGB(hue * Config.i().getBorder().getRainbow().getHarshness(), 1f, 1f);
            int r = (rgb >>> 16) & 0xFF;
            int g = (rgb >>> 8) & 0xFF;
            int b = rgb & 0xFF;
            return Color.fromRGB(r, g, b);
        }

        List<Integer> configuredColor = direction.isConnected()
                ? Config.i().getBorder().getColor()
                : Config.i().getBorder().getEdgeColor();

        return Color.fromRGB(
                configuredColor.getFirst(),
                configuredColor.get(1),
                configuredColor.getLast()
        );
    }

    private static boolean hasServer(String server) {
        return server != null && !server.isBlank();
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

        public boolean isConnected() {
            return switch (this) {
                case NORTH -> hasServer(Servers.i().getNorth());
                case SOUTH -> hasServer(Servers.i().getSouth());
                case EAST -> hasServer(Servers.i().getEast());
                case WEST -> hasServer(Servers.i().getWest());
            };
        }
    }
}