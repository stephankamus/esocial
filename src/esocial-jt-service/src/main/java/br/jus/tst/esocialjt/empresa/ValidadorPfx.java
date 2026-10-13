package br.jus.tst.esocialjt.empresa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

public final class ValidadorPfx {

	private static final Pattern CNPJ_NO_SUBJECT = Pattern.compile("(?<!\\d)(\\d{14})(?!\\d)");

	public static final class Resultado {
		public final String alias;
		public final String cnpj;
		public final Date validade;

		Resultado(String alias, String cnpj, Date validade) {
			this.alias = alias;
			this.cnpj = cnpj;
			this.validade = validade;
		}
	}

	private ValidadorPfx() {
	}

	/** Valida senha, presença de chave privada e validade; cnpj é null se não identificável no subject. */
	public static Resultado validar(byte[] pfx, String senha) {
		try {
			KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new ByteArrayInputStream(pfx), senha.toCharArray());
			Enumeration<String> aliases = ks.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				if (!ks.isKeyEntry(alias)) {
					continue;
				}
				X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
				Date validade = x509.getNotAfter();
				if (validade.before(new Date())) {
					throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Certificado vencido em " + validade);
				}
				Matcher m = CNPJ_NO_SUBJECT.matcher(x509.getSubjectX500Principal().getName());
				return new Resultado(alias, m.find() ? m.group(1) : null, validade);
			}
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Arquivo não contém chave privada");
		} catch (IOException e) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Arquivo inválido ou senha incorreta");
		} catch (GeneralSecurityException e) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Não foi possível ler o certificado");
		}
	}
}
