package br.com.fiap.esg.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class MenuInterativoConsole {

    private final ObjectMapper objectMapper;

    @Value("${server.port:8080}")
    private int serverPort;

    private String jwtToken = null;
    private String usuarioLogado = null;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @EventListener(ApplicationReadyEvent.class)
    public void iniciarMenuAposStartup() {
        // Nao inicia em porta aleatoria de teste (server.port=0)
        if (serverPort <= 0) {
            return;
        }

        Thread menuThread = new Thread(this::executarLoopMenu, "menu-interativo-cli");
        menuThread.setDaemon(true);
        menuThread.start();
    }

    private void executarLoopMenu() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return;
        }

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            boolean executando = true;

            while (executando) {
                exibirCabecalhoMenu();
                System.out.print("Escolha uma opcao do menu: ");

                String opcao;
                try {
                    if (!scanner.hasNextLine()) {
                        // Ambiente sem terminal interativo (ex: container Docker em background)
                        return;
                    }
                    opcao = scanner.nextLine().trim();
                } catch (NoSuchElementException e) {
                    return;
                }

                if (opcao.isEmpty()) {
                    continue;
                }

                try {
                    switch (opcao) {
                        case "1" -> menuAutenticacao(scanner);
                        case "2" -> {
                            garantirAutenticacaoAutomatica();
                            enviarRequisicao("GET", "/setores?page=0&size=10", null);
                        }
                        case "3" -> {
                            garantirAutenticacaoAutomatica();
                            menuCadastrarSetor(scanner);
                        }
                        case "4" -> {
                            garantirAutenticacaoAutomatica();
                            menuAtualizarSetor(scanner);
                        }
                        case "5" -> {
                            garantirAutenticacaoAutomatica();
                            enviarRequisicao("GET", "/equipamentos?page=0&size=10", null);
                        }
                        case "6" -> {
                            garantirAutenticacaoAutomatica();
                            menuCadastrarEquipamento(scanner);
                        }
                        case "7" -> {
                            garantirAutenticacaoAutomatica();
                            menuRegistrarLeituraConsumo(scanner);
                        }
                        case "8" -> {
                            garantirAutenticacaoAutomatica();
                            menuDesligarEquipamentoOcioso(scanner);
                        }
                        case "9" -> {
                            garantirAutenticacaoAutomatica();
                            menuHistoricoConsumo(scanner);
                        }
                        case "10" -> {
                            garantirAutenticacaoAutomatica();
                            menuAlertas(scanner);
                        }
                        case "11" -> {
                            garantirAutenticacaoAutomatica();
                            enviarRequisicao("GET", "/indicadores-sustentabilidade", null);
                        }
                        case "12" -> executarDemonstracaoCompletaAutomatica();
                        case "13" -> {
                            garantirAutenticacaoAutomatica();
                            demonstrarValidacaoEExcecoes();
                        }
                        case "0" -> {
                            System.out.println("\n[INFO] Menu interativo encerrado. A API continua ativa em http://localhost:" + serverPort);
                            executando = false;
                        }
                        default -> System.out.println("\n[AVISO] Opcao invalida! Digite um numero entre 0 e 13.");
                    }
                } catch (Exception e) {
                    System.out.println("\n[ERRO] Falha ao processar operacao no menu: " + e.getMessage());
                }
            }
        }
    }

    private void exibirCabecalhoMenu() {
        String statusAuth = (jwtToken != null)
                ? "AUTENTICADO (" + usuarioLogado + " - Token JWT Ativo)"
                : "NAO AUTENTICADO (Sera autenticado automaticamente ao usar as opcoes)";

        System.out.println("\n================================================================================");
        System.out.println("   MENU INTERATIVO ESG - TEMA 1: EFICIENCIA ENERGETICA E SUSTENTABILIDADE");
        System.out.println("   API RESTful: http://localhost:" + serverPort + " | Swagger: http://localhost:" + serverPort + "/swagger-ui.html");
        System.out.println("   Status Spring Security: " + statusAuth);
        System.out.println("================================================================================");
        System.out.println("  [1]  Autenticar / Gerar Token JWT (POST /auth/login ou /auth/register)");
        System.out.println("  [2]  Listar Setores e Limites de Consumo (GET /setores)");
        System.out.println("  [3]  Cadastrar Novo Setor Monitorado (POST /setores)");
        System.out.println("  [4]  Atualizar Limites de um Setor (PUT /setores/{id})");
        System.out.println("  [5]  Listar Equipamentos e Sensores IoT (GET /equipamentos)");
        System.out.println("  [6]  Cadastrar Novo Equipamento / Sensor IoT (POST /equipamentos)");
        System.out.println("  [7]  Registrar Leitura de Consumo + Regra IoT (POST /leituras-consumo)");
        System.out.println("  [8]  Desligar Equipamento Ocioso Remotamente via IoT (PUT /equipamentos/{id}/desligar-ocioso)");
        System.out.println("  [9]  Consultar Historico de Consumo por Setor (GET /leituras-consumo/setor/{id})");
        System.out.println("  [10] Listar ou Resolver Alertas Automaticos (GET /alertas | PUT /alertas/{id}/resolver)");
        System.out.println("  [11] Painel de Indicadores de Sustentabilidade ESG (GET /indicadores-sustentabilidade)");
        System.out.println("  ------------------------------------------------------------------------------");
        System.out.println("  [12] >>> DEMONSTRACAO COMPLETA AUTOMATICA (Testa todos os endpoints de uma vez)");
        System.out.println("  [13] >>> TESTAR VALIDACAO (@Valid) E TRATAMENTO DE EXCECOES (HTTP 400 / 404)");
        System.out.println("  [0]  Sair do menu interativo (manter apenas API rodando)");
        System.out.println("================================================================================");
    }

    private void garantirAutenticacaoAutomatica() {
        if (jwtToken != null) {
            return;
        }
        System.out.println("\n[AUTO-AUTH] Obtendo Token JWT automaticamente para execucao da requisicao...");
        realizarLoginOuCadastroAutomatico("gestor.cli@esg.com.br", "123456");
    }

    private void realizarLoginOuCadastroAutomatico(String email, String senha) {
        try {
            String loginJson = String.format("{\"email\":\"%s\",\"senha\":\"%s\"}", email, senha);
            HttpResponse<String> resp = dispararHttp("POST", "/auth/login", loginJson);

            if (resp.statusCode() != 200) {
                String regJson = String.format(
                        "{\"nome\":\"Avaliador ESG\",\"email\":\"%s\",\"senha\":\"%s\",\"role\":\"ADMIN\"}",
                        email, senha);
                resp = dispararHttp("POST", "/auth/register", regJson);
            }

            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                JsonNode node = objectMapper.readTree(resp.body());
                this.jwtToken = node.get("token").asText();
                this.usuarioLogado = node.get("email").asText() + " [" + node.get("role").asText() + "]";
                System.out.println("[OK] Token JWT obtido com sucesso para: " + this.usuarioLogado);
            } else {
                imprimirRespostaHttp("POST", "/auth/login", resp);
            }
        } catch (Exception e) {
            System.out.println("[ERRO] Nao foi possivel autenticar automaticamente: " + e.getMessage());
        }
    }

    private void menuAutenticacao(Scanner scanner) {
        System.out.println("\n--- AUTENTICACAO JWT (SPRING SECURITY) ---");
        System.out.print("Deseja [1] Login automatico de avaliador ou [2] Digitar e-mail/senha? (Enter = 1): ");
        String sub = scanner.nextLine().trim();

        if ("2".equals(sub)) {
            System.out.print("E-mail: ");
            String email = scanner.nextLine().trim();
            System.out.print("Senha: ");
            String senha = scanner.nextLine().trim();
            realizarLoginOuCadastroAutomatico(email, senha);
        } else {
            realizarLoginOuCadastroAutomatico("gestor.cli@esg.com.br", "123456");
        }
    }

    private void menuCadastrarSetor(Scanner scanner) {
        System.out.println("\n--- CADASTRAR NOVO SETOR MONITORADO (POST /setores) ---");
        System.out.print("Nome do Setor (Enter = 'Laboratorio de Eficiencia Solar'): ");
        String nome = lerOuPadrao(scanner, "Laboratorio de Eficiencia Solar");

        System.out.print("Localizacao (Enter = 'Bloco Sustentavel - Cobertura'): ");
        String localizacao = lerOuPadrao(scanner, "Bloco Sustentavel - Cobertura");

        System.out.print("Responsavel (Enter = 'Eng. Marcos Oliveira'): ");
        String responsavel = lerOuPadrao(scanner, "Eng. Marcos Oliveira");

        System.out.print("Limite Mensal em kWh (Enter = 5000.00): ");
        String limiteMensal = lerOuPadrao(scanner, "5000.00");

        System.out.print("Limite Diario em kWh (Enter = 180.00): ");
        String limiteDiario = lerOuPadrao(scanner, "180.00");

        String json = String.format("""
                {
                  "nome": "%s",
                  "localizacao": "%s",
                  "responsavel": "%s",
                  "limiteMensalKwh": %s,
                  "limiteDiarioKwh": %s,
                  "ativo": true
                }
                """, nome, localizacao, responsavel, limiteMensal, limiteDiario);

        enviarRequisicao("POST", "/setores", json);
    }

    private void menuAtualizarSetor(Scanner scanner) {
        System.out.println("\n--- ATUALIZAR LIMITES DO SETOR (PUT /setores/{id}) ---");
        System.out.print("ID do Setor a atualizar (Enter = 1): ");
        String id = lerOuPadrao(scanner, "1");

        System.out.print("Novo Nome do Setor (Enter = 'Data Center Principal - Otimizado'): ");
        String nome = lerOuPadrao(scanner, "Data Center Principal - Otimizado");

        System.out.print("Novo Limite Mensal kWh (Enter = 14000.00): ");
        String limiteMensal = lerOuPadrao(scanner, "14000.00");

        System.out.print("Novo Limite Diario kWh (Enter = 460.00): ");
        String limiteDiario = lerOuPadrao(scanner, "460.00");

        String json = String.format("""
                {
                  "nome": "%s",
                  "localizacao": "Bloco A - Subsolo 1",
                  "responsavel": "Carlos Mendes",
                  "limiteMensalKwh": %s,
                  "limiteDiarioKwh": %s,
                  "ativo": true
                }
                """, nome, limiteMensal, limiteDiario);

        enviarRequisicao("PUT", "/setores/" + id, json);
    }

    private void menuCadastrarEquipamento(Scanner scanner) {
        System.out.println("\n--- CADASTRAR EQUIPAMENTO / SENSOR IOT (POST /equipamentos) ---");
        System.out.print("ID do Setor vinculado (Enter = 1): ");
        String setorId = lerOuPadrao(scanner, "1");

        String codigoPadrao = "IOT-SENS-" + (System.currentTimeMillis() % 10000);
        System.out.print("Codigo unico do Sensor IoT (Enter = '" + codigoPadrao + "'): ");
        String codigoSensor = lerOuPadrao(scanner, codigoPadrao);

        System.out.print("Nome do Equipamento (Enter = 'Ar Condicionado Inverter Sala Servidores'): ");
        String nome = lerOuPadrao(scanner, "Ar Condicionado Inverter Sala Servidores");

        System.out.print("Tipo (CLIMATIZACAO, ILUMINACAO, MOTOR_INDUSTRIAL) (Enter = 'CLIMATIZACAO'): ");
        String tipo = lerOuPadrao(scanner, "CLIMATIZACAO");

        System.out.print("Potencia Nominal em Watts (Enter = 6000.00): ");
        String potencia = lerOuPadrao(scanner, "6000.00");

        System.out.print("Limite de Consumo por Hora em kWh (Enter = 6.00): ");
        String limiteHora = lerOuPadrao(scanner, "6.00");

        String json = String.format("""
                {
                  "setorId": %s,
                  "codigoSensor": "%s",
                  "nome": "%s",
                  "tipoEquipamento": "%s",
                  "potenciaWatts": %s,
                  "limiteConsumoHoraKwh": %s,
                  "statusOperacional": "LIGADO",
                  "desligamentoAutomatico": true
                }
                """, setorId, codigoSensor, nome, tipo, potencia, limiteHora);

        enviarRequisicao("POST", "/equipamentos", json);
    }

    private void menuRegistrarLeituraConsumo(Scanner scanner) {
        System.out.println("\n--- REGISTRAR LEITURA DE CONSUMO E TESTAR REGRA IOT (POST /leituras-consumo) ---");
        System.out.println("Dica: Limite do Equipamento 2 = 5.50 kWh | Ociosidade >= 30 min desliga automaticamente o equipamento!");
        System.out.print("ID do Equipamento IoT (Enter = 2): ");
        String equipId = lerOuPadrao(scanner, "2");

        System.out.print("Consumo aferido em kWh (Enter = 8.900 para disparar Alerta Automatico): ");
        String consumo = lerOuPadrao(scanner, "8.900");

        System.out.print("Tempo Ocioso em minutos (Enter = 40 para acionar Desligamento Automatico IoT): ");
        String tempoOcioso = lerOuPadrao(scanner, "40");

        System.out.print("Observacao (Enter = 'Telemetria enviada via Menu Interativo VS Code'): ");
        String obs = lerOuPadrao(scanner, "Telemetria enviada via Menu Interativo VS Code");

        String json = String.format("""
                {
                  "equipamentoId": %s,
                  "consumoKwh": %s,
                  "tempoOciosoMinutos": %s,
                  "observacao": "%s"
                }
                """, equipId, consumo, tempoOcioso, obs);

        enviarRequisicao("POST", "/leituras-consumo", json);
    }

    private void menuDesligarEquipamentoOcioso(Scanner scanner) {
        System.out.println("\n--- DESLIGAMENTO REMOTO DE EQUIPAMENTO OCIOSO VIA IOT (PUT /equipamentos/{id}/desligar-ocioso) ---");
        System.out.print("ID do Equipamento para desligar remotamente (Enter = 1): ");
        String id = lerOuPadrao(scanner, "1");

        enviarRequisicao("PUT", "/equipamentos/" + id + "/desligar-ocioso", null);
    }

    private void menuHistoricoConsumo(Scanner scanner) {
        System.out.println("\n--- HISTORICO DE CONSUMO ENERGETICO ---");
        System.out.print("Deseja [1] Listar todas as leituras ou [2] Filtrar por Setor? (Enter = 1): ");
        String op = lerOuPadrao(scanner, "1");

        if ("2".equals(op)) {
            System.out.print("Informe o ID do Setor (Enter = 2): ");
            String setorId = lerOuPadrao(scanner, "2");
            enviarRequisicao("GET", "/leituras-consumo/setor/" + setorId, null);
        } else {
            enviarRequisicao("GET", "/leituras-consumo?page=0&size=10", null);
        }
    }

    private void menuAlertas(Scanner scanner) {
        System.out.println("\n--- GESTAO DE ALERTAS AUTOMATICOS DE EFICIENCIA ---");
        System.out.print("Escolha [1] Listar todos, [2] Listar apenas ABERTOS, [3] Resolver um Alerta (Enter = 1): ");
        String op = lerOuPadrao(scanner, "1");

        if ("2".equals(op)) {
            enviarRequisicao("GET", "/alertas?status=ABERTO", null);
        } else if ("3".equals(op)) {
            System.out.print("ID do Alerta a resolver (Enter = 1): ");
            String id = lerOuPadrao(scanner, "1");
            System.out.print("Acao corretiva executada (Enter = 'Reconfiguracao de curva termica e manutencao do sensor'): ");
            String acao = lerOuPadrao(scanner, "Reconfiguracao de curva termica e manutencao do sensor");

            String json = String.format("{\"acaoExecutada\": \"%s\"}", acao);
            enviarRequisicao("PUT", "/alertas/" + id + "/resolver", json);
        } else {
            enviarRequisicao("GET", "/alertas", null);
        }
    }

    private void executarDemonstracaoCompletaAutomatica() {
        System.out.println("\n################################################################################");
        System.out.println("   INICIANDO DEMONSTRACAO AUTOMATICA DE TODOS OS ENDPOINTS RESTFUL (ESG)");
        System.out.println("################################################################################");

        // 1. Autenticacao
        garantirAutenticacaoAutomatica();

        // 2. Listar Setores
        System.out.println("\n>>> PASSO 1/6: Listando Setores Monitorados (GET /setores)");
        enviarRequisicao("GET", "/setores?page=0&size=5", null);

        // 3. Cadastrar Novo Sensor IoT
        String sensorDemo = "IOT-DEMO-" + (System.currentTimeMillis() % 10000);
        System.out.println("\n>>> PASSO 2/6: Cadastrando Novo Equipamento/Sensor IoT (POST /equipamentos)");
        String novoEquipJson = String.format("""
                {
                  "setorId": 2,
                  "codigoSensor": "%s",
                  "nome": "Banco de Iluminacao Inteligente Demo",
                  "tipoEquipamento": "ILUMINACAO",
                  "potenciaWatts": 3500.00,
                  "limiteConsumoHoraKwh": 3.50,
                  "statusOperacional": "LIGADO",
                  "desligamentoAutomatico": true
                }
                """, sensorDemo);
        String respEquip = enviarRequisicao("POST", "/equipamentos", novoEquipJson);

        long equipIdCriado = 2L;
        try {
            if (respEquip != null) {
                JsonNode node = objectMapper.readTree(respEquip);
                if (node.has("id")) {
                    equipIdCriado = node.get("id").asLong();
                }
            }
        } catch (Exception ignored) {
        }

        // 4. Registrar Leitura de Consumo acima do limite e ocioso >= 30 min
        System.out.println("\n>>> PASSO 3/6: Registrando Leitura com Excesso de Consumo e Ociosidade >= 30 min (POST /leituras-consumo)");
        String leituraJson = String.format("""
                {
                  "equipamentoId": %d,
                  "consumoKwh": 6.800,
                  "tempoOciosoMinutos": 45,
                  "observacao": "Demonstracao automatica: consumo (6.80 kWh > 3.50 kWh) e 45 min ocioso"
                }
                """, equipIdCriado);
        enviarRequisicao("POST", "/leituras-consumo", leituraJson);

        // 5. Consultar Alertas Automaticos Gerados
        System.out.println("\n>>> PASSO 4/6: Consultando Alertas Automaticos Gerados (GET /alertas)");
        enviarRequisicao("GET", "/alertas", null);

        // 6. Consultar Historico de Leituras do Setor 2
        System.out.println("\n>>> PASSO 5/6: Consultando Historico de Consumo do Setor 2 (GET /leituras-consumo/setor/2)");
        enviarRequisicao("GET", "/leituras-consumo/setor/2", null);

        // 7. Consultar Painel de Indicadores ESG
        System.out.println("\n>>> PASSO 6/6: Consultando Painel Consolidado de Indicadores de Sustentabilidade (GET /indicadores-sustentabilidade)");
        enviarRequisicao("GET", "/indicadores-sustentabilidade", null);

        System.out.println("\n################################################################################");
        System.out.println("   DEMONSTRACAO COMPLETA CONCLUIDA COM SUCESSO!");
        System.out.println("################################################################################");
    }

    private void demonstrarValidacaoEExcecoes() {
        System.out.println("\n--- DEMONSTRANDO BEAN VALIDATION (@Valid) E GLOBAL EXCEPTION HANDLER ---");

        System.out.println("\n1) Enviando POST /setores com campos em branco e limites negativos (Deve retornar HTTP 400 Bad Request):");
        String jsonInvalido = """
                {
                  "nome": "",
                  "localizacao": "",
                  "responsavel": "",
                  "limiteMensalKwh": -100,
                  "limiteDiarioKwh": -50
                }
                """;
        enviarRequisicao("POST", "/setores", jsonInvalido);

        System.out.println("\n2) Buscando GET /setores/999999 inexistente (Deve retornar HTTP 404 Not Found):");
        enviarRequisicao("GET", "/setores/999999", null);
    }

    private String lerOuPadrao(Scanner scanner, String valorPadrao) {
        String lido = scanner.nextLine().trim();
        return lido.isEmpty() ? valorPadrao : lido;
    }

    private String enviarRequisicao(String metodo, String caminho, String jsonBody) {
        try {
            HttpResponse<String> response = dispararHttp(metodo, caminho, jsonBody);
            imprimirRespostaHttp(metodo, caminho, response);
            return response.body();
        } catch (Exception e) {
            System.out.println("[ERRO HTTP] Falha ao chamar " + metodo + " " + caminho + ": " + e.getMessage());
            return null;
        }
    }

    private HttpResponse<String> dispararHttp(String metodo, String caminho, String jsonBody) throws Exception {
        String url = "http://localhost:" + serverPort + caminho;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");

        if (jwtToken != null && !jwtToken.isBlank()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest.BodyPublisher publisher = (jsonBody != null && !jsonBody.isBlank())
                ? HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8)
                : HttpRequest.BodyPublishers.noBody();

        builder.method(metodo, publisher);
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private void imprimirRespostaHttp(String metodo, String caminho, HttpResponse<String> response) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("REQUISICAO : " + metodo + " http://localhost:" + serverPort + caminho);
        System.out.println("STATUS HTTP: " + response.statusCode());
        System.out.println("RESPOSTA JSON:");
        try {
            if (response.body() != null && !response.body().isBlank()) {
                Object json = objectMapper.readValue(response.body(), Object.class);
                String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                System.out.println(pretty);
            } else {
                System.out.println("(Sem conteudo no corpo - HTTP " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            System.out.println(response.body());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }
}
