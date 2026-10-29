package com.dwinovo.numen.pathing.gametest;

import com.dwinovo.numen.pathing.api.NavRequest;
import com.dwinovo.numen.pathing.api.Outcome;
import com.dwinovo.numen.pathing.api.Trace;
import com.dwinovo.numen.pathing.search.Goals;
import com.dwinovo.numen.pathing.spec.RouteSpec;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * 寻路把"为什么"报给宿主：走不下去、重搜、收场，每条都带着格子与原因。
 *
 * <p>这几条以前只在日志里出现一次，说完就没了——要复盘"她为什么一动不动"只能靠复现。
 */
public class PathTraceGameTests {

    /** 记下每一条的端口。 */
    static final class Recorder implements Trace {
        final List<PathEvent> events = new ArrayList<>();

        @Override
        public void path(PathEvent event) {
            events.add(event);
        }

        List<PathEvent> of(String kind) {
            return events.stream().filter(e -> e.kind().equals(kind)).toList();
        }
    }

    /** 四面与头顶都是基岩：搜完没有路，收场那条要带着诊断出来的那一类。 */
    @GameTest(template = Trial.ARENA, timeoutTicks = 600)
    public static void aSealedBoxSheCannotLeaveIsReportedOnGiveUp(GameTestHelper helper) {
        Trial t = new Trial(helper).floor();
        t.fill(4, 0, 4, 8, 4, 8, Blocks.BEDROCK);
        t.fill(5, 1, 5, 7, 3, 7, Blocks.AIR);
        Recorder trace = new Recorder();
        t.trace = trace;
        TestBody body = t.body(6, 1, 6);

        t.go(body, NavRequest.to(Goals.at(t.at(15, 1, 15)), RouteSpec.defaults())).within(400)
                .fails(Outcome.NoRoute.class)
                .then(run -> {
                    List<Trace.PathEvent> giveUp = trace.of("giveup");
                    helper.assertTrue(!giveUp.isEmpty(), "收场了一条都没报: " + trace.events);
                    Trace.PathEvent last = giveUp.get(giveUp.size() - 1);
                    helper.assertTrue(last.diagnosis() != null,
                            "收场那条没诊断出是哪一类失败: " + last);
                    helper.assertTrue(last.from() != null || last.at() != null,
                            "收场那条没有一处坐标: " + last);
                });
    }

    /** 一条走得通的路上：报出来的每条都得说清是什么事，不收场就不该报收场。 */
    @GameTest(template = Trial.ARENA, timeoutTicks = 600)
    public static void aWalkThatWorksDoesNotReportAGiveUp(GameTestHelper helper) {
        Trial t = new Trial(helper).floor();
        Recorder trace = new Recorder();
        t.trace = trace;
        TestBody body = t.body(2, 1, 12);

        t.go(body, NavRequest.to(Goals.at(t.at(10, 1, 12)), RouteSpec.defaults())).within(400)
                .arrives()
                .then(run -> {
                    for (Trace.PathEvent e : trace.events) {
                        helper.assertTrue(e.kind() != null, "每条都得说是什么事: " + e);
                        helper.assertTrue(e.kind().equals("replan") || e.kind().equals("blocked")
                                        || e.kind().equals("giveup"),
                                "kind 只有那三种,却是 " + e.kind());
                    }
                    helper.assertTrue(trace.of("giveup").isEmpty(), "走到了却报了收场: " + trace.events);
                });
    }

    /** 没人听的时候什么都不发生：{@link Trace#NONE} 是默认。 */
    @GameTest(template = Trial.ARENA, timeoutTicks = 600)
    public static void withoutAListenerTheWalkIsUnaffected(GameTestHelper helper) {
        Trial t = new Trial(helper).floor();
        TestBody body = t.body(2, 1, 12);

        t.go(body, NavRequest.to(Goals.at(t.at(10, 1, 12)), RouteSpec.defaults())).within(400).arrives();
    }
}
