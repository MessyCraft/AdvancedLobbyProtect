package io.github.messycraft.lobbyProtect;

import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyProtectPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getLogger().info("LobbyProtect 已启用：大厅保护生效中（bypass 权限：lobbyprotect.bypass）");
    }

    @Override
    public void onDisable() {
        getLogger().info("LobbyProtect 已卸载。");
    }
}
