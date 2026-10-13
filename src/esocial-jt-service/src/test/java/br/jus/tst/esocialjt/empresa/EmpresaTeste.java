package br.jus.tst.esocialjt.empresa;

/** Empresa criada pela migration de teste V1001 (ID 1). */
public final class EmpresaTeste {

	public static final long ID = 1L;
	public static final String CNPJ = "00509968000148";

	private EmpresaTeste() {
	}

	public static Empresa empresa() {
		return empresa(ID, CNPJ);
	}

	/** Segunda empresa (ID 2), para testes de isolamento. */
	public static Empresa outraEmpresa() {
		return empresa(2L, "11222333000181");
	}

	private static Empresa empresa(long id, String cnpj) {
		Empresa e = new Empresa();
		e.setId(id);
		e.setCnpj(cnpj);
		e.setRazaoSocial("EMPRESA TESTE");
		return e;
	}
}
