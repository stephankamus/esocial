# esocial (fork multiempresa) - memória do projeto

Fork de `tst-labs/esocial` (remote `upstream`) usado pelo software contábil AsuTech (workspace
`AsuTech-Contabilidade-BackEnd` / `-Frontend`). Objetivo do fork: **uma única instância atendendo vários CNPJs**,
cada um com seu certificado digital A1, em vez de 1 stack (front + serviço + banco) por cliente.

Branch de trabalho: `multiempresa`. `master` deve apenas espelhar o upstream.

## Decisões
- Não há certificado do escritório (procuração eletrônica inviável) -> certificado **por empresa**.
- Tabela `EST_EMPRESA` (ID próprio) é a raiz do tenant; ocorrência e lote apontam por `COD_EMPRESA`.
  O ERP **sempre** envia `codEmpresa`; o CNPJ não é o identificador de integração.
- `.pfx` no banco (`EST_CERTIFICADO.BIN_ARQUIVO`, bytea) e senha criptografada com AES-256-GCM
  (`esocialjt.chave-criptografia`, 32 bytes em base64). Perder a chave = reenviar os certificados.
- Sem migração de legado: banco é recriado (`docker compose down -v`). `COD_EMPRESA` é NOT NULL.
- Para reduzir conflitos de merge com o upstream: código novo em arquivos/pacote novos
  (`br.jus.tst.esocialjt.empresa`), migrations na faixa `V1000+` (`spring.flyway.out-of-order=true`),
  e edições mínimas nos arquivos do núcleo.

## Arquitetura original (upstream)
- 3 containers: frontend React+Keycloak, `esocial-jt-service` (Spring Boot 2.6.1, Java 8), PostgreSQL.
- Certificado era um bean singleton (`CertificadoProducer`, `esocialjt.arquivoCertificado`), usado em:
  assinatura (`AssinaturaXmlServico`) e TLS mútuo com o governo (`ComunicacaoConfig`).
- CNPJ do empregador vem no JSON da ocorrência (`ideEmpregador.nrInsc`); `GeradorLote` monta o lote.
- `GeradorXml` ainda lê `esocialjt.ambiente` (tpAmb) global.

## O que já foi feito
1. Migration `V1000__cria_empresa_e_certificado.sql`: `EST_EMPRESA`, `EST_CERTIFICADO`, `COD_EMPRESA` em
   `EST_OCORRENCIA` e `EST_LOTE`.
2. Pacote `empresa`: entidades, repositórios, CRUD REST `/empresas` (busca por `?busca=`, `/cnpj/{cnpj}`),
   upload `POST /empresas/{id}/certificados` (multipart `arquivo`+`senha`), validação do .pfx
   (senha, validade, CNPJ do certificado = CNPJ da empresa), erros em `{"mensagem": ...}`.
3. `Certificado.deBytes(...)` (keystore em memória), `CertificadoRegistry.obter(codEmpresa)` (cache por empresa,
   invalida sozinho ao trocar o certificado ativo), `AssinaturaXmlServico.assinar(xml, cert)`,
   `AssinadorXml.assinar(xml, codEmpresa)`.
4. `ComunicacaoRegistry.obter(codEmpresa)` (pacote `empresa`): `ComunicacaoServico` por empresa, com TLS mútuo do
   certificado dela e URLs do ambiente dela (`EST_EMPRESA.TXT_AMBIENTE`, senão `esocialjt.ambiente`). Cache invalida
   se o certificado ou o ambiente mudar. `ComunicacaoEsocialGov` ganhou `enviarLoteEventos(lote, codEmpresa)` e
   `consultarLoteEventos(protocolo, codEmpresa)`; os métodos sem empresa continuam usando o singleton global
   (`ComunicacaoConfig`) até o passo 5 trocar os chamadores. `testarConexaoEsocial()` testa com as empresas que têm
   certificado (basta uma conectar); sem nenhuma, usa o certificado global.

5. `Ocorrencia` e `Lote` têm `empresa` (NOT NULL). `OcorrenciaDTO.codEmpresa` (módulo `esocial-jt-dominio`) é
   obrigatório no `POST /ocorrencias` e no Kafka (`OcorrenciaServico.salvar(ocorrencia, codEmpresa)`); a empresa
   precisa estar ativa e `ideEmpregador.nrInsc` deve ser o CNPJ dela ou a raiz de 8 dígitos (senão 422). A exclusão
   herda a empresa da ocorrência original. `LoteServico.gerarLotes` cria lotes separados por empresa. `GeradorXml`
   assina com `AssinadorXml.assinar(xml, codEmpresa)` e usa o ambiente da empresa no `tpAmb` (via ThreadLocal, pois
   as subclasses chamam `preencherConstantes` sem o evento). `GeradorLote` usa o transmissor da empresa
   (`TPINSC/NRINSC_TRANSMISSOR`) ou a própria empresa; as env `tpInscIdeTransmissor/nrInscIdeTransmissor` só valem
   sem empresa (legado). `EnvioServico` e `AtualizacaoProcessamentoServico` usam as versões com `codEmpresa`.
   `/verificacao` exige `?codEmpresa=`. A limpeza de produção restrita (`/producao-restrita/acoes/limpar`) usa a
   empresa pelo CNPJ e recusa se alguma empresa estiver em PRODUCAO (a limpeza local apaga dados de todas).
   Testes: migration de teste `V1001` cria as empresas 1 (CNPJ 00509968000148) e 2; `EmpresaTeste` as devolve;
   datasets usam `COD_EMPRESA: "1"`. Suíte: 373 testes passando.
