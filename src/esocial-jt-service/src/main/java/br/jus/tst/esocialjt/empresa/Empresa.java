package br.jus.tst.esocialjt.empresa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "EST_EMPRESA")
public class Empresa implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "EST_EMPRESA_GENERATOR", sequenceName = "SEQ_EMPRESA_ID", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EST_EMPRESA_GENERATOR")
	@Column(name = "COD_EMPRESA")
	private Long id;

	@Column(name = "TXT_CNPJ", nullable = false, unique = true, length = 14)
	private String cnpj;

	@Column(name = "TXT_RAZAO_SOCIAL", nullable = false)
	private String razaoSocial;

	@Column(name = "COD_TP_INSC", nullable = false)
	private int tpInsc = 1;

	@Column(name = "COD_TP_INSC_TRANSMISSOR")
	private Integer tpInscTransmissor;

	@Column(name = "TXT_NR_INSC_TRANSMISSOR")
	private String nrInscTransmissor;

	@Column(name = "TXT_AMBIENTE")
	private String ambiente;

	@Column(name = "IND_ATIVO", nullable = false)
	private boolean ativo = true;

	@Column(name = "DTA_CRIACAO", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date dtaCriacao;

	@Column(name = "DTA_ATUALIZACAO")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dtaAtualizacao;

	@PrePersist
	void aoCriar() {
		dtaCriacao = new Date();
	}

	@PreUpdate
	void aoAtualizar() {
		dtaAtualizacao = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getRazaoSocial() {
		return razaoSocial;
	}

	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}

	public int getTpInsc() {
		return tpInsc;
	}

	public void setTpInsc(int tpInsc) {
		this.tpInsc = tpInsc;
	}

	public Integer getTpInscTransmissor() {
		return tpInscTransmissor;
	}

	public void setTpInscTransmissor(Integer tpInscTransmissor) {
		this.tpInscTransmissor = tpInscTransmissor;
	}

	public String getNrInscTransmissor() {
		return nrInscTransmissor;
	}

	public void setNrInscTransmissor(String nrInscTransmissor) {
		this.nrInscTransmissor = nrInscTransmissor;
	}

	public String getAmbiente() {
		return ambiente;
	}

	public void setAmbiente(String ambiente) {
		this.ambiente = ambiente;
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

	public Date getDtaAtualizacao() {
		return dtaAtualizacao;
	}
}
