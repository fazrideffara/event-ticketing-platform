package com.fintech.ticket;

import com.fintech.ticket.entity.Event;
import com.fintech.ticket.entity.Role;
import com.fintech.ticket.entity.TicketTier;
import com.fintech.ticket.entity.User;
import com.fintech.ticket.repository.EventRepository;
import com.fintech.ticket.repository.TicketTierRepository;
import com.fintech.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class TicketOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketOrderServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner dataSeeder(
            UserRepository userRepository,
            EventRepository eventRepository,
            TicketTierRepository ticketTierRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Seed Users if not present
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@fintech.com")
                        .password(passwordEncoder.encode("AdminPass123!"))
                        .fullName("System Administrator")
                        .role(Role.ROLE_ADMIN)
                        .build();

                User user = User.builder()
                        .username("fazri")
                        .email("fazri@fintech.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .fullName("Muhammad Fazri Deffara Riswandy")
                        .role(Role.ROLE_USER)
                        .build();

                userRepository.saveAll(List.of(admin, user));
                log.info("Seeded default users: 'admin' and 'fazri'");
            }

            // 2. Seed Events and Ticket Tiers if not present
            if (eventRepository.count() == 0) {
                Event festival = Event.builder()
                        .name("Jakarta Summer Music Festival 2026")
                        .description("International open-air musical festival featuring multiple electronic and indie stages.")
                        .venue("Gelora Bung Karno Main Stadium, Jakarta")
                        .eventDate(LocalDateTime.now().plusMonths(2))
                        .status("PUBLISHED")
                        .build();

                Event savedFestival = eventRepository.save(festival);

                TicketTier vip = TicketTier.builder()
                        .event(savedFestival)
                        .tierName("VIP Infinity Lounge")
                        .price(new BigDecimal("5000000.00"))
                        .totalQuota(100)
                        .availableQuota(100)
                        .build();

                TicketTier general = TicketTier.builder()
                        .event(savedFestival)
                        .tierName("General Admission")
                        .price(new BigDecimal("2500000.00"))
                        .totalQuota(500)
                        .availableQuota(500)
                        .build();

                TicketTier seated = TicketTier.builder()
                        .event(savedFestival)
                        .tierName("Tribune Seated")
                        .price(new BigDecimal("1500000.00"))
                        .totalQuota(300)
                        .availableQuota(300)
                        .build();

                Event orchestra = Event.builder()
                        .name("Nusantara Symphony Orchestra Live 2026")
                        .description("Magnificent classical and modern orchestral performance.")
                        .venue("Jakarta International Theater, Jakarta")
                        .eventDate(LocalDateTime.now().plusMonths(3))
                        .status("PUBLISHED")
                        .build();

                Event savedOrchestra = eventRepository.save(orchestra);

                TicketTier orchVip = TicketTier.builder()
                        .event(savedOrchestra)
                        .tierName("Diamond Tier")
                        .price(new BigDecimal("4500000.00"))
                        .totalQuota(150)
                        .availableQuota(150)
                        .build();

                TicketTier orchRegular = TicketTier.builder()
                        .event(savedOrchestra)
                        .tierName("Gold Tier")
                        .price(new BigDecimal("1200000.00"))
                        .totalQuota(400)
                        .availableQuota(400)
                        .build();

                ticketTierRepository.saveAll(List.of(vip, general, seated, orchVip, orchRegular));
                log.info("Seeded initial concert events and ticket tiers");
            }
        };
    }
}
