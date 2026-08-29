package io.github.messycraft.lobbyProtect;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.Optional;

/**
 * 大厅保护监听器：静默取消常见的破坏性事件。
 * <p>
 * 常规保护规则受权限 {@code lobbyprotect.bypass} 控制，
 * 特殊规则（耕地/海龟蛋被踩踏破坏）无视 bypass，所有人一律禁止。
 * <p>
 * 兼容 1.8 - 最新版本：不引用仅在部分版本存在的 Material 枚举字段，
 * 一律通过材质名称字符串比较（耕地在 1.8 名为 SOIL，1.13+ 名为 FARMLAND）。
 */
public class ProtectionListener implements Listener {

    private static final String BYPASS_PERMISSION = "lobbyprotect.bypass";

    private boolean canBypass(Player player) {
        return player.hasPermission(BYPASS_PERMISSION);
    }

    /**
     * 特殊规则：踩踏耕地 / 海龟蛋 —— 无视 bypass，所有人都不允许破坏。
     * 使用 LOWEST 优先级，确保在其他插件处理前先取消。
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        String type = clicked.getType().name();
        if (type.equals("FARMLAND") || type.equals("SOIL") || type.equals("TURTLE_EGG")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null || !canBypass(player)) {
            event.setCancelled(true);
        }
    }

    /** 火焰烧毁方块（自然蔓延），无人可 bypass。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // 玩家点燃的 TNT，允许有 bypass 的玩家引爆
        Entity source = null;
        if (event.getEntity() instanceof TNTPrimed) {
            source = ((TNTPrimed) event.getEntity()).getSource();
        }
        if (source instanceof Player && canBypass((Player) source)) {
            return;
        }
        event.setCancelled(true);
    }

    /** 方块爆炸（床/重生锚等），无人可 bypass。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    /** 展示框 / 画被非玩家因素（爆炸等）破坏。 */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            return; // 由下方按玩家判断处理
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player) || !canBypass((Player) event.getRemover())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        Optional<Player> damager = resolvePlayer(event.getDamager());
        if (!damager.isPresent() || !canBypass(damager.get())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player) || !canBypass((Player) event.getAttacker())) {
            event.setCancelled(true);
        }
    }

    /** 末影人搬方块等实体改方块；掉落沙/砂砾自然落地不拦截。 */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            if (!canBypass((Player) entity)) {
                event.setCancelled(true);
            }
        } else if (!(entity instanceof FallingBlock)) {
            event.setCancelled(true);
        }
    }

    private Optional<Player> resolvePlayer(Entity damager) {
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
