package pe.edu.utec.labreserve.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;
import pe.edu.utec.labreserve.laboratory.infrastructure.LaboratoryRepository;
import pe.edu.utec.labreserve.user.domain.Role;
import pe.edu.utec.labreserve.user.domain.User;
import pe.edu.utec.labreserve.user.infrastructure.UserRepository;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           LaboratoryRepository laboratoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin").orElseGet(() -> userRepository.save(User.builder()
                .username("admin").email("admin@utec.edu.pe")
                .password(passwordEncoder.encode("Admin2026!")).role(Role.ROLE_ADMIN).build()));

        User tech = userRepository.findByUsername("tech.lab").orElseGet(() -> userRepository.save(User.builder()
                .username("tech.lab").email("tech.lab@utec.edu.pe")
                .password(passwordEncoder.encode("Tech2026!")).role(Role.ROLE_TECHNICIAN).build()));

        if (!laboratoryRepository.existsByName("FabLab")) {
            laboratoryRepository.save(Laboratory.builder()
                    .name("FabLab").location("Pabellón A - Piso 3")
                    .manager(tech).status(LaboratoryStatus.ACTIVE).build());
        }
        if (!laboratoryRepository.existsByName("Lab Electrónica")) {
            laboratoryRepository.save(Laboratory.builder()
                    .name("Lab Electrónica").location("Pabellón B - Piso 2")
                    .manager(admin).status(LaboratoryStatus.MAINTENANCE).build());
        }
        log.info("Datos semilla listos: admin/Admin2026!, tech.lab/Tech2026!, labs FabLab (ACTIVE) y Lab Electrónica (MAINTENANCE)");
    }
}
