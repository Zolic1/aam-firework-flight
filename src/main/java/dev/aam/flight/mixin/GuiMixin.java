package dev.aam.flight.mixin;

import dev.aam.flight.RwrUtil;
import dev.aam.flight.client.AamFlightClient;
import dev.aam.flight.net.RadarPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * RWR（左下角）+ 锁定框 + 视野外边缘箭头喵。
 *
 * 投影方式：用「相机基向量」把「眼睛->目标」的 3D 偏移量正交分解，
 *   z = d·forward, x = d·right, y = d·up
 *   screenX = w/2 + (x/z)/tanHalfH * (w/2)
 *   screenY = h/2 - (y/z)/tanHalfV * (h/2)
 * 这样任意距离 / 任意偏角都精确，不会像角度近似那样越远越偏喵。
 */
@Mixin(Gui.class)
public class GuiMixin {

    private static final int R_OUT = 44;
    private static final int R_CENTER = 8;
    private static final int MARGIN = 22;
    private static final double ARC_RADIUS_OFF = 7.0;
    private static final double R_RING_OUT = R_OUT - 7.0;   // 光点用外环半径喵
    private static final double R_RING_MID = R_OUT * 0.72;
    private static final double R_RING_IN = R_OUT * 0.50;
    private static final int ARC_THICK = 3;

    private static final int C_GREEN      = 0xFF00FF66;
    private static final int C_GREEN_SOFT = 0xAA00FF66;
    private static final int C_GREEN_DIM  = 0x3A00FF66;
    private static final int C_LOCKING    = 0x8800FF66;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void aam$rwr(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;
        if (!RwrUtil.hasHelmet(mc.player)) return;

        RadarPayload data = AamFlightClient.LATEST;
        aam$sounds(mc, data);

        int cx = MARGIN + R_OUT;
        int cy = g.guiHeight() - MARGIN - R_OUT;

        ringLine(g, cx, cy, R_RING_OUT + 3.0, 1.0, C_GREEN);   // 外圈细线喵
        ringLine(g, cx, cy, R_RING_MID, 1.0, C_GREEN_DIM);
        ringLine(g, cx, cy, R_RING_IN, 1.0, C_GREEN);

        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(45.0 + i * 90.0);
            double sx = Math.sin(a);
            double sy = -Math.cos(a);
            dashed(g,
                    cx + (int) Math.round(sx * (R_CENTER + 3)), cy + (int) Math.round(sy * (R_CENTER + 3)),
                    cx + (int) Math.round(sx * (R_OUT * 0.66)), cy + (int) Math.round(sy * (R_OUT * 0.66)),
                    C_GREEN_SOFT);
        }

        ringLine(g, cx, cy, R_CENTER, C_GREEN);
        g.fill(cx - 3, cy, cx + 4, cy + 1, C_GREEN);
        g.fill(cx, cy - 3, cx + 1, cy + 4, C_GREEN);

        if (data != null) {
            double r = R_OUT - ARC_RADIUS_OFF;
            for (RadarPayload.Blip b : data.blips()) {
                if (b.type() != RadarPayload.TYPE_PLAYER && b.type() != RadarPayload.TYPE_MISSILE) continue;
                // 按距离分三层：>200 外圈 / 120~200 中圈 / <120 内圈喵
                double br = b.dist() > 200.0f ? R_RING_OUT
                        : (b.dist() > 120.0f ? R_RING_MID : R_RING_IN);
                blipArc(g, cx, cy, br, b.bearing(), C_GREEN);
            }
        }

        // 视野内玩家小圆点喵
        if (data != null) {
            aam$playerDots(g, mc, data);
        }

        if (data != null && data.threat()) {
            net.minecraft.network.chat.Component labelComp = data.lockedOnMe()
                    ? net.minecraft.network.chat.Component.translatable("aamflight.rwr.enemy_lock")
                    : net.minecraft.network.chat.Component.translatable("aamflight.rwr.enemy_missile");
            int tw = mc.font.width(labelComp);
            int bw = tw + 10;
            int bh = 12;
            int bx = cx - bw / 2;
            int by = cy - R_OUT - bh - 6;

            g.fill(bx, by, bx + bw, by + 1, C_GREEN);
            g.fill(bx, by + bh - 1, bx + bw, by + bh, C_GREEN);
            g.fill(bx, by, bx + 1, by + bh, C_GREEN);
            g.fill(bx + bw - 1, by, bx + bw, by + bh, C_GREEN);
            g.drawString(mc.font, labelComp, cx - tw / 2, by + 2, C_GREEN, false);
        }

