package info.preva1l.advancedserverzones.borders.transfer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.network.Broker;
import info.preva1l.advancedserverzones.network.Message;
import info.preva1l.advancedserverzones.network.Payload;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import lombok.val;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class TransferService implements Listener {
    public static final TransferService instance = new TransferService();

    private final Cache<UUID, TransferData> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .build();

    private final Cache<UUID, Boolean> transferred = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .build();

    public final Map<UUID, Runnable> transferFinalizers = new ConcurrentHashMap<>();

    public final Set<Integer> delayRemovalPacket = new HashSet<>();

    @Configure
    public void configure() {
        Bukkit.getPluginManager().registerEvents(this, AdvancedServerZones.instance);
        Bukkit.getMessenger().registerOutgoingPluginChannel(AdvancedServerZones.instance, "BungeeCord");
    }

    public void addData(TransferData data) {
        cache.put(data.player(), data);
        delayRemovalPacket.add(data.entityId());
    }

    public TransferData getData(UUID playerUUID) {
        return cache.getIfPresent(playerUUID);
    }

    /**
     * Send a player to a different server
     *
     * @param data the data of the transfer
     */
    public void initiateTransfer(Player player, TransferData data) {
        if (transferred.asMap().containsKey(data.player())) return;
        transferred.put(data.player(), true);
        transferFinalizers.put(data.player(), () -> {
            if (!player.isOnline()) return;

            int dist = player.getClientViewDistance();
            ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

            player.getNearbyEntities(dist, dist, dist).forEach(entity -> {
                try {
                    var trackedEntity = ((CraftEntity) entity).getHandle().moonrise$getTrackedEntity();
                    trackedEntity.serverEntity.removePairing(nmsPlayer);
                } catch (Throwable ignored) {
                }
            });

            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeUTF("Connect");
            output.writeUTF(data.targetServer());
            player.sendPluginMessage(AdvancedServerZones.instance, "BungeeCord", output.toByteArray());
        });
        Message.builder()
                .type(Message.Type.TRANSFER)
                .payload(Payload.withTransferData(data))
                .build()
                .send(Broker.instance);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void playJoinEvent(AsyncPlayerSpawnLocationEvent e) {
        //<editor-fold desc="seamless transfers to be reworked">
//        CraftPlayer player = (CraftPlayer) e.getPlayer();
//        ServerPlayer p = player.getHandle();
//        p.setId(ids.get(p.getUUID()));
//        e.getPlayer().showEntity(AdvancedServerZones.i(), e.getPlayer());
//        e.getPlayer().showPlayer(AdvancedServerZones.i(), e.getPlayer());
        //</editor-fold>


        val uuid = e.getConnection().getProfile().getId();
        if (uuid == null) return;
        TransferData data = cache.getIfPresent(uuid);
        if (data == null) return;

        e.setSpawnLocation(data.position().predictedLocation(data.lastPing()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        val data = cache.asMap().remove(event.getPlayer().getUniqueId());
        if (data == null) return;
        ((CraftPlayer) event.getPlayer()).getHandle().setId(data.entityId());
    }
}