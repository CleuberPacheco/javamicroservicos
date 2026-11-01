package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findByAtivoTrue();
}
