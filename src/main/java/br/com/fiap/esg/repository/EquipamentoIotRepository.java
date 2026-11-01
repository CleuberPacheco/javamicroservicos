package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.EquipamentoIot;
import br.com.fiap.esg.model.StatusEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipamentoIotRepository extends JpaRepository<EquipamentoIot, Long> {
    Optional<EquipamentoIot> findByCodigoSensor(String codigoSensor);
    boolean existsByCodigoSensor(String codigoSensor);
    List<EquipamentoIot> findBySetorId(Long setorId);
    long countByStatusOperacional(StatusEquipamento statusOperacional);
}
