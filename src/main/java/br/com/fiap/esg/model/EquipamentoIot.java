package br.com.fiap.esg.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_EQUIPAMENTO_IOT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class EquipamentoIot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_equipamento")
    @SequenceGenerator(name = "seq_equipamento", sequenceName = "SEQ_TB_EQUIPAMENTO_IOT", allocationSize = 1)
    @Column(name = "ID_EQUIPAMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @Column(name = "CODIGO_SENSOR", nullable = false, unique = true, length = 50)
    private String codigoSensor;

    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @Column(name = "TIPO_EQUIPAMENTO", nullable = false, length = 60)
    private String tipoEquipamento;

    @Column(name = "POTENCIA_WATTS", nullable = false, precision = 10, scale = 2)
    private BigDecimal potenciaWatts;

    @Column(name = "LIMITE_CONSUMO_HORA_KWH", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteConsumoHoraKwh;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_OPERACIONAL", nullable = false, length = 30)
    private StatusEquipamento statusOperacional;

    @Column(name = "DESLIGAMENTO_AUTOMATICO", nullable = false)
    private Boolean desligamentoAutomatico;

    @Column(name = "ULTIMA_ATUALIZACAO", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @PrePersist
    public void prePersist() {
        if (this.statusOperacional == null) {
            this.statusOperacional = StatusEquipamento.LIGADO;
        }
        if (this.desligamentoAutomatico == null) {
            this.desligamentoAutomatico = true;
        }
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }
}
