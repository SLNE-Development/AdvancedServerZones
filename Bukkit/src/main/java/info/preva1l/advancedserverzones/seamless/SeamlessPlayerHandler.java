package info.preva1l.advancedserverzones.seamless;

import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.borders.transfer.TransferService;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This is a class.
 *
 * @author Preva1l
 * @since 31/12/2025
 */
public class SeamlessPlayerHandler extends ChannelDuplexHandler {
    private final UUID playerUUID;
    private Player player;
    private final int realId;

    public SeamlessPlayerHandler(UUID playerUUID, int realId) {
        this.playerUUID = playerUUID;
        this.realId = realId;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientboundLoginPacket packet) {
            ClientboundLoginPacket loginPacket = new ClientboundLoginPacket(
                    realId != -1 ? realId : packet.playerId(),
                    packet.hardcore(),
                    packet.levels(),
                    packet.maxPlayers(),
                    packet.chunkRadius(),
                    packet.simulationDistance(),
                    packet.reducedDebugInfo(),
                    packet.showDeathScreen(),
                    packet.doLimitedCrafting(),
                    packet.commonPlayerSpawnInfo(),
                    packet.enforcesSecureChat()
            );
            super.write(ctx, loginPacket, promise);
            return;
        }

        if (msg instanceof ClientboundRemoveEntitiesPacket packet) {
            player = Bukkit.getPlayer(playerUUID);
            Set<Integer> delayed = new HashSet<>();
            for (int entityId : packet.getEntityIds()) {
                if (TransferService.instance.delayRemovalPacket.contains(entityId)) {
                    delayed.add(entityId);
                }
            }

            if (!delayed.isEmpty()) {
                packet.getEntityIds().removeAll(delayed);
                for (int entityId : delayed) {
                    Bukkit.getScheduler().runTaskLater(AdvancedServerZones.instance, () -> {
                        if (((CraftWorld) player.getWorld()).getHandle().getEntity(entityId) == null) {
                            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(entityId));
                        }
                    }, 20);
                }
            }
        }

        super.write(ctx, msg, promise);
    }
}
