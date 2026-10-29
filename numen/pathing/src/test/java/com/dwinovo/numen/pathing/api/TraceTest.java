package com.dwinovo.numen.pathing.api;

import com.dwinovo.numen.pathing.plan.TerrainPolicy;
import com.dwinovo.numen.pathing.plan.Materials;
import com.dwinovo.numen.pathing.plan.Permit;
import com.dwinovo.numen.pathing.plan.Threats;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 寻路报给宿主的那一条：为什么放弃、走到哪一格断的。它只是数据，格式化与落盘都在宿主那一侧。 */
class TraceTest {

    @Test
    void aGiveUpCarriesTheReasonAndWhereItBroke() {
        Trace.PathEvent e = Trace.PathEvent.giveUp(new Vec3(1, 64, 2), new Vec3(9, 64, 2),
                BlockPos.containing(5, 64, 2), "NO_CLEARANCE", "NoRoute");

        assertEquals("giveup", e.kind());
        assertEquals("NO_CLEARANCE", e.reason(), "reason 是枚举的 name()，不是 toString()");
        assertEquals("NoRoute", e.diagnosis());
        assertEquals(5.5, e.at().x, "格子记的是它的中心");
        assertNull(e.detail());
    }

    @Test
    void aReplanHasAFreeTextDetailAndNoReason() {
        Trace.PathEvent e = Trace.PathEvent.replan(new Vec3(1, 64, 2), null, null, "离开路线");

        assertEquals("replan", e.kind());
        assertNull(e.reason());
        assertEquals("离开路线", e.detail());
    }

    @Test
    void aBlockedStepSaysWhichCellAndWhy() {
        Trace.PathEvent e = Trace.PathEvent.blocked(new Vec3(1, 64, 2), new Vec3(2, 64, 2),
                BlockPos.containing(2, 64, 2), "OUT_OF_REACH", null, "WALK");

        assertEquals("blocked", e.kind());
        assertEquals("OUT_OF_REACH", e.reason());
        assertEquals("WALK", e.detail(), "走法记在 detail 里，不另开一栏");
    }

    @Test
    void nobodyListeningIsNotAnError() {
        Trace.NONE.path(Trace.PathEvent.replan(new Vec3(0, 0, 0), null, null, "x"));
    }

    @Test
    void portsKeepTheirThreeStrategiesAlongsideTheTrace() {
        TerrainPolicy allow = (change, pos, state, view) -> Permit.ALLOW;
        Materials none = Optional::empty;
        Ports ports = new Ports(allow, none, Threats.NONE, Trace.NONE);

        assertTrue(ports.trace() == Trace.NONE);
        assertTrue(ports.terrain() == allow, "三个策略照旧在原来的位置上");
    }
}
