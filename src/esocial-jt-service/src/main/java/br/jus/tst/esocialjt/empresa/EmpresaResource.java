package br.jus.tst.esocialjt.empresa;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/empresas")
public class EmpresaResource {

	@Autowired
	private EmpresaServico empresaServico;

	@Autowired
	private CertificadoEmpresaServico certificadoServico;

	@Operation(summary = "Busca empresas por CNPJ ou razão social (trecho). Sem parâmetro, lista todas.")
	@GetMapping
	public List<EmpresaDTO> listar(@RequestParam(value = "busca", required = false) String busca,
			@RequestParam(value = "somenteAtivas", defaultValue = "false") boolean somenteAtivas) {
		return empresaServico.listar(busca, somenteAtivas);
	}

	@GetMapping("/{id}")
	public EmpresaDTO buscar(@PathVariable("id") Long id) {
		return empresaServico.buscar(id);
	}

	@GetMapping("/cnpj/{cnpj}")
	public EmpresaDTO buscarPorCnpj(@PathVariable("cnpj") String cnpj) {
		return empresaServico.buscarPorCnpj(cnpj);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmpresaDTO criar(@Valid @RequestBody EmpresaDTO dto) {
		return empresaServico.criar(dto);
	}

	@PutMapping("/{id}")
	public EmpresaDTO atualizar(@PathVariable("id") Long id, @Valid @RequestBody EmpresaDTO dto) {
		return empresaServico.atualizar(id, dto);
	}

	@Operation(summary = "Envia o certificado .pfx (A1) da empresa. O certificado ativo anterior é desativado.")
	@PostMapping(path = "/{id}/certificados", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public CertificadoEmpresaDTO enviarCertificado(@PathVariable("id") Long id,
			@RequestParam("arquivo") MultipartFile arquivo, @RequestParam("senha") String senha) {
		return certificadoServico.enviar(id, arquivo, senha);
	}

	@GetMapping("/{id}/certificados")
	public List<CertificadoEmpresaDTO> listarCertificados(@PathVariable("id") Long id) {
		return certificadoServico.listar(id);
	}

	@DeleteMapping("/{id}/certificados/{codCertificado}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void desativarCertificado(@PathVariable("id") Long id, @PathVariable("codCertificado") Long codCertificado) {
		certificadoServico.desativar(id, codCertificado);
	}
}
