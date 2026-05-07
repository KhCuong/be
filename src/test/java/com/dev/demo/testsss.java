package com.dev.demo;

import java.security.SecureRandom;
import java.util.Base64;

public class testsss {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}
