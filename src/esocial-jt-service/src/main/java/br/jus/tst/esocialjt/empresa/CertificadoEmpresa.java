package br.jus.tst.esocialjt.empresa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "EST_CERTIFICADO")
public class CertificadoEmpresa {

	@Id
	@SequenceGenerator(name = "EST_CERTIFICADO_GENERATOR", sequenceName = "SEQ_CERTIFICADO_ID", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EST_CERTIFICADO_GENERATOR")
	@Column(name = "COD_CERTIFICADO")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "COD_EMPRESA", nullable = false)
	private Empresa empresa;

	// bytea mapeado como byte[] (sem @Lob, que no Postgres usaria large object)
	@Column(name = "BIN_ARQUIVO", nullable = false)
	private byte[] arquivo;

	@Column(name = "TXT_SENHA_CRIPTOGRAFADA", nullable = false)
	private String senhaCriptografada;

	@Column(name = "TXT_TIPO", nullable = false)
	private String tipo = "pkcs12";

	@Column(name = "TXT_ALIAS")
	private String alias;

	@Column(name = "TXT_CNPJ_CERTIFICADO")
	private String cnpjCertificado;

	@Column(name = "DTA_VALIDADE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dtaValidade;

	@Column(name = "IND_ATIVO", nullable = false)
	private boolean ativo = true;

	@Column(name = "DTA_CRIACAO", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dtaCriacao;

	@PrePersist
	void aoCriar() {
		dtaCriacao = new Date();
	}

	public Long getId() {
		return id;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public byte[] getArquivo() {
		return arquivo;
	}

	public void setArquivo(byte[] arquivo) {
		this.arquivo = arquivo;
	}

	public String getSenhaCriptografada() {
		return senhaCriptografada;
	}

	public void setSenhaCriptografada(String senhaCriptografada) {
		this.senhaCriptografada = senhaCriptografada;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getCnpjCertificado() {
		return cnpjCertificado;
	}

	public void setCnpjCertificado(String cnpjCertificado) {
		this.cnpjCertificado = cnpjCertificado;
	}

	public Date getDtaValidade() {
		return dtaValidade;
	}

	public void setDtaValidade(Date dtaValidade) {
		this.dtaValidade = dtaValidade;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public Date getDtaCriacao() {
		return dtaCriacao;
	}
}
