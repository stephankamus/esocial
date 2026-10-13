package br.jus.tst.esocialjt.empresa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

	Optional<Empresa> findByCnpj(String cnpj);

	boolean existsByCnpj(String cnpj);

	@Query("select e from Empresa e where e.cnpj like concat('%', :busca, '%') "
			+ "or lower(e.razaoSocial) like lower(concat('%', :busca, '%')) order by e.razaoSocial")
	List<Empresa> buscar(@Param("busca") String busca);
}
