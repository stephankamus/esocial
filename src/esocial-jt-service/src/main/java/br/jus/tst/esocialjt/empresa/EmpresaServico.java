package br.jus.tst.esocialjt.empresa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.jus.tst.esocialjt.TipoAmbiente;

@Service
@Transactional
public class EmpresaServico {

	@Autowired
	private EmpresaRepository repository;

	public EmpresaDTO criar(EmpresaDTO dto) {
		validar(dto);
		if (repository.existsByCnpj(dto.getCnpj())) {
			throw new EmpresaException(HttpStatus.CONFLICT, "Já existe empresa com este CNPJ");
		}
		Empresa e = new Empresa();
		copiar(dto, e);
		return EmpresaDTO.de(repository.save(e));
	}

	public EmpresaDTO atualizar(Long id, EmpresaDTO dto) {
		validar(dto);
		Empresa e = buscarEntidade(id);
		if (!e.getCnpj().equals(dto.getCnpj())) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "O CNPJ de uma empresa não pode ser alterado");
		}
		copiar(dto, e);
		return EmpresaDTO.de(repository.save(e));
	}

	@Transactional(readOnly = true)
	public EmpresaDTO buscar(Long id) {
		return EmpresaDTO.de(buscarEntidade(id));
	}

	@Transactional(readOnly = true)
	public EmpresaDTO buscarPorCnpj(String cnpj) {
		return EmpresaDTO.de(repository.findByCnpj(cnpj)
				.orElseThrow(() -> new EmpresaException(HttpStatus.NOT_FOUND, "Empresa não encontrada")));
	}

	@Transactional(readOnly = true)
	public List<EmpresaDTO> listar(String busca, boolean somenteAtivas) {
		String termo = busca == null ? "" : busca.trim();
		return repository.buscar(termo).stream().filter(e -> !somenteAtivas || e.isAtivo()).map(EmpresaDTO::de)
				.collect(Collectors.toList());
	}

	/** Retorna a empresa apenas se ativa; usado pelo fluxo de ocorrências. */
	@Transactional(readOnly = true)
	public Empresa obterAtiva(Long id) {
		Empresa e = buscarEntidade(id);
		if (!e.isAtivo()) {
			throw new EmpresaException(HttpStatus.UNPROCESSABLE_ENTITY, "Empresa inativa");
		}
		return e;
	}

	Empresa buscarEntidade(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new EmpresaException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
	}

	@Transactional(readOnly = true)
	public Empresa obterPorCnpj(String cnpj) {
		return repository.findByCnpj(cnpj)
				.orElseThrow(() -> new EmpresaException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
	}

	private void copiar(EmpresaDTO dto, Empresa e) {
		e.setCnpj(dto.getCnpj());
		e.setRazaoSocial(dto.getRazaoSocial().trim());
		e.setTpInsc(dto.getTpInsc() == null ? 1 : dto.getTpInsc());
		e.setTpInscTransmissor(dto.getTpInscTransmissor());
		e.setNrInscTransmissor(dto.getNrInscTransmissor());
		e.setAmbiente(dto.getAmbiente());
		e.setAtivo(dto.getAtivo() == null || dto.getAtivo());
	}

	private void validar(EmpresaDTO dto) {
		if (!cnpjValido(dto.getCnpj())) {
			throw new EmpresaException(HttpStatus.BAD_REQUEST, "CNPJ inválido");
		}
		if (dto.getAmbiente() != null) {
			try {
				TipoAmbiente.valueOf(dto.getAmbiente());
			} catch (IllegalArgumentException ex) {
				throw new EmpresaException(HttpStatus.BAD_REQUEST, "Ambiente inválido: use PRODUCAO ou PRODUCAO_RESTRITA");
			}
		}
		boolean temTp = dto.getTpInscTransmissor() != null;
		boolean temNr = dto.getNrInscTransmissor() != null && !dto.getNrInscTransmissor().trim().isEmpty();
		if (temTp != temNr) {
			throw new EmpresaException(HttpStatus.BAD_REQUEST,
					"Informe tpInscTransmissor e nrInscTransmissor juntos, ou nenhum");
		}
	}

	static boolean cnpjValido(String cnpj) {
		if (cnpj == null || !cnpj.matches("\\d{14}") || cnpj.matches("(\\d)\\1{13}")) {
			return false;
		}
		int[] pesos1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
		int[] pesos2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
		return digito(cnpj, pesos1) == cnpj.charAt(12) - '0' && digito(cnpj, pesos2) == cnpj.charAt(13) - '0';
	}

	private static int digito(String cnpj, int[] pesos) {
		int soma = 0;
		for (int i = 0; i < pesos.length; i++) {
			soma += (cnpj.charAt(i) - '0') * pesos[i];
		}
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}
}
