package br.com.fiap.esg.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_LEITURA_CONSUMO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class LeituraConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_leitura")
    @SequenceGenerator(name = "seq_leitura", sequenceName = "SEQ_TB_LEITURA_CONSUMO", allocationSize = 1)
    @Column(name = "ID_LEITURA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUIPAMENTO", nullable = false)
    private EquipamentoIot equipamento;

    @Column(name = "CONSUMO_KWH", nullable = false, precision = 10, scale = 3)
    private BigDecimal consumoKwh;

    @Column(name = "TEMPO_OCIOSO_MINUTOS", nullable = false)
    private Integer tempoOciosoMinutos;

    @Column(name = "ULTRAPASSOU_LIMITE", nullable = false)
    private Boolean ultrapassouLimite;

    @Column(name = "DESLIGADO_AUTOMATICAMENTE", nullable = false)
    private Boolean desligadoAutomaticamente;

    @Column(name = "DATA_HORA_LEITURA", nullable = false)
    private LocalDateTime dataHoraLeitura;

    @Column(name = "OBSERVACAO", length = 255)
    private String observacao;

    @PrePersist
    public void prePersist() {
        if (this.tempoOciosoMinutos == null) {
            this.tempoOciosoMinutos = 0;
        }
        if (this.ultrapassouLimite == null) {
            this.ultrapassouLimite = false;
        }
        if (this.desligadoAutomaticamente == null) {
            this.desligadoAutomaticamente = false;
        }
        if (this.dataHoraLeitura == null) {
            this.dataHoraLeitura = LocalDateTime.now();
        }
    }
}
