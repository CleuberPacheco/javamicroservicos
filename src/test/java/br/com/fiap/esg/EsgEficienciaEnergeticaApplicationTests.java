package br.com.fiap.esg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EsgEficienciaEnergeticaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve autenticar usuario, cadastrar setor, sensor IoT, registrar leitura com alerta automatico e desligamento por ociosidade")
    void fluxoCompletoEficienciaEnergeticaEsgTest() throws Exception {
        // 1. Registrar usuario ADMIN e obter Token JWT
        String registroJson = """
                {
                    "nome": "Gestor ESG Teste",
                    "email": "gestor.teste@esg.com.br",
                    "senha": "password123",
                    "role": "ADMIN"
                }
                """;

        MvcResult authResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode authNode = objectMapper.readTree(authResult.getResponse().getContentAsString());
        String token = authNode.get("token").asText();

        // 2. Cadastrar Setor Monitorado
        String setorJson = """
                {
                    "nome": "Laboratorio de Inovacao ESG",
                    "localizacao": "Bloco D - 2o Andar",
                    "responsavel": "Dra. Helena Souza",
                    "limiteMensalKwh": 3000.00,
                    "limiteDiarioKwh": 100.00,
                    "ativo": true
                }
                """;

        MvcResult setorResult = mockMvc.perform(post("/setores")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(setorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Laboratorio de Inovacao ESG"))
                .andReturn();

        Long setorId = objectMapper.readTree(setorResult.getResponse().getContentAsString()).get("id").asLong();

        // 3. Cadastrar Equipamento / Sensor IoT com desligamento automatico ativo
        String equipamentoJson = String.format("""
                {
                    "setorId": %d,
                    "codigoSensor": "IOT-LAB-900",
                    "nome": "Sistema Climatizacao Smart",
                    "tipoEquipamento": "CLIMATIZACAO",
                    "potenciaWatts": 4500.00,
                    "limiteConsumoHoraKwh": 4.50,
                    "statusOperacional": "LIGADO",
                    "desligamentoAutomatico": true
                }
                """, setorId);

        MvcResult equipResult = mockMvc.perform(post("/equipamentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(equipamentoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoSensor").value("IOT-LAB-900"))
                .andReturn();

        Long equipamentoId = objectMapper.readTree(equipResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. Registrar leitura que ultrapassa o limite (6.20 kWh > 4.50 kWh) e tem 40 min de ociosidade (>= 30 min)
        String leituraJson = String.format("""
                {
                    "equipamentoId": %d,
                    "consumoKwh": 6.200,
                    "tempoOciosoMinutos": 40,
                    "observacao": "Simulacao de pico de consumo e ociosidade prolongada"
                }
                """, equipamentoId);

        mockMvc.perform(post("/leituras-consumo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leituraJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ultrapassouLimite").value(true))
                .andExpect(jsonPath("$.desligadoAutomaticamente").value(true))
                .andExpect(jsonPath("$.statusAtualEquipamento").value("DESLIGADO"));

        // 5. Verificar se os alertas automaticos foram gerados
        mockMvc.perform(get("/alertas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 6. Consultar os indicadores de sustentabilidade consolidados
        mockMvc.perform(get("/indicadores-sustentabilidade")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDesligamentosAutomaticosIoT").value(1))
                .andExpect(jsonPath("$.equipamentosDesligados").value(1));
    }

    @Test
    @DisplayName("Deve bloquear requisicao sem token JWT nos endpoints protegidos (HTTP 403)")
    void deveBloquearAcessoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/indicadores-sustentabilidade"))
                .andExpect(status().isForbidden());
    }
}
