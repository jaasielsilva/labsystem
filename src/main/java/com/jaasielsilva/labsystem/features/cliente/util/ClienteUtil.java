package com.jaasielsilva.labsystem.features.cliente.util;

public class ClienteUtil {

    public static String normalizePhone(String phone) {
        if (phone == null) return null;

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() < 10 || digits.length() > 11) {
            throw new IllegalArgumentException("Telefone inválido.");
        }

        return digits;
    }
}