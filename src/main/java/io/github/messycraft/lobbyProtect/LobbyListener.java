package io.github.messycraft.lobbyProtect;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * 监听器公共基类：提供 bypass 权限判断与伤害来源解析。
 */
abstract class LobbyListener implements Listener {

    private static final String BYPASS_PERMISSION = "lobbyprotect.bypass";

    protected boolean canBypass(Player player) {
        return player != null && player.hasPermission(BYPASS_PERMISSION);
    }

    /** 解析伤害来源对应的玩家（直接攻击或弹射物射击者），非玩家来源返回 empty。 */
    protected Optional<Player> resolvePlayer(Entity damager) {
        if (damager instanceof Player) {
            return Optional.of((Player) damager);
        }
        if (damager instanceof Projectile) {
            Object shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player) {
                return Optional.of((Player) shooter);
            }
        }
        return Optional.empty();
    }
}
