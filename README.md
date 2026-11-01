# Microsserviço RESTful ESG — Tema 1: Eficiência Energética e Sustentabilidade

Projeto desenvolvido em **Java 21** com **Spring Boot 3**, **Spring Security (JWT)**, **Oracle Database**, **Flyway Migrations** e **Docker**, atendendo integralmente aos requisitos da atividade avaliativa de ESG.

---

## 1. Sobre o Tema Escolhido (Tema 1 - Eficiência Energética e Sustentabilidade)

A solução implementa um serviço RESTful completo para monitoramento energético corporativo/industrial, cobrindo os três pilares do tema:
1. **Monitoramento e otimização do consumo de energia:** Cadastro de setores/unidades com limites diário e mensal em kWh (`TB_SETOR`) e registro contínuo de leituras de consumo (`TB_LEITURA_CONSUMO`).
2. **Alertas automáticos quando o consumo ultrapassa determinado limite:** Sempre que uma leitura de consumo registrada em `POST /leituras-consumo` supera o limite horário configurado para o equipamento (`limiteConsumoHoraKwh`), o microsserviço gera automaticamente um alerta (`TB_ALERTA_EFICIENCIA`) com severidade `ALTA` ou `CRITICA`.
3. **Integração com sensores IoT para desligamento automático de equipamentos ociosos:** Quando a telemetria informa tempo ocioso $\ge 30$ minutos (`tempoOciosoMinutos >= 30`) em um equipamento com `desligamentoAutomatico = true`, o sistema altera automaticamente o status operacional do equipamento para `DESLIGADO` e registra o evento de economia energética. Também é possível acionar o desligamento remoto diretamente via `PUT /equipamentos/{id}/desligar-ocioso`.

---

## 2. Tecnologias e Módulos do Spring Boot Utilizados

- **Java 21 LTS** & **Spring Boot 3.3.5**
- **Spring Boot Starters (`web`, `data-jpa`, `validation`, `security`, `actuator`, `test`)**
- **Spring Security + Auth0 JWT:** Autenticação Stateless via Bearer Token e controle de acesso baseado em perfis (`ADMIN`, `OPERADOR`, `AUDITOR`).
- **Jakarta Bean Validation & `@RestControllerAdvice`:** Validação rigorosa de payloads (`@NotBlank`, `@NotNull`, `@Positive`, `@Email`, etc.) e tratamento global padronizado de exceções (`GlobalExceptionHandler`).
- **Oracle Database (`ojdbc11`) + Flyway (`flyway-database-oracle`):** Versionamento automático de schema e carga inicial de dados em `src/main/resources/db/migration`.
- **Spring Boot Actuator & SpringDoc OpenAPI (Swagger UI):** Monitoramento de saúde (`/actuator/health`) e documentação interativa (`/swagger-ui.html`).
- **Docker & Docker Compose:** Build multi-stage otimizado e pronto para implantação.

---

## 3. Configuração do Banco de Dados Oracle (FIAP)

No arquivo [`src/main/resources/application.properties`](./src/main/resources/application.properties), a conexão já está configurada para o banco Oracle da FIAP (`oracle.fiap.com.br`):

```properties
spring.datasource.url=${ORACLE_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL}
spring.datasource.username=${ORACLE_USER:rmXXXXXX}
spring.datasource.password=${ORACLE_PASSWORD:XXXXXX}
```

> **Importante:** Antes de executar, substitua `rmXXXXXX` e `XXXXXX` pelo seu usuário e senha do Oracle da FIAP diretamente no `application.properties` **ou** informe-os via variáveis de ambiente (`ORACLE_USER` e `ORACLE_PASSWORD`).

### Migrações Flyway (`src/main/resources/db/migration`)
- `V1__create_tables.sql`: Cria as sequences e as 5 tabelas relacionais no Oracle (`TB_USUARIO`, `TB_SETOR`, `TB_EQUIPAMENTO_IOT`, `TB_LEITURA_CONSUMO`, `TB_ALERTA_EFICIENCIA`).
- `V2__insert_initial_data.sql`: Insere usuários iniciais, setores, sensores IoT, leituras e alertas para testes imediatos.

---

## 4. Como Executar o Projeto

### Opção A: Executando com Docker Compose (Recomendado)

1. Exporte suas credenciais do Oracle (ou edite no `docker-compose.yml` / `application.properties`):
   ```bash
   export ORACLE_USER=rm123456
   export ORACLE_PASSWORD=suasenha
   ```
   *(No PowerShell do Windows: `$env:ORACLE_USER="rm123456"; $env:ORACLE_PASSWORD="suasenha"`)*

