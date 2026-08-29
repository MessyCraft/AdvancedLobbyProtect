package io.github.messycraft.advancedlobbyprotect;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdvancedLobbyProtectPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MapProtectionListener mapListener = new MapProtectionListener();
        getServer().getPluginManager().registerEvents(new PlayerProtectionListener(), this);
        getServer().getPluginManager().registerEvents(mapListener, this);
        registerMoistureProtection(mapListener);
        getLogger().info("AdvancedLobbyProtect 已启用：玩家保护（bypass 权限：advancedlobbyprotect.bypass）+ 地图保护 均已生效");
    }

    /**
     * 耕地湿度变化（干涸）保护：MoistureChangeEvent 仅存在于 1.13+ API，
     * 编译目标为 1.8，因此通过反射注册；1.12- 无此事件，
     * 耕地退化回泥土已由 BlockFadeEvent 拦截。
     */
    private void registerMoistureProtection(MapProtectionListener listener) {
        try {
            Class<? extends Event> moistureEvent = Class.forName("org.bukkit.event.block.MoistureChangeEvent")
                    .asSubclass(Event.class);
            getServer().getPluginManager().registerEvent(moistureEvent, listener, EventPriority.NORMAL,
                    (ignored, event) -> ((Cancellable) event).setCancelled(true), this, true);
            getLogger().info("已注册耕地湿度保护（MoistureChangeEvent）");
        } catch (ClassNotFoundException ignored) {
            getLogger().info("当前版本无 MoistureChangeEvent，耕地干涸保护由 BlockFadeEvent 承担");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedLobbyProtect 已卸载。");
    }
}
