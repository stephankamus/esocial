package br.jus.tst.esocialjt.empresa;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class EmpresaDTO {

	private Long id;

	@NotBlank
	@Pattern(regexp = "\\d{14}", message = "deve conter 14 dígitos, sem máscara")
	private String cnpj;

	@NotBlank
	private String razaoSocial;

	private Integer tpInsc;
	private Integer tpInscTransmissor;
	private String nrInscTransmissor;
	private String ambiente;
	private Boolean ativo;
	private Date dtaCriacao;

	public static EmpresaDTO de(Empresa e) {
		EmpresaDTO d = new EmpresaDTO();
		d.id = e.getId();
		d.cnpj = e.getCnpj();
		d.razaoSocial = e.getRazaoSocial();
		d.tpInsc = e.getTpInsc();
		d.tpInscTransmissor = e.getTpInscTransmissor();
		d.nrInscTransmissor = e.getNrInscTransmissor();
		d.ambiente = e.getAmbiente();
		d.ativo = e.isAtivo();
		d.dtaCriacao = e.getDtaCriacao();
		return d;
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

	public Integer getTpInsc() {
		return tpInsc;
	}

	public void setTpInsc(Integer tpInsc) {
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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Date getDtaCriacao() {
		return dtaCriacao;
	}

	public void setDtaCriacao(Date dtaCriacao) {
		this.dtaCriacao = dtaCriacao;
	}
}
