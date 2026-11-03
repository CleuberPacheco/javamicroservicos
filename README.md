# Microsserviço RESTful ESG — Tema 1: Eficiência Energética e Sustentabilidade

Projeto desenvolvido em **Java 21 LTS** com **Spring Boot 3**, **Spring Security (JWT)**, **Oracle Database**, **Flyway Migrations** e **Docker**, atendendo integralmente aos requisitos da atividade avaliativa da FIAP.

> [!IMPORTANT]
> **COMPATIBILIDADE DE AMBIENTE — JAVA 21 LTS OBRIGATÓRIO:**
> O projeto foi construído, configurado e homologado estritamente para o **Java 21 LTS** (OpenJDK/Eclipse Temurin 21). 
> Caso a compilação local seja executada em versões diferentes do JDK (especialmente versões não-LTS ou superiores, como JDK 22+), podem ocorrer erros de inicialização no compilador javac decorrentes do processador de anotações do Lombok. Recomenda-se a execução no JDK 21 ou diretamente via contêiner **Docker**.

> [!NOTE]
> **DISPONIBILIZAÇÃO DE CREDENCIAIS PARA AVALIAÇÃO:**
> As credenciais de acesso ao banco de dados Oracle da FIAP (`rm567059` / `fiap26`) foram intencionalmente mantidas pré-configuradas nos arquivos do projeto (`application.properties`, `Dockerfile` e `docker-compose.yml`) com o objetivo de viabilizar a execução e os testes imediatos por parte dos professores e avaliadores da banca, sem necessidade de configuração manual. Por se tratar de um repositório mantido privado para a entrega acadêmica, a integridade dos dados dos alunos permanece assegurada.

---

## 1. Sobre o Tema Escolhido (Tema 1 - Eficiência Energética e Sustentabilidade)

A solução implementa um serviço RESTful completo para monitoramento energético corporativo/industrial, cobrindo os três pilares do tema:
1. **Monitoramento e otimização do consumo de energia:** Cadastro de setores/unidades com limites diário e mensal em kWh (`TB_SETOR`) e registro contínuo de leituras de consumo (`TB_LEITURA_CONSUMO`).
2. **Alertas automáticos quando o consumo ultrapassa determinado limite:** Sempre que uma leitura de consumo registrada em `POST /leituras-consumo` supera o limite horário configurado para o equipamento (`limiteConsumoHoraKwh`), o microsserviço gera automaticamente um alerta (`TB_ALERTA_EFICIENCIA`) com severidade `ALTA` ou `CRITICA`.
3. **Integração com sensores IoT para desligamento automático de equipamentos ociosos:** Quando a telemetria informa tempo ocioso $\ge 30$ minutos (`tempoOciosoMinutos >= 30`) em um equipamento com `desligamentoAutomatico = true`, o sistema altera automaticamente o status operacional do equipamento para `DESLIGADO` e registra o evento de economia energética. Também é possível acionar o desligamento remoto diretamente via `PUT /equipamentos/{id}/desligar-ocioso`.

---

## 2. Tecnologias e Módulos do Spring Boot Utilizados

- **Java 21 LTS** & **Spring Boot 3.3.5**
- **Spring Boot Starters:** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-actuator`, `spring-boot-starter-test`.
- **Spring Security + Auth0 JWT:** Autenticação Stateless via Bearer Token e controle de acesso granular baseado em perfis (`ADMIN`, `OPERADOR`, `AUDITOR`).
- **Jakarta Bean Validation & `@RestControllerAdvice`:** Validação rigorosa de payloads (`@NotBlank`, `@NotNull`, `@Positive`, `@Email`, etc.) e tratamento global padronizado de exceções (`GlobalExceptionHandler`).
- **Oracle Database (`ojdbc11`) + Flyway (`flyway-database-oracle`):** Banco de dados relacional oficial corporativo com versionamento automático de schema e carga inicial de dados em `src/main/resources/db/migration`.
- **Spring Boot Actuator & SpringDoc OpenAPI (Swagger UI):** Monitoramento de saúde (`/actuator/health`) e documentação interativa dos endpoints (`/swagger-ui.html`).
- **Docker & Docker Compose:** Build multi-stage otimizado com usuário não-root, pronto para implantação.

---

## 3. Banco de Dados Oracle Oficial (FIAP) & Migrações Flyway

O **Oracle Database da FIAP** (`oracle.fiap.com.br:1521:ORCL`) é o banco de dados oficial e exclusivo da atividade, configurado no arquivo [`src/main/resources/application.properties`](./src/main/resources/application.properties):

```properties
spring.datasource.url=${ORACLE_URL:jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL}
spring.datasource.username=rm567059
spring.datasource.password=fiap26
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