        if (data == null) return;

        if (data.locked()) {
            drawLock(g, mc, data);
        } else if (data.lockProgress() > 0.0f) {
            net.minecraft.network.chat.Component p = net.minecraft.network.chat.Component.translatable(
                    "aamflight.hud.locking", Math.round(data.lockProgress() * 100));
            int pw = mc.font.width(p);
            g.drawString(mc.font, p, g.guiWidth() / 2 - pw / 2, g.guiHeight() / 2 + 46, C_LOCKING, true);
        }
    }

    // ==================== 锁警告音（原版音效模拟战雷 RWR 喵） ====================

    private static boolean prevLocked = false;
    private static int beepCooldown = 0;

    private void aam$sounds(Minecraft mc, RadarPayload data) {
        boolean locked = data != null && data.locked();
        boolean threat = data != null && data.threat();

        if (locked && !prevLocked) {
            play(mc, SoundEvents.NOTE_BLOCK_PLING.value(), 2.0f, 0.8f);
        } else if (!locked && prevLocked) {
            play(mc, SoundEvents.NOTE_BLOCK_PLING.value(), 0.7f, 0.6f);
        }
        prevLocked = locked;

        // 被锁定 → 急促警报；只有敌导弹 → 音效放缓喵
        boolean lockedOnMe = data != null && data.lockedOnMe();
        int interval = lockedOnMe ? 6 : 16;
        if (threat) {
            if (beepCooldown <= 0) {
                play(mc, SoundEvents.NOTE_BLOCK_PLING.value(), lockedOnMe ? 1.5f : 1.0f, lockedOnMe ? 0.6f : 0.4f);
                beepCooldown = interval;
            } else {
                beepCooldown--;
            }
        } else {
            beepCooldown = 0;
        }
    }

    private static void play(Minecraft mc, SoundEvent sound, float pitch, float volume) {
        if (mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    // ==================== 锁定框 / 边缘箭头（精确投影喵） ====================

    /** 视野内玩家：非友方红点、友方蓝点喵 */
    private void aam$playerDots(GuiGraphics g, Minecraft mc, RadarPayload data) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        double fovV = mc.options.fov().get();
        double aspect = (double) w / Math.max(1, h);
        double fovH = 2.0 * Math.toDegrees(Math.atan(Math.tan(Math.toRadians(fovV) / 2.0) * aspect));
        double tanHalfV = Math.tan(Math.toRadians(fovV) / 2.0);
        double tanHalfH = Math.tan(Math.toRadians(fovH) / 2.0);

        var cam = mc.gameRenderer.getMainCamera();
        if (cam == null || !cam.isInitialized()) return;
        Vec3 forward = new Vec3(cam.forwardVector());
        Vec3 up = new Vec3(cam.upVector());
        Vec3 right = new Vec3(cam.leftVector()).scale(-1.0);

        for (RadarPayload.Blip b : data.blips()) {
            if (b.type() != RadarPayload.TYPE_PLAYER) continue;
            Vec3 d = new Vec3(b.dx(), b.dy(), b.dz());
            double z = d.dot(forward);
            if (z < 0.5) continue;                     // 身后就不画喵
            double px = d.dot(right) / z;
            double py = d.dot(up) / z;
            double sx = w / 2.0 + px / tanHalfH * (w / 2.0);
            double sy = h / 2.0 - py / tanHalfV * (h / 2.0);
            if (sx < 2 || sx > w - 2 || sy < 2 || sy > h - 2) continue;

            int cx = (int) Math.round(sx);
            int cy = (int) Math.round(sy) - 14;        // 移到头顶位置喵
            int color = b.friendly() ? 0xFF40C0FF : 0xFFFF4040;

            // 3x3 小圆点（带黑边更容易看清喵）
            g.fill(cx - 2, cy - 2, cx + 3, cy + 3, 0x90000000);
            g.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
        }
    }
    private void drawLock(GuiGraphics g, Minecraft mc, RadarPayload data) {
        int w = g.guiWidth();
        int h = g.guiHeight();

        double fovV = mc.options.fov().get();
        double aspect = (double) w / Math.max(1, h);
        double fovH = 2.0 * Math.toDegrees(Math.atan(Math.tan(Math.toRadians(fovV) / 2.0) * aspect));
        double tanHalfV = Math.tan(Math.toRadians(fovV) / 2.0);
        double tanHalfH = Math.tan(Math.toRadians(fovH) / 2.0);

        // 相机基向量喵：直接用 Camera 的向量，滚筒飞行时也跟着滚喵
        var cam = mc.gameRenderer.getMainCamera();
        Vec3 forward;
        Vec3 up;
        Vec3 right;
        if (cam != null && cam.isInitialized()) {
            forward = new Vec3(cam.forwardVector());
            up = new Vec3(cam.upVector());
            right = new Vec3(cam.leftVector()).scale(-1.0);
        } else {
            forward = mc.player.getLookAngle();
            right = forward.cross(new Vec3(0.0, 1.0, 0.0));
            if (right.lengthSqr() < 1.0E-8) right = new Vec3(1.0, 0.0, 0.0);
            right = right.normalize();
            up = right.cross(forward).normalize();
        }

        Vec3 d = new Vec3(data.lockDx(), data.lockDy(), data.lockDz());
        double z = d.dot(forward);
        double px = d.dot(right);
        double py = d.dot(up);

        // 目标在身后 / 太近 → 用边缘箭头喵
        if (z < 0.1) {
            drawEdgeArrow(g, mc, data, px, py);
            return;
        }

        double sx = w / 2.0 + (px / z) / tanHalfH * (w / 2.0);
        double sy = h / 2.0 - (py / z) / tanHalfV * (h / 2.0);

        double halfW = Math.atan((data.lockWidth() * 0.5) / z) / tanHalfH * (w / 2.0);
        double halfH = Math.atan((data.lockHeight() * 0.5) / z) / tanHalfV * (h / 2.0);

        int hw = (int) Math.round(Math.max(8.0, Math.min(260.0, halfW * 1.05)));
        int hh = (int) Math.round(Math.max(10.0, Math.min(260.0, halfH * 1.05)));

        int x = (int) Math.round(sx);
        int y = (int) Math.round(sy);

        // 框中心或框角已经出屏 → 边缘箭头（避免"明明在视野里却变箭头/框跑出去"喵）
        boolean onScreen = x + hw > 4 && x - hw < w - 4 && y + hh > 4 && y - hh < h - 4;
        if (!onScreen) {
            drawEdgeArrow(g, mc, data, px, py);
            return;
        }

        int leg = Math.max(5, Math.min(hw, hh) / 3);
        int t = 1;

        g.fill(x - hw, y - hh, x - hw + leg, y - hh + t, C_GREEN);
        g.fill(x - hw, y - hh, x - hw + t, y - hh + leg, C_GREEN);
        g.fill(x + hw - leg, y - hh, x + hw, y - hh + t, C_GREEN);
        g.fill(x + hw - t, y - hh, x + hw, y - hh + leg, C_GREEN);
        g.fill(x - hw, y + hh - t, x - hw + leg, y + hh, C_GREEN);
        g.fill(x - hw, y + hh - leg, x - hw + t, y + hh, C_GREEN);
        g.fill(x + hw - leg, y + hh - t, x + hw, y + hh, C_GREEN);
        g.fill(x + hw - t, y + hh - leg, x + hw, y + hh, C_GREEN);

        // 名称
        net.minecraft.network.chat.Component nameComp = lockNameComponent(data);
        int nw = mc.font.width(nameComp);
        g.drawString(mc.font, nameComp, x - nw / 2, y - hh - 12, C_GREEN, true);

        // 血条
        float hp = data.lockHp();
        float maxHp = Math.max(1.0f, data.lockMaxHp());
        float ratio = Math.max(0.0f, Math.min(1.0f, hp / maxHp));

        int barW = Math.max(nw, hw * 2);
        int barX = x - barW / 2;
        int barY = y + hh + 4;

        // 目标刚被击毁 → 残影 0.7 秒：空血条 + 「已击毁目标」喵
        if (data.lockDestroyed()) {
            g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 5, 0xB0000000);
            g.fill(barX, barY, barX + barW, barY + 4, 0xFF203020);
            net.minecraft.network.chat.Component dead = net.minecraft.network.chat.Component.translatable("aamflight.lock.destroyed");
            g.drawString(mc.font, dead, x - mc.font.width(dead) / 2, barY + 7, 0xFFFF5050, true);
            return;
        }

        // 导弹：不画血条，改成文字显示耐久 / 机动 / 到达喵
        boolean isMissile = data.lockIsMissile();
        if (isMissile) {
            // 耐久：只画血条，不写数字喵
            if (maxHp > 0.5f) {
                g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 5, 0xB0000000);
                g.fill(barX, barY, barX + barW, barY + 4, 0xFF203020);
                int hpColorM = ratio > 0.5f ? C_GREEN : (ratio > 0.25f ? 0xFFFFD23B : 0xFFFF3030);
                g.fill(barX, barY, barX + (int) (barW * ratio), barY + 4, hpColorM);
            }

            net.minecraft.network.chat.Component line2 = net.minecraft.network.chat.Component.translatable(
                    "aamflight.lock.maneuver", String.format("%.1f", data.lockManeuver()));
            net.minecraft.network.chat.Component line3 = (data.lockEta() >= 0)
                    ? net.minecraft.network.chat.Component.translatable("aamflight.lock.impact", String.format("%.1f", data.lockEta()))
                    : net.minecraft.network.chat.Component.translatable("aamflight.lock.impact_none");
            g.drawString(mc.font, line2, x - mc.font.width(line2) / 2, barY + 7, C_GREEN_SOFT, true);
            g.drawString(mc.font, line3, x - mc.font.width(line3) / 2, barY + 18, C_GREEN_SOFT, true);

            double distM = Math.max(0.1, data.lockDist());
            String infoM = (int) Math.round(distM) + "m   " + String.format("%.1f", data.lockSpeed()) + " m/s";
            g.drawString(mc.font, infoM, x - mc.font.width(infoM) / 2, barY + 29, C_GREEN, true);
            return;
        }

        boolean showHp = maxHp > 0.5f;
        if (showHp) {
            g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 5, 0xB0000000);
            g.fill(barX, barY, barX + barW, barY + 4, 0xFF203020);
            int hpColor = ratio > 0.5f ? C_GREEN : (ratio > 0.25f ? 0xFFFFD23B : 0xFFFF3030);
            g.fill(barX, barY, barX + (int) (barW * ratio), barY + 4, hpColor);

            String hpText = (int) Math.ceil(hp) + " / " + (int) Math.ceil(maxHp);
            g.drawString(mc.font, hpText, x - mc.font.width(hpText) / 2, barY + 7, C_GREEN_SOFT, true);
        }

        // 距离 + 速度
        double dist = Math.max(0.1, data.lockDist());
        String info = (int) Math.round(dist) + "m   " + String.format("%.1f", data.lockSpeed()) + " m/s";
        g.drawString(mc.font, info, x - mc.font.width(info) / 2, barY + 18, C_GREEN, true);
    }

    private void drawEdgeArrow(GuiGraphics g, Minecraft mc, RadarPayload data, double px, double py) {
        int w = g.guiWidth();
        int h = g.guiHeight();

        double dirX = px;
        double dirY = -py;
        double norm = Math.sqrt(dirX * dirX + dirY * dirY);
        if (norm < 1.0E-4) {
            double ang = Math.toRadians(data.lockBearing());
            dirX = Math.sin(ang);
            dirY = -Math.cos(ang);
            norm = 1.0;
        }
        dirX /= norm;
        dirY /= norm;

        double maxX = w / 2.0 - 34.0;
        double maxY = h / 2.0 - 34.0;
        double t = Math.min(maxX / Math.max(1.0E-6, Math.abs(dirX)),
                maxY / Math.max(1.0E-6, Math.abs(dirY)));

        int ax = (int) Math.round(w / 2.0 + dirX * t);
        int ay = (int) Math.round(h / 2.0 + dirY * t);

        double len = 12.0;
        double bx = ax - dirX * len;
        double by = ay - dirY * len;
        double ox = -dirY;
        double oy = dirX;

        thickLine(g, ax, ay, (int) Math.round(bx + ox * 8), (int) Math.round(by + oy * 8), 2, C_GREEN);
        thickLine(g, ax, ay, (int) Math.round(bx - ox * 8), (int) Math.round(by - oy * 8), 2, C_GREEN);

        String info = (int) Math.round(data.lockDist()) + "m";
        int tw = mc.font.width(info);
        g.drawString(mc.font, info, ax - tw / 2, (dirY > 0) ? ay + 14 : ay - 22, C_GREEN, true);

        net.minecraft.network.chat.Component nameComp = lockNameComponent(data);
        int nw = mc.font.width(nameComp);
        g.drawString(mc.font, nameComp, ax - nw / 2, (dirY > 0) ? ay + 25 : ay - 33, C_GREEN_SOFT, true);
    }

    /** 锁定目标的名字（导弹走翻译键，其它用实体名喵） */
    private static net.minecraft.network.chat.Component lockNameComponent(RadarPayload data) {
        if (data.lockIsMissile()) {
            return net.minecraft.network.chat.Component.translatable("aamflight.lock.missile");
        }
        String n = data.lockName() == null ? "" : data.lockName();
        if (data.lockFriendly()) {
            return net.minecraft.network.chat.Component.translatable("aamflight.lock.friendly", n);
        }
        return net.minecraft.network.chat.Component.literal(n);
    }
    private static void thickLine(GuiGraphics g, int x1, int y1, int x2, int y2, int r, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            g.fill(x - r, y - r, x + r + 1, y + r + 1, color);
        }
    }

    // ==================== 雷达绘图 ====================

    /**
     * 目标指示环：实心圆环段喵
     *  - 用扫描线方式填充「角度区间 + 半径区间」，是一块完整的环带喵
     *  - 半径被夹在 [RP_MIN, RP_MAX] 之间，绝不会画进内圈喵
     */
    private static void blipArc(GuiGraphics g, int cx, int cy, double r, float bearing, int color) {
        double halfSpan = 10.0;         // 弧段张角（±10°）喵
        double rIn = r - 2.6;           // 环带内径（加粗喵）
        double rOut = r + 2.6;          // 环带外径（加粗喵）
        int R = (int) Math.ceil(rOut) + 1;

        // 逐行扫描：算出该行在环带内的水平区间，再按角度裁剪喵
        for (int dy = -R; dy <= R; dy++) {
            double y = dy;
            double o2 = rOut * rOut - y * y;
            if (o2 <= 0) continue;
            int xo = (int) Math.floor(Math.sqrt(o2));
            double i2 = rIn * rIn - y * y;
            int xi = i2 > 0 ? (int) Math.ceil(Math.sqrt(i2)) : 0;

            for (int dx = -xo; dx <= xo; dx++) {
                if (Math.abs(dx) < xi) continue;               // 环带中间是空的喵
                // 判断该点角度是否落在 bearing ± halfSpan 内喵
                double ang = Math.toDegrees(Math.atan2(dx, -y));
                double diff = ((ang - bearing + 540.0) % 360.0) - 180.0;
                if (Math.abs(diff) > halfSpan) continue;
                g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
            }
        }
    }

    private static void ringLine(GuiGraphics g, int cx, int cy, double r, int color) {
        ringLine(g, cx, cy, r, 1.0, color);
    }

    /** 带粗细的圆环喵 */
    private static void ringLine(GuiGraphics g, int cx, int cy, double r, double thick, int color) {
        int R = (int) Math.ceil(r) + 1;
        for (int dy = -R; dy <= R; dy++) {
            double y = dy;
            double outer = r + thick * 0.5;
            double inner = Math.max(0.0, r - thick * 0.5);
            double o2 = outer * outer - y * y;
            if (o2 <= 0) continue;
            int xo = (int) Math.floor(Math.sqrt(o2));
            int xi = 0;
            double i2 = inner * inner - y * y;
            if (i2 > 0) xi = (int) Math.ceil(Math.sqrt(i2));
            if (xi > 0) {
                g.fill(cx - xo, cy + dy, cx - xi + 1, cy + dy + 1, color);
                g.fill(cx + xi, cy + dy, cx + xo + 1, cy + dy + 1, color);
            } else {
                g.fill(cx - xo, cy + dy, cx + xo + 1, cy + dy + 1, color);
            }
        }
    }

    private static void dashed(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) return;
        for (int i = 0; i <= steps; i++) {
            if ((i / 3) % 2 == 1) continue;
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            g.fill(x, y, x + 1, y + 1, color);
        }
    }
}
