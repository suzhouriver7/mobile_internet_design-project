package dev.campuscompanionbackend.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class VerifyCodeUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCode6Num() {
        int code = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
