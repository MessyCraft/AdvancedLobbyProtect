package io.github.messycraft.advancedlobbyprotect;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

/**
 * 地图保护：所有会改变地图/装饰的自然与世界变化，一律静默取消。
 * <p>
 * 与玩家是否持有 advancedlobbyprotect.bypass 无关，也不区分破坏是否由玩家造成
 * ——树叶凋零、火焰蔓延、耕地被踩、水流冲走火把等，任何人都无法绕过。
 */
public class MapProtectionListener extends LobbyListener {

    /** 特殊规则：踩踏耕地 / 海龟蛋。使用 LOWEST 优先级，确保最先取消。 */
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

    /** 树叶自然凋零。 */
    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    /** 火焰烧毁方块。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    /** 自然点火（岩浆引燃、雷击、火势蔓延）；玩家点火由玩家保护规则按 bypass 处理。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getPlayer() == null) {
            event.setCancelled(true);
        }
    }

    /** 冰/雪融化、珊瑚死亡退化、耕地干透退化回泥土等方块自然消退。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    /** 作物、甘蔗、仙人掌、竹子、海带等随时间自然生长。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    /** 结冰、降雪覆盖、混凝土凝固等方块自然形成。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        event.setCancelled(true);
    }

    /** 液体流动（水流/岩浆流冲走火把、地毯、红石等）、龙蛋瞬移。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(true);
    }

    /** 草蔓延、藤蔓生长、蘑菇扩散等方块自然蔓延，保持地图完全静止。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    /** 实体爆炸破坏方块（苦力怕、恶魂、TNT 等）。 */
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    /** 方块爆炸（床/重生锚在错误维度爆炸等）。 */
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    /** 展示框被非玩家伤害（防止爆炸等把里面的物品震落）。 */
    @EventHandler(ignoreCancelled = true)
    public void onItemFrameWorldDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent
                && resolvePlayer(((EntityDamageByEntityEvent) event).getDamager()).isPresent()) {
            return; // 玩家造成的由玩家保护规则处理
        }
        event.setCancelled(true);
    }

    /** 展示框/画被非玩家因素（爆炸、闪电等）破坏。 */
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent
                && resolvePlayer(((HangingBreakByEntityEvent) event).getRemover()).isPresent()) {
            return; // 玩家造成的由玩家保护规则处理
        }
        event.setCancelled(true);
    }

    /** 盔甲架受到的非玩家伤害（爆炸、火烧、生物攻击等）。 */
    @EventHandler(ignoreCancelled = true)
    public void onArmorStandWorldDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent
                && resolvePlayer(((EntityDamageByEntityEvent) event).getDamager()).isPresent()) {
            return; // 玩家造成的由玩家保护规则处理
        }
        event.setCancelled(true);
    }

    /**
     * 非玩家实体改方块：末影人搬方块、羊吃草、僵尸破门、
     * 生物踩坏耕地/海龟蛋、海龟蛋孵化、船撞碎睡莲等。
     * 掉落沙/砂砾自然落地不拦截（保留方块重力物理）。
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Player) {
            return; // 玩家造成的由玩家保护规则处理
        }
        if (!(event.getEntity() instanceof FallingBlock)) {
            event.setCancelled(true);
        }
    }

    /** 非玩家因素破坏载具（爆炸、生物攻击等）。 */
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getAttacker() instanceof Player)) {
            event.setCancelled(true);
        }
    }
}