6. Filtro opcional `codEmpresa` em `GET /ocorrencias/paginado` e no corpo de `POST /ocorrencias/busca-ids`
   (sem ele, todas as empresas). Contrato para o ERP abaixo. Suíte: 375 testes.

## Contrato da API para o ERP (base `/esocial-jt-service`)
Erros de empresa/certificado: `{"mensagem": "..."}` com 400 (dados inválidos), 404 (não existe), 409 (CNPJ duplicado),
422 (regra: empresa inativa, sem certificado, CNPJ divergente, certificado vencido).
- `GET /empresas?busca=<trecho de CNPJ ou razão social>&somenteAtivas=true` -> lista `EmpresaDTO`.
- `GET /empresas/cnpj/{cnpj}` (14 dígitos) -> `EmpresaDTO` (usar para vincular empresa do ERP -> `id` do eSocial).
- `POST /empresas` / `PUT /empresas/{id}`: `{cnpj, razaoSocial, tpInsc?, tpInscTransmissor?, nrInscTransmissor?,
  ambiente? (PRODUCAO|PRODUCAO_RESTRITA), ativo?}`. CNPJ não muda depois de criado.
- `POST /empresas/{id}/certificados` multipart (`arquivo` .pfx, `senha`); `GET /empresas/{id}/certificados`;
  `DELETE /empresas/{id}/certificados/{cod}` (desativa). Nunca retorna arquivo/senha.
- `POST /ocorrencias`: JSON de sempre + `codEmpresa` (obrigatório). `dadosOcorrencia.ideEmpregador.nrInsc` = CNPJ da
  empresa (14) ou raiz (8). Mesmo campo no tópico Kafka.
- `GET /ocorrencias/paginado?...&codEmpresa=`; `POST /ocorrencias/busca-ids` `{..., "codEmpresa": 1}`.
- A resposta de ocorrência e lote inclui o objeto `empresa`.
- Ainda sem filtro por empresa: `/ocorrencias/sumario/{tipo}`, `/ocorrencias/tipos`, `/ocorrencias/dados-basicos`
  (deprecated) e as ações em lote (`arquivar-erros`, `arquivar-excluidos-retificados`, que valem para todas).

## Pendências (ordem planejada)
7. Tela de cadastro (empresa + upload de certificado) no frontend React (`frontend/`).
8. Depois: ajustar backend e frontend do ERP (guardar `codEmpresa` do eSocial por empresa do ERP e enviá-lo
   em todas as chamadas).
- Segurança: rotas `/empresas` usam a mesma role Keycloak do resto; avaliar role própria.
- `/xml/acoes/assinar` e `/lote/acoes/enviar` ainda usam o certificado global.

## Build e testes (Windows)
JDK e Maven ficam fora do PATH. Em cada sessão do terminal:
```powershell
$env:JAVA_HOME="C:\Users\Steph\source\repos\java\jdk8u472-b08"
$env:Path="$env:JAVA_HOME\bin;C:\Users\Steph\source\repos\java\apache-maven-3.10.0\bin;$env:Path"
cd C:\Users\Steph\source\repos\esocial\src
mvn -q -DskipTests -pl esocial-jt-service -am install   # 1a vez: instala dominio/esquemas/comunicacao
cd esocial-jt-service; mvn test
```
- Fixture de teste: `src/test/resources/empresa-teste.pfx` (autoassinado, senha `teste123`,
  CN com CNPJ 11222333000181). Não é um certificado real.
- Testes de integração usam H2 + DBRider; o H2 aceita as migrations Flyway (inclui `bytea`).

## Cuidados / armadilhas
- Hibernate: `@Lob byte[]` no Postgres vira large object; por isso `BIN_ARQUIVO` usa `byte[]` sem `@Lob`.
- Spring Boot 2.6 esconde a mensagem de `ResponseStatusException`; por isso `EmpresaException` +
  `EmpresaExceptionHandler`.
- Com `esocialjt.urlServicoAssinatura` configurado, a assinatura é remota e usa o certificado do serviço remoto.
- Upstream (`tst-labs/esocial`) é ativo: para atualizar, `git fetch upstream` e merge/rebase;
  conflitos esperados em `AssinaturaXmlServico`, `AssinadorXml`, `Certificado`, `ComunicacaoConfig`,
  `GeradorXml`, `LoteServico`, `OcorrenciaResource`.
