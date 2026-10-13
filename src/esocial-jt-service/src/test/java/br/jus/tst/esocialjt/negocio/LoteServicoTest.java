package br.jus.tst.esocialjt.negocio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;

import br.jus.tst.esocialjt.dominio.EnvioEvento;
import br.jus.tst.esocialjt.dominio.Estado;
import br.jus.tst.esocialjt.dominio.Evento;
import br.jus.tst.esocialjt.dominio.Ocorrencia;
import br.jus.tst.esocialjt.empresa.Empresa;
import br.jus.tst.esocialjt.empresa.EmpresaTeste;
import br.jus.tst.esocialjt.dominio.Lote;

@RunWith(SpringRunner.class)
@SpringBootTest
@DBRider
public class LoteServicoTest {

	@Autowired
	private LoteServico loteServico;

	@Test
	@DataSet(value = { "ocorrencia.yml", "lote.yml", "evento.yml" }, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarTodosLotesPeloId() {
		List<Lote> lotes = loteServico.criarConsulta().comIds(1l).buscar();
		assertThat(lotes).extracting("id").containsOnly(1l);
	}

	@Test
	@DataSet(value = {"ocorrencia.yml", "lote.yml", "evento.yml" }, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarTodosLotesEmDoisEstados() {
		List<Lote> lotes = loteServico.criarConsulta().nosEstados(Estado.EM_FILA, Estado.PROCESSAMENTO).buscar();
		assertThat(lotes).extracting("id").containsOnly(1l, 2l, 3l);
	}

	@Test
	@DataSet(value = {"ocorrencia.yml", "lote.yml", "evento.yml" }, executeScriptsBefore = "cleanup.sql")
	public void deveRecuperarTodosSeParametroVazio() {
		List<Lote> lotes = loteServico.criarConsulta().nosEstados().buscar();
		assertThat(lotes).extracting("id").contains(1l, 2l);
	}

	@Test
	@DataSet(executeScriptsBefore = "cleanup.sql")
	public void deveLimitarLotesACinquentaEventos() {
		List<EnvioEvento> listaEnvioEvento = gerarEnvioEvento(51);
		List<Lote> lotes = loteServico.gerarLotes(listaEnvioEvento);

		SoftAssertions soft = new SoftAssertions();
		soft.assertThat(lotes).hasSize(2);
		soft.assertThat(lotes.get(0).getEnviosEvento()).hasSize(50);
		soft.assertThat(lotes.get(1).getEnviosEvento()).hasSize(1);

		soft.assertAll();
	}

	@Test
	@DataSet(executeScriptsBefore = "cleanup.sql")
	public void deveGerarComCinquentaEventos() {
		List<EnvioEvento> listaEnvioEvento = gerarEnvioEvento(50);
		List<Lote> lotes = loteServico.gerarLotes(listaEnvioEvento);

		SoftAssertions soft = new SoftAssertions();
		soft.assertThat(lotes).hasSize(1);
		Lote lote = lotes.get(0);
		soft.assertThat(lote.getEnviosEvento()).hasSize(50);
		soft.assertThat(lote.getEnviosEvento().get(0).getLote()).isEqualTo(lote);

		soft.assertAll();
	}

	@Test
	public void deveGerarListaVaziaSeNenhumEvento() {
		List<Lote> lotes = loteServico.gerarLotes(Collections.emptyList());
		assertThat(lotes).hasSize(0);
	}

	private List<EnvioEvento> gerarEnvioEvento(int quantidade) {
		return gerarEnvioEvento(quantidade, EmpresaTeste.empresa());
	}

	@Test
	@DataSet(executeScriptsBefore = "cleanup.sql")
	public void deveSepararLotesPorEmpresa() {
		List<EnvioEvento> envios = gerarEnvioEvento(2, EmpresaTeste.empresa());
		envios.addAll(gerarEnvioEvento(2, EmpresaTeste.outraEmpresa()));

		List<Lote> lotes = loteServico.gerarLotes(envios);

		assertThat(lotes).hasSize(2);
		assertThat(lotes).extracting(l -> l.getEmpresa().getId()).containsExactlyInAnyOrder(1L, 2L);
		assertThat(lotes).allSatisfy(l -> assertThat(l.getEnviosEvento()).hasSize(2));
	}

	private List<EnvioEvento> gerarEnvioEvento(int quantidade, Empresa empresa) {
		ArrayList<EnvioEvento> listaEnvioEvento = new ArrayList<EnvioEvento>();
		for (int i = 0; i < quantidade; i++) {
			EnvioEvento envio = new EnvioEvento();
			Evento evento = new Evento();
			evento.setOcorrencia(new Ocorrencia().setEmpresa(empresa));
			envio.setEvento(evento);
			listaEnvioEvento.add(envio);
		}
		return listaEnvioEvento;
	}

}