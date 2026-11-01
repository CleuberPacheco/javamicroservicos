package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.LeituraConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LeituraConsumoRepository extends JpaRepository<LeituraConsumo, Long> {

    List<LeituraConsumo> findByEquipamentoSetorIdOrderByDataHoraLeituraDesc(Long setorId);

    List<LeituraConsumo> findByEquipamentoIdOrderByDataHoraLeituraDesc(Long equipamentoId);

    @Query("SELECT COALESCE(SUM(l.consumoKwh), 0) FROM LeituraConsumo l")
    BigDecimal somarConsumoTotalKwh();

    @Query("SELECT COALESCE(SUM(l.consumoKwh), 0) FROM LeituraConsumo l WHERE l.equipamento.setor.id = :setorId")
    BigDecimal somarConsumoPorSetorKwh(Long setorId);

    long countByDesligadoAutomaticamenteTrue();

    long countByUltrapassouLimiteTrue();
}
