package br.com.fiap.esg.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Slf4j
@Configuration
@Profile("!test")
public class ResilientOracleDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String oracleUrl;

    @Value("${spring.datasource.username}")
    private String oracleUsername;

    @Value("${spring.datasource.password}")
    private String oraclePassword;

    @Value("${spring.datasource.driver-class-name:oracle.jdbc.OracleDriver}")
    private String oracleDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            HikariConfig oracleConfig = new HikariConfig();
            oracleConfig.setJdbcUrl(oracleUrl);
            oracleConfig.setUsername(oracleUsername);
            oracleConfig.setPassword(oraclePassword);
            oracleConfig.setDriverClassName(oracleDriver);
            // Usa apenas 1 sessao unica para nunca estourar o SESSIONS_PER_USER (ORA-02391) da FIAP
            oracleConfig.setMaximumPoolSize(1);
            oracleConfig.setMinimumIdle(1);
            oracleConfig.setIdleTimeout(20000);
            oracleConfig.setMaxLifetime(45000);
            oracleConfig.setConnectionTimeout(8000);

            HikariDataSource ds = new HikariDataSource(oracleConfig);
            log.info("Conectado com sucesso ao Oracle Database ({}) com o usuario {}.", oracleUrl, oracleUsername);
            return ds;

        } catch (Exception ex) {
            log.warn("================================================================================");
            log.warn("AVISO ORACLE FIAP: {}", ex.getMessage() != null ? ex.getMessage().trim() : "Limite de sessoes");
            log.warn("Ativando contingencia em Modo Compatibilidade Oracle (Flyway V1 + V2)");
            log.warn("para que a API e o Menu Interativo subam imediatamente sem erro!");
            log.warn("================================================================================");

            HikariConfig fallbackConfig = new HikariConfig();
            fallbackConfig.setJdbcUrl("jdbc:h2:mem:esg_oracle_contingency;MODE=Oracle;DB_CLOSE_DELAY=-1");
            fallbackConfig.setUsername("sa");
            fallbackConfig.setPassword("");
            fallbackConfig.setDriverClassName("org.h2.Driver");
            fallbackConfig.setMaximumPoolSize(2);
            return new HikariDataSource(fallbackConfig);
        }
    }
}
