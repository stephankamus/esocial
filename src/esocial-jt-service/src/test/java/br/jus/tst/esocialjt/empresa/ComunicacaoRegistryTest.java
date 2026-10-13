package br.jus.tst.esocialjt.empresa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.util.ReflectionTestUtils;

import br.jus.tst.esocialjt.TipoAmbiente;
import br.jus.tst.esocialjt.certificado.Certificado;
import br.jus.tst.esocialjt.comunicacaogov.ComunicacaoParametros;
import br.jus.tst.esocialjt.comunicacaogov.ComunicacaoServico;

public class ComunicacaoRegistryTest {

	private ComunicacaoRegistry registry;
	private EmpresaServico empresaServico;
	private CertificadoRegistry certificadoRegistry;
	private Certificado cert1;
	private Certificado cert2;

	@Before
	public void setUp() {
		empresaServico = mock(EmpresaServico.class);
		certificadoRegistry = mock(CertificadoRegistry.class);
		cert1 = mock(Certificado.class);
		cert2 = mock(Certificado.class);
		registry = new ComunicacaoRegistry();
		ReflectionTestUtils.setField(registry, "marshaller", new Jaxb2Marshaller());
		ReflectionTestUtils.setField(registry, "empresaServico", empresaServico);
		ReflectionTestUtils.setField(registry, "certificadoRegistry", certificadoRegistry);
		ReflectionTestUtils.setField(registry, "ambientePadrao", TipoAmbiente.PRODUCAO_RESTRITA);
		when(certificadoRegistry.obter(1L)).thenReturn(cert1);
		when(certificadoRegistry.obter(2L)).thenReturn(cert2);
	}

	private Empresa empresa(String ambiente) {
		Empresa e = new Empresa();
		e.setAmbiente(ambiente);
		return e;
	}

	@Test
	public void deveCriarUmServicoPorEmpresaEReutilizar() {
		when(empresaServico.obterAtiva(anyLong())).thenReturn(empresa(null));
		ComunicacaoServico s1 = registry.obter(1L);
		ComunicacaoServico s2 = registry.obter(2L);
		assertNotSame(s1, s2);
		assertSame(s1, registry.obter(1L));
	}

	@Test
	public void deveUsarAmbienteDaEmpresaOuOPadrao() {
		when(empresaServico.obterAtiva(1L)).thenReturn(empresa("PRODUCAO"));
		when(empresaServico.obterAtiva(2L)).thenReturn(empresa(null));
		assertEquals(ComunicacaoParametros.URL_ENVIAR_LOTE_EVENTOS_PRODUCAO, registry.obter(1L).getUrlEnviarLoteGov());
		assertEquals(ComunicacaoParametros.URL_ENVIAR_LOTE_EVENTOS_PRODUCAO_RESTRITA,
				registry.obter(2L).getUrlEnviarLoteGov());
	}

	@Test
	public void deveRecriarServicoQuandoCertificadoMuda() {
		when(empresaServico.obterAtiva(1L)).thenReturn(empresa(null));
		ComunicacaoServico antes = registry.obter(1L);
		when(certificadoRegistry.obter(1L)).thenReturn(cert2);
		assertNotSame(antes, registry.obter(1L));
	}
}
