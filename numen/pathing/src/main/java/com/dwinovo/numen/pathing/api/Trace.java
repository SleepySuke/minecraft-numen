package com.dwinovo.numen.pathing.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * 宿主收寻路这一类事的那一头：为什么重搜、哪一步走不下去、这一趟为什么收场。
 *
 * <p>模块里只有这一条出口，而且只有值：{@link com.dwinovo.numen.pathing.plan.Reason} 与 {@link Outcome}
 * 也已经算好了，缺的只是有人存。怎么格式化、存到哪、存给谁看，全是宿主的事——
 * 模块不认识日志，也不认识落盘。
 *
 * <p>只在世界所在的线程上被调用。
 */
public interface Trace {

    void path(PathEvent event);

    /** 没人听：评测、单测与不关心轨迹的宿主用它。 */
    Trace NONE = event -> { };

    /**
     * 一条寻路的事。
     *
     * @param kind      {@code replan} 扔掉路线重搜 / {@code blocked} 这一步走不下去 / {@code giveup} 这一趟收场
     * @param from      这一步的起点；没有为 {@code null}
     * @param to        这一步的落点；没有为 {@code null}
     * @param reason    {@link com.dwinovo.numen.pathing.plan.Reason} 的 {@code name()}；执行期的事（卡住、看不见）与
     *                  重搜没有前提上的原因，为 {@code null}
     * @param at        卡在哪一格；没有为 {@code null}
     * @param diagnosis 收场时门面诊断出的 {@link Outcome} 那一类的名字；还没诊断、或者不是收场，为 {@code null}
     * @param detail    这一条自己的一句话（走法、重搜的理由）；没有为 {@code null}
     */
    record PathEvent(String kind, Vec3 from, Vec3 to, String reason, Vec3 at, String diagnosis, String detail) {

        public static PathEvent replan(Vec3 from, Vec3 to, BlockPos at, String detail) {
            return new PathEvent("replan", from, to, null, at == null ? null : Vec3.atCenterOf(at), null, detail);
        }

        public static PathEvent blocked(Vec3 from, Vec3 to, BlockPos cell, String reason, String diagnosis,
                                        String detail) {
            return new PathEvent("blocked", from, to, reason, cell == null ? null : Vec3.atCenterOf(cell),
                    diagnosis, detail);
        }

        public static PathEvent giveUp(Vec3 from, Vec3 to, BlockPos at, String reason, String diagnosis) {
            return new PathEvent("giveup", from, to, reason, at == null ? null : Vec3.atCenterOf(at),
                    diagnosis, null);
        }
    }
}