2. Suba o contêiner com Docker Compose:
   ```bash
   docker compose up --build -d
   ```

3. A API estará disponível em `http://localhost:8080`.

### Opção B: Executando com Docker Puro (`Dockerfile`)

```bash
docker build -t esg-eficiencia-energetica:1.0 .
docker run -p 8080:8080 \
  -e ORACLE_USER=rm123456 \
  -e ORACLE_PASSWORD=suasenha \
  esg-eficiencia-energetica:1.0
```

### Opção C: Executando Localmente via Maven Wrapper

```bash
./mvnw spring-boot:run
```
Para rodar a suíte de testes automatizados (`@SpringBootTest` + `MockMvc`):
```bash
./mvnw test
```

---

## 5. Endpoints RESTful Disponíveis

| Método | Endpoint | Descrição | Permissão |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/register` | Cadastra novo usuário e retorna Token JWT | Público |
| `POST` | `/auth/login` | Autentica usuário e retorna Token JWT | Público |
| `POST` | `/setores` | Cadastra novo setor com limites diário/mensal (kWh) | `ADMIN`, `OPERADOR` |
| `GET` | `/setores` | Lista setores monitorados (paginado) | Autenticado |
| `GET` | `/setores/{id}` | Busca detalhes de um setor específico | Autenticado |
| `PUT` | `/setores/{id}` | Atualiza dados e metas de consumo do setor | `ADMIN`, `OPERADOR` |
| `DELETE` | `/setores/{id}` | Remove um setor cadastrado | `ADMIN` |
| `POST` | `/equipamentos` | Cadastra equipamento/sensor IoT em um setor | `ADMIN`, `OPERADOR` |
| `GET` | `/equipamentos` | Lista equipamentos IoT e seu status operacional | Autenticado |
| `GET` | `/equipamentos/{id}` | Busca equipamento IoT por ID | Autenticado |
| `PUT` | `/equipamentos/{id}` | Atualiza parâmetros e limites do equipamento IoT | `ADMIN`, `OPERADOR` |
| `PUT` | `/equipamentos/{id}/desligar-ocioso` | Aciona desligamento remoto de equipamento ocioso via IoT | `ADMIN`, `OPERADOR` |
| `DELETE` | `/equipamentos/{id}` | Remove equipamento IoT | `ADMIN` |
| `POST` | `/leituras-consumo` | Registra leitura (kWh + tempo ocioso), gera alerta e desliga se ocioso $\ge 30$ min | `ADMIN`, `OPERADOR` |
| `GET` | `/leituras-consumo` | Lista histórico geral de leituras de consumo | Autenticado |
| `GET` | `/leituras-consumo/setor/{setorId}` | Histórico de consumo de um setor específico | Autenticado |
| `GET` | `/alertas` | Lista alertas automáticos (filtro opcional `?status=ABERTO`) | Autenticado |
| `PUT` | `/alertas/{id}/resolver` | Registra ação corretiva e resolve um alerta | `ADMIN`, `OPERADOR` |
| `GET` | `/indicadores-sustentabilidade` | Painel consolidado ESG (consumo kWh, $\text{CO}_2$, economia IoT e status por setor) | Autenticado |

### Documentação Interativa e Healthcheck
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`

---

## 6. Menu Interativo no Console (VS Code) + Postman

### Menu Interativo no Próprio Terminal Java (`MenuInterativoConsole`)
Assim que a aplicação sobe (`Started EsgEficienciaEnergeticaApplication`), um **Menu Interativo de Console** é exibido automaticamente no terminal do VS Code, permitindo ao professor/avaliador testar **todos os endpoints RESTful, autenticação JWT, regras de alerta e desligamento automático IoT, e validações (`@Valid`)** digitando apenas o número da opção (`1` a `13`), sem precisar abrir o Postman!
- A opção **`[12]`** executa uma **Demonstração Completa Automática** disparando todas as requisições HTTP em sequência e imprimindo o Status HTTP e o JSON formatado no console.
- A opção **`[13]`** demonstra o tratamento global de exceções (`HTTP 400 Bad Request` e `HTTP 404 Not Found`).

### Testando com o Postman (Opcional)

1. Importe o arquivo `postman/ESG_Eficiencia_Energetica_Postman_Collection.json` no Postman.
2. Execute a requisição **`POST /auth/register`** (ou **`POST /auth/login`**) na pasta `1. Autenticacao`.
3. O script de teste da coleção salvará automaticamente o token retornado na variável `{{jwtToken}}`, autenticando todas as demais requisições da coleção automaticamente.
