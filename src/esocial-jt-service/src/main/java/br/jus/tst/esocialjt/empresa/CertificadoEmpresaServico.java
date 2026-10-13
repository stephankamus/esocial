package br.jus.tst.esocialjt.empresa;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class CertificadoEmpresaServico {

	@Autowired
	private CertificadoEmpresaRepository repository;

	@Autowired
	private EmpresaServico empresaServico;

	@Autowired
	private CriptografiaSenha criptografia;

	/** Valida o .pfx, desativa o certificado ativo anterior e grava o novo com a senha criptografada. */
	public CertificadoEmpresaDTO enviar(Long codEmpresa, MultipartFile arquivo, String senha) {
		Empresa empresa = empresaServico.buscarEntidade(codEmpresa);
		if (arquivo == null || arquivo.isEmpty() || senha == null || senha.isEmpty()) {
			throw new EmpresaException(HttpStatus.BAD_REQUEST, "Arquivo e senha são obrigatórios");
		}
		byte[] bytes;
		try {
			bytes = arquivo.getBytes();
		} catch (IOException e) {
			throw new EmpresaException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo enviado");
		}
		ValidadorPfx.Resultado info = ValidadorPfx.validar(bytes, senha);
		if (info.cnpj != null && !info.cnpj.equals(empresa.getCnpj())) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY,
					"O CNPJ do certificado (" + info.cnpj + ") difere do CNPJ da empresa");
		}

		repository.findByEmpresaIdAndAtivoTrue(codEmpresa).forEach(c -> c.setAtivo(false));

		CertificadoEmpresa novo = new CertificadoEmpresa();
		novo.setEmpresa(empresa);
		novo.setArquivo(bytes);
		novo.setSenhaCriptografada(criptografia.criptografar(senha));
		novo.setAlias(info.alias);
		novo.setCnpjCertificado(info.cnpj);
		novo.setDtaValidade(info.validade);
		return CertificadoEmpresaDTO.de(repository.save(novo));
	}

	@Transactional(readOnly = true)
	public List<CertificadoEmpresaDTO> listar(Long codEmpresa) {
		empresaServico.buscarEntidade(codEmpresa);
		return repository.findByEmpresaIdOrderByIdDesc(codEmpresa).stream().map(CertificadoEmpresaDTO::de)
				.collect(Collectors.toList());
	}

	public void desativar(Long codEmpresa, Long codCertificado) {
		CertificadoEmpresa c = repository.findById(codCertificado)
				.filter(x -> x.getEmpresa().getId().equals(codEmpresa))
				.orElseThrow(() -> new EmpresaException(HttpStatus.NOT_FOUND, "Certificado não encontrado"));
		c.setAtivo(false);
	}

	/** Certificado ativo da empresa, para uso na assinatura e na comunicação com o governo. */
	@Transactional(readOnly = true)
	public CertificadoEmpresa obterAtivo(Long codEmpresa) {
		return repository.findFirstByEmpresaIdAndAtivoTrueOrderByIdDesc(codEmpresa).orElseThrow(
				() -> new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Empresa sem certificado ativo"));
	}
}
