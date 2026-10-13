package br.jus.tst.esocialjt.empresa;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** AES-256-GCM; formato armazenado: base64(iv[12] + texto cifrado + tag). */
@Component
public class CriptografiaSenha {

	private static final int TAM_IV = 12;
	private static final int TAM_TAG_BITS = 128;

	@Value("${esocialjt.chave-criptografia:}")
	private String chaveBase64;

	private final SecureRandom random = new SecureRandom();

	public String criptografar(String senha) {
		try {
			byte[] iv = new byte[TAM_IV];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, chave(), new GCMParameterSpec(TAM_TAG_BITS, iv));
			byte[] cifrado = cipher.doFinal(senha.getBytes(StandardCharsets.UTF_8));
			byte[] saida = new byte[iv.length + cifrado.length];
			System.arraycopy(iv, 0, saida, 0, iv.length);
			System.arraycopy(cifrado, 0, saida, iv.length, cifrado.length);
			return Base64.getEncoder().encodeToString(saida);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Falha ao criptografar senha", e);
		}
	}

	public String descriptografar(String armazenado) {
		try {
			byte[] entrada = Base64.getDecoder().decode(armazenado);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, chave(), new GCMParameterSpec(TAM_TAG_BITS, entrada, 0, TAM_IV));
			byte[] claro = cipher.doFinal(entrada, TAM_IV, entrada.length - TAM_IV);
			return new String(claro, StandardCharsets.UTF_8);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Falha ao descriptografar senha (chave incorreta?)", e);
		}
	}

	private SecretKeySpec chave() {
		if (chaveBase64 == null || chaveBase64.trim().isEmpty()) {
			throw new EmpresaException(HttpStatus.INTERNAL_SERVER_ERROR,
					"esocialjt.chave-criptografia não configurada");
		}
		byte[] bytes = Base64.getDecoder().decode(chaveBase64.trim());
		if (bytes.length != 32) {
			throw new EmpresaException(HttpStatus.INTERNAL_SERVER_ERROR,
					"esocialjt.chave-criptografia deve ter 32 bytes em base64");
		}
		return new SecretKeySpec(bytes, "AES");
	}
}
