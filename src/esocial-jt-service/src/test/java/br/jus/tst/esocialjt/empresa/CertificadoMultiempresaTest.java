package br.jus.tst.esocialjt.empresa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import br.jus.tst.esocialjt.certificado.Certificado;
import br.jus.tst.esocialjt.xml.assinatura.AssinaturaXmlServico;

public class CertificadoMultiempresaTest {

	private static final String SENHA = "teste123";

	private byte[] pfx() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/empresa-teste.pfx")) {
			return IOUtils.toByteArray(in);
		}
	}

	@Test
	public void validadorDeveExtrairCnpjAliasEValidade() throws Exception {
		ValidadorPfx.Resultado r = ValidadorPfx.validar(pfx(), SENHA);
		assertEquals("11222333000181", r.cnpj);
		assertEquals("teste", r.alias);
		assertNotNull(r.validade);
	}

	@Test
	public void validadorDeveRejeitarSenhaIncorreta() throws Exception {
		try {
			ValidadorPfx.validar(pfx(), "errada");
			fail();
		} catch (EmpresaException e) {
			assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getStatus());
		}
	}

	@Test
	public void criptografiaDeveSerReversivelEUsarIvDiferenteACadaVez() {
		CriptografiaSenha c = new CriptografiaSenha();
		ReflectionTestUtils.setField(c, "chaveBase64", "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
		String a = c.criptografar("segredo");
		String b = c.criptografar("segredo");
		assertTrue(!a.equals(b));
		assertEquals("segredo", c.descriptografar(a));
	}

	@Test(expected = EmpresaException.class)
	public void criptografiaSemChaveDeveFalhar() {
		new CriptografiaSenha().criptografar("x");
	}

	@Test
	public void deveAssinarXmlComCertificadoEmMemoria() throws Exception {
		Certificado cert = Certificado.deBytes(pfx(), null, SENHA.toCharArray(), "changeit".toCharArray(), "pkcs12",
				null);
		String xml = new AssinaturaXmlServico().assinar("<eSocial xmlns=\"http://exemplo\"><a>1</a></eSocial>", cert);
		assertNotNull(xml);
		assertTrue(xml.contains("SignatureValue"));
		assertTrue(xml.contains("X509Certificate"));
	}
}
