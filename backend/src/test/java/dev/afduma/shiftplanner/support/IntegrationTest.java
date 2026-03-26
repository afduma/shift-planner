package dev.afduma.shiftplanner.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public abstract class IntegrationTest {

  @SuppressWarnings("resource")
  static final PostgreSQLContainer POSTGRESQL =
      new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
          .withDatabaseName("shiftplanner")
          .withUsername("shiftplanner")
          .withPassword("shiftplanner");

  static {
    POSTGRESQL.start();
  }

  protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @Autowired private WebApplicationContext webApplicationContext;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL::getPassword);
  }

  @BeforeEach
  void setUpMockMvc() {
    mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
  }

  protected String loginAndExtractAccessToken() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "email": "admin@shiftplanner.local",
                          "password": "admin123"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return body.get("accessToken").asString();
  }
}
