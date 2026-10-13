package br.jus.tst.esocialjt.empresa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.jus.tst.esocialjt.certificado.Certificado;

/** Fornece o Certificado ativo de cada empresa, reutilizando o já carregado enquanto o certificado não mudar. */
@Component
public class CertificadoRegistry {

	private static final class Entrada {
		final long codCertificado;
		final Certificado certificado;

		Entrada(long codCertificado, Certificado certificado) {
			this.codCertificado = codCertificado;
			this.certificado = certificado;
		}
	}

	@Autowired
	private CertificadoEmpresaServico servico;

	@Autowired
	private CriptografiaSenha criptografia;

	@Value("${esocialjt.arquivoCacerts:}")
	private String arquivoCacerts;

	@Value("${esocialjt.senhaCacerts:changeit}")
	private String senhaCacerts;

	private final Map<Long, Entrada> cache = new ConcurrentHashMap<>();

	/** Lança EmpresaException se a empresa não tiver certificado ativo. */
	public Certificado obter(Long codEmpresa) {
		CertificadoEmpresa ativo = servico.obterAtivo(codEmpresa);
		Entrada atual = cache.get(codEmpresa);
		if (atual != null && atual.codCertificado == ativo.getId()) {
			return atual.certificado;
		}
		Certificado novo = Certificado.deBytes(ativo.getArquivo(), arquivoCacerts,
				criptografia.descriptografar(ativo.getSenhaCriptografada()).toCharArray(),
				senhaCacerts.toCharArray(), ativo.getTipo(), ativo.getAlias());
		cache.put(codEmpresa, new Entrada(ativo.getId(), novo));
		return novo;
	}
}
