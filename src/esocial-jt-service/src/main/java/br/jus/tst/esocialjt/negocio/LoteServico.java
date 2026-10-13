package br.jus.tst.esocialjt.negocio;

import br.jus.tst.esocialjt.dominio.EnvioEvento;
import br.jus.tst.esocialjt.dominio.Lote;
import br.jus.tst.esocialjt.empresa.Empresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LoteServico {

	@Autowired
	private EntityManager em;

	@Autowired
	private EstadoServico estadoServico;

	@Value("${esocialjt.limite-eventos-por-lote: 50}")
	private Long LIMITE_EVENTOS_LOTE;

	@Autowired
	PublicacaoServico publicacaoServico;

	public Lote atualiza(Lote lote) {
		Lote loteSalvo = em.merge(lote);
		publicacaoServico.publicarAlteracaoEstado(lote);
		return loteSalvo;
	}

	public void atualiza(List<Lote> lotes) {
		lotes.forEach(this::atualiza);
	}

	public void salvar(Lote lote) {
		em.persist(lote);
	}

	public List<Lote> gerarLotes(List<EnvioEvento> enviosEvento) {
		ArrayList<Lote> listaLotes = new ArrayList<>();
		// um lote só pode ter eventos de uma empresa (mesmo certificado e empregador)
		Map<Long, Lote> loteAtualPorEmpresa = new HashMap<>();
		for (EnvioEvento envioEvento : enviosEvento) {
			Empresa empresa = envioEvento.getEvento().getOcorrencia().getEmpresa();
			Lote lote = loteAtualPorEmpresa.get(empresa.getId());
			if (lote == null || lote.getEnviosEvento().size() >= LIMITE_EVENTOS_LOTE) {
				lote = novoLote(empresa);
				loteAtualPorEmpresa.put(empresa.getId(), lote);
				listaLotes.add(lote);
			}
			lote.adicionarEnvioEvento(envioEvento);
			envioEvento.setLote(lote);
		}
		return listaLotes;
	}

	private Lote novoLote(Empresa empresa) {
		Lote lote = new Lote();
		lote.setEmpresa(empresa);
		estadoServico.atualizarEstado(lote);
		return atualiza(lote);
	}

	public ConsultaLote criarConsulta() {
		return new ConsultaLote(em);
	}
}
