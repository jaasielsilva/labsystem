package com.jaasielsilva.labsystem.common;

import com.jaasielsilva.labsystem.exception.BusinessException;

public final class ImpersonationContext {

    private static final ThreadLocal<ImpersonationState> CURRENT = new ThreadLocal<>();

    private ImpersonationContext() {
    }

    public static void set(ImpersonationState state) {
        if (state == null) {
            clear();
            return;
        }
        CURRENT.set(state);
    }

    public static boolean isActive() {
        return CURRENT.get() != null;
    }

    public static ImpersonationState get() {
        return CURRENT.get();
    }

    public static Long requireEmpresaId() {
        ImpersonationState state = CURRENT.get();
        if (state == null || state.empresaId() == null) {
            throw new BusinessException("Contexto de impersonação não disponível.");
        }
        return state.empresaId();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
