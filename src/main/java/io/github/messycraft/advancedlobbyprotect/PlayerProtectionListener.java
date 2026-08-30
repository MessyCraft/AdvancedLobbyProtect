package io.github.messycraft.advancedlobbyprotect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.Optional;

/**
 * 玩家保护：玩家主动实施的破坏行为，静默取消，受 advancedlobbyprotect.bypass 控制
 * （持有权限者可正常操作，用于管理员建造维护）。
 */
public class PlayerProtectionListener extends LobbyListener {

    // ---------------------------------------------------------------- 展示框

    /** 禁止右键展示框（旋转其中的物品、放入/取出物品）。 */
    @EventHandler(ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** 玩家击打展示框把里面的物品打落（含箭等弹射物）。 */
    @EventHandler(ignoreCancelled = true)
    public void onItemFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        Optional<Player> damager = resolvePlayer(event.getDamager());
        if (damager.isPresent() && !canBypass(damager.get())) {
            event.setCancelled(true);
        }
    }

    /** 玩家破坏展示框/画（含弹射物射击者）。 */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Optional<Player> remover = resolvePlayer(event.getRemover());
        if (remover.isPresent() && !canBypass(remover.get())) {
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------------- 盔甲架

    /** 玩家伤害盔甲架（含弹射物射击者）。 */
    @EventHandler(ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        Optional<Player> damager = resolvePlayer(event.getDamager());
        if (damager.isPresent() && !canBypass(damager.get())) {
            event.setCancelled(true);
        }
    }

    /** 玩家取放盔甲架装备。 */
    @EventHandler(ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------------- 生物

    // 生物伤害保护已归入地图保护规则（包括玩家在内所有生物均不可受伤，无人可绕过）；
    // 此处仅保留盔甲架、展示框这类允许 bypass 管理员操作的例外。

    // ---------------------------------------------------------------- 方块

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

    /** 玩家点火（打火石、火焰弹等）；自然点火由地图保护规则无条件取消。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player != null && !canBypass(player)) {
            event.setCancelled(true);
        }
    }

    /** 玩家造成方块变化（如踩踏作物等边缘情况）。 */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Player && !canBypass((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    // ---------------------------------------------------------------- 门与载具

    /**
     * 禁止手动右键开关门、活板门、栅栏门；
     * 压力板、按钮等机制交互不受影响（可正常触发红石开门）。
     */
    @EventHandler(ignoreCancelled = true)
    public void onDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null || !isDoorLike(clicked.getType())) {
            return;
        }
        if (!canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** 跨版本判断门类方块：门 / 活板门 / 栅栏门。 */
    private boolean isDoorLike(Material type) {
        String name = type.name();
        return name.endsWith("_DOOR")        // 各版本木门、铁门，以及 1.8 的 TRAP_DOOR（活板门）
                || name.endsWith("TRAPDOOR") // 1.13+ 活板门（如 OAK_TRAPDOOR）
                || name.endsWith("_GATE")    // 栅栏门（各版本均为 *_FENCE_GATE）
                || name.equals("IRON_DOOR_BLOCK"); // 1.8 铁门方块
    }

    /** 玩家破坏载具（船、矿车等）。 */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player && !canBypass((Player) event.getAttacker())) {
            event.setCancelled(true);
        }
    }
}
