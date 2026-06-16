package com.jaasielsilva.labsystem.common;

public record ImpersonationState(Long empresaId, String empresaNome) {

    public static ImpersonationState of(Long empresaId, String empresaNome) {
        if (empresaId == null || empresaNome == null || empresaNome.isBlank()) {
            return null;
        }
        return new ImpersonationState(empresaId, empresaNome);
    }
}
