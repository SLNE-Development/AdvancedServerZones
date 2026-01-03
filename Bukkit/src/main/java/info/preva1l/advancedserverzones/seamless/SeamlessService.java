package info.preva1l.advancedserverzones.seamless;

import info.preva1l.advancedserverzones.AdvancedServerZones;
import info.preva1l.advancedserverzones.borders.transfer.TransferData;
import info.preva1l.advancedserverzones.borders.transfer.TransferService;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import io.papermc.paper.connection.PaperPlayerLoginConnection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Field;

/**
 * This is a class.
 *
 * @author Preva1l
 * @since 31/12/2025
 */
@Service
public class SeamlessService implements Listener {
    public static final SeamlessService instance = new SeamlessService();

    private final Field packetListenerField;

    public SeamlessService() {
        try {
            this.packetListenerField = PaperPlayerLoginConnection.class.getDeclaredField("packetListener");
            this.packetListenerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Configure
    public void configure() {
        Bukkit.getPluginManager().registerEvents(this, AdvancedServerZones.instance);
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent event) throws IllegalAccessException {
        TransferData data = TransferService.instance.getData(event.getUniqueId());
        ServerLoginPacketListenerImpl packetListener = (ServerLoginPacketListenerImpl) packetListenerField.get(event.getConnection());
        packetListener
                .connection
                .channel
                .pipeline()
                .addBefore("packet_handler", "seamless", new SeamlessPlayerHandler(event.getUniqueId(), data != null ? data.entityId() : -1));
    }
}
