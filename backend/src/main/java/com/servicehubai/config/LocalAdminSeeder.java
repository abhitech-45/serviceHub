package com.servicehubai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.servicehubai.user.infrastructure.UserRepository;
import com.servicehubai.user.domain.UserEntity;

@Component
@Profile("local")
public class LocalAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String displayName;

    public LocalAdminSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${servicehub.local-admin.email:admin@servicehub.local}") String email,
            @Value("${servicehub.local-admin.password:Admin@12345}") String password,
            @Value("${servicehub.local-admin.display-name:ServiceHub Administrator}") String displayName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase();
        this.password = password;
        this.displayName = displayName;
    }

    @Override
    @Transactional
    public void run(String... args) {
        UserEntity admin = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (admin == null) {
            userRepository.save(UserEntity.administrator(email, passwordEncoder.encode(password), displayName));
        }

        seedSupportDirectoryEntry("academic.support@servicehub.local", "Academic Support Desk");
        seedSupportDirectoryEntry("admissions.support@servicehub.local", "Admissions Support Desk");
        seedSupportDirectoryEntry("it.support@servicehub.local", "IT Helpdesk");
        seedSupportDirectoryEntry("student.services@servicehub.local", "Student Services Desk");
        seedSupportDirectoryEntry("campus.operations@servicehub.local", "Campus Operations Desk");

    }

    private void seedSupportDirectoryEntry(String agentEmail, String agentDisplayName) {
        if (userRepository.findByEmailIgnoreCase(agentEmail).isEmpty()) {
            userRepository.save(UserEntity.supportAgentDirectoryEntry(agentEmail, agentDisplayName));
        }
    }
}
