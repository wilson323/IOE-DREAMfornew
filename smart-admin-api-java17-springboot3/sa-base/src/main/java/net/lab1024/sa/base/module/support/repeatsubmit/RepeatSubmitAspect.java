package net.lab1024.sa.base.module.support.repeatsubmit;

/**
 * 重复提交AOP占位。
 */
public class RepeatSubmitAspect {
    private final Object ticket;

    public RepeatSubmitAspect(Object ticket) {
        this.ticket = ticket;
    }
}
