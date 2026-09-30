package pe.edu.utec.labreserve.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.utec.labreserve.laboratory.domain.Laboratory;
import pe.edu.utec.labreserve.laboratory.domain.LaboratoryStatus;
import pe.edu.utec.labreserve.laboratory.infrastructure.LaboratoryRepository;
import pe.edu.utec.labreserve.user.domain.Role;
import pe.edu.utec.labreserve.user.domain.User;
import pe.edu.utec.labreserve.user.infrastructure.UserRepository;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class LabReserveIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired LaboratoryRepository laboratoryRepository;
    @Autowired PasswordEncoder encoder;

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("token").asText();
    }

    @Test
    void flujoCompleto_registro_login_turno_reserva() throws Exception {
        User tech = userRepository.save(User.builder().username("tech.it").email("tech.it@utec.edu.pe")
                .password(encoder.encode("Tech2026!")).role(Role.ROLE_TECHNICIAN).build());
        userRepository.save(User.builder().username("other.tech").email("other@utec.edu.pe")
                .password(encoder.encode("Tech2026!")).role(Role.ROLE_TECHNICIAN).build());
        Laboratory lab = laboratoryRepository.save(Laboratory.builder().name("FabLab IT").location("A-301")
                .manager(tech).status(LaboratoryStatus.ACTIVE).build());

        String register = """
                {"username":"raul.lab","email":"raul@utec.edu.pe","password":"LabPass2026"}""";
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("raul.lab"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isConflict());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"raul.lab\",\"password\":\"mala-clave\"}"))
                .andExpect(status().isUnauthorized());

        String studentToken = login("raul.lab", "LabPass2026");
        String techToken = login("tech.it", "Tech2026!");
        String otherTechToken = login("other.tech", "Tech2026!");

        ZonedDateTime start = ZonedDateTime.now().plusDays(5).withNano(0);
        String slotBody = """
                {"equipmentCode":"IMP-3D-04","startTime":"%s","endTime":"%s","capacity":1}"""
                .formatted(start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        start.plusHours(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        String slotUrl = "/laboratories/" + lab.getId() + "/slots";

        mvc.perform(post(slotUrl).contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(slotUrl).header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isForbidden());
        mvc.perform(post(slotUrl).header("Authorization", "Bearer " + otherTechToken)
                        .contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isForbidden());

        String slotResp = mvc.perform(post(slotUrl).header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.laboratoryName").value("FabLab IT"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();
        long slotId = om.readTree(slotResp).get("id").asLong();

        mvc.perform(post(slotUrl).header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isConflict());
        mvc.perform(post("/laboratories/9999/slots").header("Authorization", "Bearer " + techToken)
                        .contentType(MediaType.APPLICATION_JSON).content(slotBody))
                .andExpect(status().isNotFound());

        mvc.perform(get("/equipment-slots").param("equipmentCode", "3d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].equipmentCode").value("IMP-3D-04"));

        String resUrl = "/equipment-slots/" + slotId + "/reservations";
        String purpose = "{\"purpose\":\"Prototipo del curso de Diseño\"}";
        mvc.perform(post(resUrl).header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON).content(purpose))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotId").value(slotId))
                .andExpect(jsonPath("$.studentUsername").value("raul.lab"))
                .andExpect(jsonPath("$.status").value("RESERVED"));
        mvc.perform(post(resUrl).header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON).content(purpose))
                .andExpect(status().isConflict());
        mvc.perform(post("/equipment-slots/9999/reservations").header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON).content(purpose))
                .andExpect(status().isNotFound());

        mvc.perform(get("/equipment-slots").param("equipmentCode", "3d"))
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/my-lab-reservations").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].equipmentCode").value("IMP-3D-04"))
                .andExpect(jsonPath("$.content[0].status").value("RESERVED"));
        mvc.perform(get("/my-lab-reservations").param("status", "cancelled")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/my-lab-reservations"))
                .andExpect(status().isUnauthorized());
    }
}
