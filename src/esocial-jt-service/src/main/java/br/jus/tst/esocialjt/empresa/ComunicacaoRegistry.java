package br.jus.tst.esocialjt.empresa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import br.jus.tst.esocialjt.TipoAmbiente;
import br.jus.tst.esocialjt.certificado.Certificado;
import br.jus.tst.esocialjt.comunicacaogov.ComunicacaoParametros;
import br.jus.tst.esocialjt.comunicacaogov.ComunicacaoServico;

/** ComunicacaoServico por empresa: TLS mútuo com o certificado dela e URLs do ambiente dela. */
@Component
public class ComunicacaoRegistry {

	private static final class Entrada {
		final Certificado certificado;
		final TipoAmbiente ambiente;
		final ComunicacaoServico servico;

		Entrada(Certificado certificado, TipoAmbiente ambiente, ComunicacaoServico servico) {
			this.certificado = certificado;
			this.ambiente = ambiente;
			this.servico = servico;
		}
	}

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private EmpresaServico empresaServico;

	@Autowired
	private CertificadoRegistry certificadoRegistry;

	@Value("${esocialjt.ambiente}")
	private TipoAmbiente ambientePadrao;

	private final Map<Long, Entrada> cache = new ConcurrentHashMap<>();

	/** Lança EmpresaException se a empresa estiver inativa ou sem certificado ativo. */
	public ComunicacaoServico obter(Long codEmpresa) {
		Empresa empresa = empresaServico.obterAtiva(codEmpresa);
		Certificado certificado = certificadoRegistry.obter(codEmpresa);
		TipoAmbiente ambiente = ambienteDe(empresa);

		Entrada atual = cache.get(codEmpresa);
		if (atual != null && atual.certificado == certificado && atual.ambiente == ambiente) {
			return atual.servico;
		}
		ComunicacaoServico servico = criar(certificado, ambiente);
		cache.put(codEmpresa, new Entrada(certificado, ambiente, servico));
		return servico;
	}

	private TipoAmbiente ambienteDe(Empresa empresa) {
		if (empresa.getAmbiente() == null) {
			return ambientePadrao;
		}
		try {
			return TipoAmbiente.valueOf(empresa.getAmbiente());
		} catch (IllegalArgumentException e) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Ambiente inválido na empresa");
		}
	}

	private ComunicacaoServico criar(Certificado certificado, TipoAmbiente ambiente) {
		ComunicacaoServico servico = new ComunicacaoServico();
		servico.setMarshaller(marshaller);
		servico.setUnmarshaller(marshaller);

		HttpsUrlConnectionMessageSender sender = new HttpsUrlConnectionMessageSender();
		sender.setKeyManagers(certificado.getKeyManagers());
		sender.setTrustManagers(certificado.getTrustManagers());
		sender.setHostnameVerifier((hostname, session) -> true);
		servico.setMessageSender(sender);

		servico.setActionEnviarLoteGov(ComunicacaoParametros.ENVIAR_ACTION);
		servico.setActionConsultaLoteGov(ComunicacaoParametros.CONSULTAR_ACTION);
		if (ambiente == TipoAmbiente.PRODUCAO) {
			servico.setUrlEnviarLoteGov(ComunicacaoParametros.URL_ENVIAR_LOTE_EVENTOS_PRODUCAO);
			servico.setUrlConsultaLoteGov(ComunicacaoParametros.URL_CONSULTAR_LOTE_EVENTOS_PRODUCAO);
		} else {
			servico.setUrlEnviarLoteGov(ComunicacaoParametros.URL_ENVIAR_LOTE_EVENTOS_PRODUCAO_RESTRITA);
			servico.setUrlConsultaLoteGov(ComunicacaoParametros.URL_CONSULTAR_LOTE_EVENTOS_PRODUCAO_RESTRITA);
		}
		return servico;
	}
}
