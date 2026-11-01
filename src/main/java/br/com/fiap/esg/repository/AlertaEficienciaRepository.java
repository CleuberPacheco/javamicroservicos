package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.AlertaEficiencia;
import br.com.fiap.esg.model.StatusAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaEficienciaRepository extends JpaRepository<AlertaEficiencia, Long> {
    List<AlertaEficiencia> findByStatusAlertaOrderByDataGeracaoDesc(StatusAlerta statusAlerta);
    List<AlertaEficiencia> findBySetorIdOrderByDataGeracaoDesc(Long setorId);
    long countByStatusAlerta(StatusAlerta statusAlerta);
}
