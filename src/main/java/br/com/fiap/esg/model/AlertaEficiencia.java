package br.com.fiap.esg.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_ALERTA_EFICIENCIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AlertaEficiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_alerta")
    @SequenceGenerator(name = "seq_alerta", sequenceName = "SEQ_TB_ALERTA_EFICIENCIA", allocationSize = 1)
    @Column(name = "ID_ALERTA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SETOR", nullable = false)
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUIPAMENTO")
    private EquipamentoIot equipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_LEITURA")
    private LeituraConsumo leitura;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_ALERTA", nullable = false, length = 60)
    private TipoAlerta tipoAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "SEVERIDADE", nullable = false, length = 20)
    private SeveridadeAlerta severidade;

    @Column(name = "MENSAGEM", nullable = false, length = 400)
    private String mensagem;

    @Column(name = "ACAO_AUTOMATICA_EXECUTADA", length = 255)
    private String acaoAutomaticaExecutada;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS_ALERTA", nullable = false, length = 20)
    private StatusAlerta statusAlerta;

    @Column(name = "DATA_GERACAO", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "DATA_RESOLUCAO")
    private LocalDateTime dataResolucao;

    @PrePersist
    public void prePersist() {
        if (this.statusAlerta == null) {
            this.statusAlerta = StatusAlerta.ABERTO;
        }
        if (this.dataGeracao == null) {
            this.dataGeracao = LocalDateTime.now();
        }
    }
}
