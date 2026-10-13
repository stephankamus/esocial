package br.jus.tst.esocialjt.empresa;

import java.util.Date;

/** Metadados do certificado; nunca expõe o arquivo nem a senha. */
public class CertificadoEmpresaDTO {

	private Long id;
	private Long codEmpresa;
	private String cnpjCertificado;
	private String alias;
	private Date dtaValidade;
	private boolean ativo;
	private Date dtaCriacao;

	public static CertificadoEmpresaDTO de(CertificadoEmpresa c) {
		CertificadoEmpresaDTO d = new CertificadoEmpresaDTO();
		d.id = c.getId();
		d.codEmpresa = c.getEmpresa().getId();
		d.cnpjCertificado = c.getCnpjCertificado();
		d.alias = c.getAlias();
		d.dtaValidade = c.getDtaValidade();
		d.ativo = c.isAtivo();
		d.dtaCriacao = c.getDtaCriacao();
		return d;
	}

	public Long getId() {
		return id;
	}

	public Long getCodEmpresa() {
		return codEmpresa;
	}

	public String getCnpjCertificado() {
		return cnpjCertificado;
	}

	public String getAlias() {
		return alias;
	}

	public Date getDtaValidade() {
		return dtaValidade;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public Date getDtaCriacao() {
		return dtaCriacao;
	}
}
