package br.jus.tst.esocialjt.ocorrencia;

import br.jus.tst.esocial.dominio.empregador.IdeEmpregador;
import br.jus.tst.esocial.dominio.tabela.lotacao.DadosLotacao;
import br.jus.tst.esocial.dominio.tabela.lotacao.IdeLotacao;
import br.jus.tst.esocial.dominio.tabela.lotacao.InfoLotacao;
import br.jus.tst.esocial.ocorrencia.Operacao;
import br.jus.tst.esocial.ocorrencia.TipoOcorrencia;
import br.jus.tst.esocial.ocorrencia.dados.TabelaLotacao;
import br.jus.tst.esocialjt.dominio.Ocorrencia;
import br.jus.tst.esocialjt.empresa.EmpresaException;
import br.jus.tst.esocialjt.empresa.EmpresaTeste;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DBRider
public class OcorrenciaServicoTest{

	@Autowired
	OcorrenciaServico servico;

	@Autowired
	private EntityManager em;
	
	@Test
	@DataSet(executeScriptsBefore = "cleanup.sql") 
	public void testeSalvarOcorrencia() {
		Ocorrencia ocorrencia = getOcorrencia();

		servico.salvar(ocorrencia);

		Ocorrencia estOcorrencia = em.find(Ocorrencia.class, ocorrencia.getId());
		assertThat(estOcorrencia.getReferencia()).isEqualTo(ocorrencia.getReferencia());
		assertThat(estOcorrencia.getDadosOcorrencia()).isNotNull();

	}
	
	@Test(expected = EmpresaException.class)
	@DataSet(executeScriptsBefore = "cleanup.sql")
	public void deveRejeitarOcorrenciaSemEmpresa() {
		servico.salvar(getOcorrencia().setEmpresa(null));
	}

	@Test(expected = EmpresaException.class)
	@DataSet(executeScriptsBefore = "cleanup.sql")
	public void deveRejeitarEmpregadorDeOutraEmpresa() {
		servico.salvar(getOcorrencia(), 2L);
	}

	@Test
	@DataSet(value = {"ocorrencia.yml"}, executeScriptsBefore = "cleanup.sql" ) 
	public void deveRecuperarTodasOcorrencias(){
		List<Ocorrencia> ocorrencias = servico.recuperaTodos();
		assertThat(ocorrencias).isNotEmpty();
	}
	
	@Test
	@DataSet(value = {"ocorrencia.yml"}, executeScriptsBefore = "cleanup.sql" ) 
	public void deveRecuperarOcorrenciasPaginadoMaisRecentePrimeiro(){
		int page = 0;
		int size = 2;
		Page<Ocorrencia> ocorrenciasPage = servico.recuperaPaginado(page, size, Collections.emptyList(), "", Collections.emptyList(), false, Collections.emptyList(), Collections.emptyList(), null, null).pagina;

		SoftAssertions soft = new SoftAssertions();
		soft.assertThat(ocorrenciasPage.getSize()).isEqualTo(2);
		soft.assertThat(ocorrenciasPage.getTotalElements()).isEqualTo(4);
		soft.assertThat(ocorrenciasPage.getTotalPages()).isEqualTo(2);
		soft.assertThat(ocorrenciasPage.get().findFirst().get().getId()).isEqualTo(4);
		soft.assertAll();
	}
	
	@Test
	@DataSet(value = {"ocorrencia.yml", "evento.yml"}, executeScriptsBefore = "cleanup.sql")
	public void deveFiltrarPaginadoPorEmpresa() {
		long daEmpresa1 = paginado(1L);
		long daEmpresa2 = paginado(2L);
		long todas = paginado(null);

		assertThat(daEmpresa1).isEqualTo(4);
		assertThat(daEmpresa2).isEqualTo(0);
		assertThat(todas).isEqualTo(4);
	}

	@Test
	@DataSet(value = {"ocorrencia.yml", "evento.yml"}, executeScriptsBefore = "cleanup.sql")
	public void deveFiltrarBuscaIdsPorEmpresa() {
		assertThat(servico.buscarIdsPorFiltros(null, null, null, null, 1L)).isNotEmpty();
		assertThat(servico.buscarIdsPorFiltros(null, null, null, null, 2L)).isEmpty();
	}

	private long paginado(Long codEmpresa) {
		return servico.recuperaPaginado(0, 10, Collections.emptyList(), "", Collections.emptyList(), false,
				Collections.emptyList(), Collections.emptyList(), null, null, codEmpresa).pagina.getTotalElements();
	}

	@Test
	@DataSet(value = {"ocorrencia.yml"}, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarTodosDadosBasicosOcorrenciasSemEvento(){
		List<OcorrenciaDadosBasicosDTO> ocorrencias = servico.recuperaDadosBasicos();
		assertThat(ocorrencias).isNotEmpty();
		assertThat(ocorrencias.get(0).getId()).isNotNull();
	}
	
	@Test
	@DataSet(value = {"ocorrencia.yml", "evento.yml"}, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarTodosDadosBasicosOcorrenciasComEvento(){
		List<OcorrenciaDadosBasicosDTO> ocorrencias = servico.recuperaDadosBasicos();
		assertThat(ocorrencias).isNotEmpty();
		assertThat(ocorrencias.get(0).getTipoEvento()).isNotNull();
		assertThat(ocorrencias.get(0).getEstadoEvento()).isNotNull();
	}
	
	@Test
	@DataSet(value = {"ocorrencia.yml"}, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarPorId() throws Exception{
		Ocorrencia ocorrencia = servico.recuperaPorId(1l);
		assertThat(ocorrencia.getId()).isGreaterThan(0);
	}
	
	@Test
	@DataSet(value = {"ocorrencia.yml"}, executeScriptsBefore = "cleanup.sql") 
	@Transactional
	public void deveGerarEvento() throws Exception{
		Ocorrencia ocorrencia = em.find(Ocorrencia.class, 1l);
		assertThat(ocorrencia.getEvento()).isNull();
		Ocorrencia ocorrenciaComEvento = servico.gerarEvento(ocorrencia);
		assertThat(ocorrenciaComEvento.getEvento()).isNotNull();
	}
	
	private Ocorrencia getOcorrencia() {
		Ocorrencia ocorrencia = new Ocorrencia();
		ocorrencia.setTipoOcorrencia(TipoOcorrencia.TABELA_LOTACAO).setReferencia("10000").setOperacao(Operacao.INCLUSAO)
				.setEmpresa(EmpresaTeste.empresa())
				.setDataOcorrencia(new Date()).setDadosOcorrencia(getTabelaLotacao());
		return ocorrencia;
	}

	private TabelaLotacao getTabelaLotacao() {
		TabelaLotacao evento = new TabelaLotacao();
		evento.setIdeEmpregador(new IdeEmpregador());
		evento.getIdeEmpregador().setNrInsc("00509968000148");

		InfoLotacao informacao = new InfoLotacao();
		DadosLotacao dados = new DadosLotacao();
		informacao.setDadosLotacao(dados);

		IdeLotacao ideLotacao = new IdeLotacao();
		ideLotacao.setCodLotacao("1");
		ideLotacao.setIniValid("2017-01");
		informacao.setIdeLotacao(ideLotacao);
		evento.setInfoLotacao(informacao);
		return evento;
	}

}
