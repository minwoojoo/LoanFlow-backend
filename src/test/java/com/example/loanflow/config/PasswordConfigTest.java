package com.example.loanflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PasswordConfigTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoderTest(){
        String rawPassword ="1234";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("해신된 비밀번호: " + encodedPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));

        assertFalse(passwordEncoder.matches("wrong1234", encodedPassword));
    }

}
