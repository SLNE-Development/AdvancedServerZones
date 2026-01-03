package info.preva1l.advancedserverzones;

import info.preva1l.advancedserverzones.api.ImplAdvancedServerZonesAPI;
import info.preva1l.advancedserverzones.config.Config;
import info.preva1l.advancedserverzones.chat.ChatSyncService;
import info.preva1l.hooker.Hooker;
import info.preva1l.hooker.HookerOptions;
import info.preva1l.trashcan.extension.BasePlugin;
import info.preva1l.trashcan.extension.annotations.PluginEnable;
import info.preva1l.trashcan.extension.annotations.PluginLoad;
import org.bukkit.Bukkit;

public final class AdvancedServerZones extends BasePlugin {
    public static AdvancedServerZones instance;

    public AdvancedServerZones() {
        instance = this;
    }

    @PluginLoad
    public void load() {
        Hooker.register(
                this.getClass(),
                new HookerOptions(
                        getLogger(),
                        false,
                        task -> Bukkit.getAsyncScheduler().runNow(this, t -> task.run()),
                        task ->  Bukkit.getScheduler().runTask(this, task),
                        task ->  Bukkit.getScheduler().runTaskLater(this, task, 10),
                        "info.preva1l.advancedserverzones.hooks"
                )
        );
    }

    @PluginEnable
    public void enable() {
        if (Config.i().getChatsync().isEnabled()) {
            ChatSyncService chatSync = new ChatSyncService();
            getServer().getPluginManager().registerEvents(chatSync, this);
        }

        AdvancedServerZonesAPI.setInstance(new ImplAdvancedServerZonesAPI());

        getLogger().info("Advanced Server Zones Loaded");
    }
}
