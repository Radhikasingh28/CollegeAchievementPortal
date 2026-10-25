
package edu.niet.portal;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Admin: " + encoder.encode("Admin@123"));
        System.out.println("Faculty: " + encoder.encode("Faculty@123"));
        System.out.println("Student: " + encoder.encode("Student@123"));
    }
}
