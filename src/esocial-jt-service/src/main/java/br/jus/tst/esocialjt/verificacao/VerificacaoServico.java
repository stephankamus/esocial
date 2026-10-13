package br.jus.tst.esocialjt.verificacao;

import br.jus.tst.esocial.ocorrencia.TipoOcorrencia;
import br.jus.tst.esocialjt.dominio.Ocorrencia;
import br.jus.tst.esocialjt.empresa.Empresa;
import br.jus.tst.esocialjt.empresa.EmpresaServico;
import br.jus.tst.esocialjt.negocio.exception.EntidadeNaoExisteException;
import br.jus.tst.esocialjt.ocorrencia.ExemploOcorrenciaServico;
import br.jus.tst.esocialjt.ocorrencia.OcorrenciaServico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class VerificacaoServico {

    @Autowired
    OcorrenciaServico servico;

    @Autowired
    EmpresaServico empresaServico;

    final List<TipoOcorrencia> verificacoes = Arrays.asList(
            TipoOcorrencia.INFORMACOES_EMPREGADOR,
            TipoOcorrencia.TABELA_ESTABELECIMENTO,
            TipoOcorrencia.TABELA_RUBRICA,
            TipoOcorrencia.TABELA_LOTACAO,
            TipoOcorrencia.TABELA_PROCESSO
    );

    public void iniciarEnvioEventosExemplo(Long codEmpresa) {
        System.out.println("Iniciando envio de eventos de exemplo");
        Empresa empresa = empresaServico.obterAtiva(codEmpresa);
        ExemploOcorrenciaServico exemplo = new ExemploOcorrenciaServico();

        verificacoes.forEach(tipo -> {
            try {
                Ocorrencia ocorrencia = exemplo.lerOcorrencia(tipo);
                ocorrencia.getDadosOcorrencia().getIdeEmpregador().setNrInsc(empresa.getCnpj());
                servico.salvar(ocorrencia, codEmpresa);
            } catch (EntidadeNaoExisteException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
