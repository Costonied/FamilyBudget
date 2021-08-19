package ru.savini.fb.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ManualPasswordEncoderUtil {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You forget input password in the arguments");
            return;
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("Encoded password is " + passwordEncoder.encode(args[0]));
    }
}
