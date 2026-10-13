package br.jus.tst.esocialjt.empresa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificadoEmpresaRepository extends JpaRepository<CertificadoEmpresa, Long> {

	Optional<CertificadoEmpresa> findFirstByEmpresaIdAndAtivoTrueOrderByIdDesc(Long empresaId);

	List<CertificadoEmpresa> findByEmpresaIdOrderByIdDesc(Long empresaId);

	List<CertificadoEmpresa> findByEmpresaIdAndAtivoTrue(Long empresaId);
}