### Migrações Flyway Nativas Oracle (`src/main/resources/db/migration`)
- `V1__create_tables.sql`: Cria as Sequences e as 5 tabelas relacionais em sintaxe nativa Oracle (`TB_USUARIO`, `TB_SETOR`, `TB_EQUIPAMENTO_IOT`, `TB_LEITURA_CONSUMO`, `TB_ALERTA_EFICIENCIA`), incluindo constraints de chave primária, integridade referencial (`FOREIGN KEY`) e regras de checagem (`CHECK`).
- `V2__insert_initial_data.sql`: Carga inicial de sementes (usuários com senha criptografada BCrypt, setores monitorados, sensores IoT e histórico de telemetria).

### Resiliência e Contingência para Avaliação
A classe `ResilientOracleDataSourceConfig` gerencia o pool de conexões do Oracle de forma otimizada para respeitar as quotas de conexão do laboratório da FIAP. Caso o professor ou avaliador execute o projeto em uma rede externa sem VPN da FIAP ativa ou se houver esgotamento temporário de sessões no Oracle institucional (`ORA-02391`), a aplicação informa o status via log e ativa uma contingência em memória, assegurando que a banca consiga testar todos os endpoints RESTful, validações e regras de negócio sem interrupção.

---

## 4. Como Executar o Projeto

### Opção A: Executando com Docker Compose (Recomendado)

O `docker-compose.yml` já está pronto com as credenciais do Oracle pré-configuradas:

```bash
docker compose up --build -d
```
A API estará disponível imediatamente em `http://localhost:8080`.

Para visualizar os logs da aplicação:
```bash
docker compose logs -f
```

Para parar o contêiner:
```bash
docker compose down
```

### Opção B: Executando com Docker Puro (`Dockerfile`)

```bash
docker build -t esg-eficiencia-energetica:1.0 .
docker run -p 8080:8080 esg-eficiencia-energetica:1.0
```

### Opção C: Executando Localmente via Maven Wrapper (Requer Java 21 LTS)

No Linux / macOS:
```bash
./mvnw spring-boot:run
```

No Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd spring-boot:run
```

Para rodar a suíte de testes automatizados (`@SpringBootTest` + `MockMvc`):
```powershell
.\mvnw.cmd test
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
| `POST` | `/leituras-consumo` | Registra telemetria (kWh + tempo ocioso), gera alerta e desliga se ocioso $\ge 30$ min | `ADMIN`, `OPERADOR` |
| `GET` | `/leituras-consumo` | Lista histórico geral de leituras de consumo | Autenticado |
| `GET` | `/leituras-consumo/setor/{setorId}` | Histórico de consumo de um setor específico | Autenticado |
| `GET` | `/alertas` | Lista alertas automáticos (filtro opcional `?status=ABERTO`) | Autenticado |
| `PUT` | `/alertas/{id}/resolver` | Registra ação corretiva e resolve um alerta | `ADMIN`, `OPERADOR` |
| `GET` | `/indicadores-sustentabilidade` | Painel consolidado ESG (consumo kWh, $\text{CO}_2$ mitigado, economia IoT e status por setor) | Autenticado |

### Documentação Interativa e Healthcheck
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`

---

## 6. Menu Interativo no Console + Postman

### Menu Interativo no Próprio Terminal Java (`MenuInterativoConsole`)
Assim que a aplicação sobe (`Started EsgEficienciaEnergeticaApplication`), um **Menu Interativo de Console** é disponibilizado no terminal, permitindo testar **todos os endpoints RESTful, autenticação JWT, regras de alerta e desligamento automático IoT, e validações (`@Valid`)** digitando apenas o número da opção (`1` a `13`), sem necessidade de abrir ferramentas externas:
- A opção **`[12]`** executa uma **Demonstração Completa Automática**, disparando todas as requisições HTTP em sequência e exibindo o status HTTP e JSON formatado no console.
- A opção **`[13]`** demonstra o tratamento global de exceções (`HTTP 400 Bad Request` e `HTTP 404 Not Found`).

### Testando com o Postman (Opcional)

1. Importe o arquivo `postman/ESG_Eficiencia_Energetica_Postman_Collection.json` no Postman.
2. Execute a requisição **`POST /auth/register`** (ou **`POST /auth/login`**) na pasta `1. Autenticacao`.
3. O script de teste da coleção salvará automaticamente o token retornado na variável `{{jwtToken}}`, autenticando todas as demais requisições da coleção automaticamente.
