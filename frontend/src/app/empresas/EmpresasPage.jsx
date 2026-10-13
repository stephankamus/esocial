import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid,
  Icon,
  IconButton,
  InputAdornment,
  LinearProgress,
  MenuItem,
  Paper,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography
} from "@mui/material";
import { useState } from "react";
import {
  useCertificadosEmpresa,
  useDesativarCertificado,
  useEmpresas,
  useEnviarCertificado,
  useSalvarEmpresa
} from "../../api/ESocialJTServiceApi";
import Page from "../../components/layout/page/Page";
import PageHeader from "../../components/page-header/PageHeader";
import useFeedback from "../../components/feedback/useFeedback";

const EMPRESA_VAZIA = {
  cnpj: "",
  razaoSocial: "",
  tpInsc: 1,
  tpInscTransmissor: "",
  nrInscTransmissor: "",
  ambiente: "PRODUCAO_RESTRITA",
  ativo: true
};

function mensagemErro(error) {
  return (
    error?.mensagem || error?.message || "Não foi possível concluir a operação."
  );
}

function formatarCnpj(cnpj = "") {
  return cnpj
    .replace(/(\d{2})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1/$2")
    .replace(/(\d{4})(\d{1,2})$/, "$1-$2");
}

function dataValidade(data) {
  return data ? new Date(data).toLocaleDateString("pt-BR") : "Não informada";
}

function EmpresasPage() {
  const [busca, setBusca] = useState("");
  const [empresaDialog, setEmpresaDialog] = useState(false);
  const [certificadoDialog, setCertificadoDialog] = useState(false);
  const [empresaAtual, setEmpresaAtual] = useState(null);
  const [form, setForm] = useState(EMPRESA_VAZIA);
  const [arquivo, setArquivo] = useState(null);
  const [senha, setSenha] = useState("");
  const { showSuccess, showError } = useFeedback();
  const empresasQuery = useEmpresas(busca);
  const salvarEmpresa = useSalvarEmpresa();
  const certificadosQuery = useCertificadosEmpresa(empresaAtual?.id);
  const enviarCertificado = useEnviarCertificado(empresaAtual?.id);
  const desativarCertificado = useDesativarCertificado(empresaAtual?.id);

  function abrirCadastro(empresa = null) {
    setEmpresaAtual(empresa);
    setForm(
      empresa
        ? {
            ...empresa,
            tpInsc: empresa.tpInsc ?? 1,
            tpInscTransmissor: empresa.tpInscTransmissor ?? "",
            nrInscTransmissor: empresa.nrInscTransmissor ?? ""
          }
        : { ...EMPRESA_VAZIA }
    );
    setEmpresaDialog(true);
  }

  function salvar(event) {
    event.preventDefault();
    const payload = {
      ...form,
      id: empresaAtual?.id,
      cnpj: form.cnpj.replace(/\D/g, ""),
      tpInsc: Number(form.tpInsc),
      tpInscTransmissor: form.tpInscTransmissor
        ? Number(form.tpInscTransmissor)
        : null,
      nrInscTransmissor: form.nrInscTransmissor || null
    };
    salvarEmpresa.mutate(payload, {
      onSuccess: () => {
        setEmpresaDialog(false);
        showSuccess(
          empresaAtual ? "Empresa atualizada." : "Empresa cadastrada."
        );
      },
      onError: (error) => showError(mensagemErro(error))
    });
  }

  function abrirCertificados(empresa) {
    setEmpresaAtual(empresa);
    setArquivo(null);
    setSenha("");
    setCertificadoDialog(true);
  }

  function enviar(event) {
    event.preventDefault();
    if (!arquivo || !senha) {
      showError("Selecione o arquivo .pfx e informe a senha.");
      return;
    }
    enviarCertificado.mutate(
      { arquivo, senha },
      {
        onSuccess: () => {
          setArquivo(null);
          setSenha("");
          event.target.reset();
          showSuccess("Certificado enviado e validado.");
        },
        onError: (error) => showError(mensagemErro(error))
      }
    );
  }

  function desativar(certificado) {
    if (!window.confirm("Desativar este certificado?")) return;
    desativarCertificado.mutate(certificado.id, {
      onSuccess: () => showSuccess("Certificado desativado."),
      onError: (error) => showError(mensagemErro(error))
    });
  }

  const empresas = empresasQuery.data || [];

  return (
    <Page>
      <PageHeader
        primary="Empresas"
        secondary="Cadastre os empregadores e gerencie seus certificados digitais."
        commands={[
          <Button
            key="nova-empresa"
            variant="contained"
            startIcon={<Icon>add</Icon>}
            onClick={() => abrirCadastro()}
          >
            Nova empresa
          </Button>
        ]}
      />

      <Grid container spacing={2} sx={{ mb: 2 }} alignItems="center">
        <Grid item xs={12} sm={8} md={6}>
          <TextField
            fullWidth
            size="small"
            label="Buscar empresa"
            value={busca}
            onChange={(event) => setBusca(event.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Icon fontSize="small">search</Icon>
                </InputAdornment>
              )
            }}
          />
        </Grid>
        <Grid item xs>
          <Typography variant="body2" color="text.secondary">
            {empresas.length} empresa(s)
          </Typography>
        </Grid>
      </Grid>

      <TableContainer component={Paper} variant="outlined">
        {empresasQuery.isLoading && <LinearProgress />}
        {empresasQuery.error ? (
          <Alert severity="error">{mensagemErro(empresasQuery.error)}</Alert>
        ) : (
          <Table size="small" aria-label="Empresas cadastradas">
            <TableHead>
              <TableRow>
                <TableCell>Empresa</TableCell>
                <TableCell>CNPJ</TableCell>
                <TableCell>Ambiente</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Ações</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {empresas.map((empresa) => (
                <TableRow key={empresa.id} hover>
                  <TableCell>
                    <Typography variant="body2" fontWeight={600}>
                      {empresa.razaoSocial}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      ID {empresa.id}
                    </Typography>
                  </TableCell>
                  <TableCell>{formatarCnpj(empresa.cnpj)}</TableCell>
                  <TableCell>
                    {empresa.ambiente === "PRODUCAO"
                      ? "Produção"
                      : "Produção restrita"}
                  </TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={empresa.ativo ? "Ativa" : "Inativa"}
                      color={empresa.ativo ? "success" : "default"}
                      variant="outlined"
                    />
                  </TableCell>
                  <TableCell align="right" sx={{ whiteSpace: "nowrap" }}>
                    <Button
                      size="small"
                      onClick={() => abrirCertificados(empresa)}
                    >
                      Certificados
                    </Button>
                    <IconButton
                      size="small"
                      aria-label={`Editar ${empresa.razaoSocial}`}
                      title="Editar empresa"
                      onClick={() => abrirCadastro(empresa)}
                    >
                      <Icon fontSize="small">edit</Icon>
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {!empresasQuery.isLoading && empresas.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5 }}>
                    <Typography color="text.secondary">
                      {busca
                        ? "Nenhuma empresa corresponde à busca."
                        : "Nenhuma empresa cadastrada."}
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </TableContainer>

      <Dialog
        open={empresaDialog}
        onClose={() => setEmpresaDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <Box component="form" onSubmit={salvar}>
          <DialogTitle>
            {empresaAtual ? "Editar empresa" : "Nova empresa"}
          </DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ pt: 1 }}>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  label="CNPJ (14 dígitos)"
                  value={form.cnpj}
                  disabled={!!empresaAtual}
                  inputProps={{ maxLength: 18 }}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      cnpj: event.target.value.replace(/\D/g, "")
                    })
                  }
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  required
                  fullWidth
                  label="Razão social"
                  value={form.razaoSocial}
                  onChange={(event) =>
                    setForm({ ...form, razaoSocial: event.target.value })
                  }
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Ambiente"
                  value={form.ambiente || "PRODUCAO_RESTRITA"}
                  onChange={(event) =>
                    setForm({ ...form, ambiente: event.target.value })
                  }
                >
                  <MenuItem value="PRODUCAO_RESTRITA">
                    Produção restrita
                  </MenuItem>
                  <MenuItem value="PRODUCAO">Produção</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Tipo de inscrição"
                  value={form.tpInsc}
                  onChange={(event) =>
                    setForm({ ...form, tpInsc: event.target.value })
                  }
                >
                  <MenuItem value={1}>CNPJ</MenuItem>
                  <MenuItem value={2}>CPF</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  select
                  fullWidth
                  label="Tipo de inscrição do transmissor"
                  value={form.tpInscTransmissor}
                  onChange={(event) =>
                    setForm({ ...form, tpInscTransmissor: event.target.value })
                  }
                >
                  <MenuItem value="">A própria empresa</MenuItem>
                  <MenuItem value={1}>CNPJ</MenuItem>
                  <MenuItem value={2}>CPF</MenuItem>
                </TextField>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Inscrição do transmissor"
                  value={form.nrInscTransmissor}
                  disabled={!form.tpInscTransmissor}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      nrInscTransmissor: event.target.value.replace(/\D/g, "")
                    })
                  }
                />
              </Grid>
              {empresaAtual && (
                <Grid item xs={12}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={!!form.ativo}
                        onChange={(event) =>
                          setForm({ ...form, ativo: event.target.checked })
                        }
                      />
                    }
                    label="Empresa ativa"
                  />
                </Grid>
              )}
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEmpresaDialog(false)}>Cancelar</Button>
            <Button
              type="submit"
              variant="contained"
              disabled={salvarEmpresa.isLoading}
            >
              {salvarEmpresa.isLoading ? (
                <CircularProgress size={20} />
              ) : (
                "Salvar"
              )}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Dialog
        open={certificadoDialog}
        onClose={() => setCertificadoDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Certificados
          {empresaAtual && (
            <Typography variant="body2" color="text.secondary">
              {empresaAtual.razaoSocial} · {formatarCnpj(empresaAtual.cnpj)}
            </Typography>
          )}
        </DialogTitle>
        <DialogContent dividers>
          <Box component="form" onSubmit={enviar} sx={{ mb: 3 }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              Enviar certificado A1
            </Typography>
            <Grid container spacing={1.5} alignItems="center">
              <Grid item xs={12} sm={7}>
                <Button component="label" variant="outlined" fullWidth>
                  <Icon sx={{ mr: 1 }}>upload_file</Icon>
                  {arquivo?.name || "Selecionar arquivo .pfx"}
                  <input
                    hidden
                    type="file"
                    accept=".pfx,.p12,application/x-pkcs12"
                    onChange={(event) =>
                      setArquivo(event.target.files?.[0] || null)
                    }
                  />
                </Button>
              </Grid>
              <Grid item xs={12} sm={5}>
                <TextField
                  fullWidth
                  size="small"
                  type="password"
                  label="Senha do certificado"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                />
              </Grid>
              <Grid item xs={12}>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={enviarCertificado.isLoading || !arquivo || !senha}
                  startIcon={
                    enviarCertificado.isLoading ? (
                      <CircularProgress size={16} color="inherit" />
                    ) : (
                      <Icon>verified</Icon>
                    )
                  }
                >
                  Validar e ativar certificado
                </Button>
              </Grid>
            </Grid>
          </Box>

          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Histórico de certificados
          </Typography>
          {certificadosQuery.isLoading && <LinearProgress />}
          {certificadosQuery.error && (
            <Alert severity="error">
              {mensagemErro(certificadosQuery.error)}
            </Alert>
          )}
          {!certificadosQuery.isLoading &&
            !certificadosQuery.error &&
            (certificadosQuery.data || []).length === 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                Nenhum certificado enviado.
              </Typography>
            )}
          {(certificadosQuery.data || []).map((certificado) => (
            <Box
              key={certificado.id}
              sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                gap: 2,
                py: 1.5,
                borderBottom: 1,
                borderColor: "divider"
              }}
            >
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="body2" fontWeight={600}>
                  {certificado.cnpjCertificado
                    ? formatarCnpj(certificado.cnpjCertificado)
                    : `Certificado ${certificado.id}`}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Validade: {dataValidade(certificado.dtaValidade)}
                  {certificado.alias ? ` · ${certificado.alias}` : ""}
                </Typography>
              </Box>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <Chip
                  size="small"
                  label={certificado.ativo ? "Ativo" : "Inativo"}
                  color={certificado.ativo ? "success" : "default"}
                  variant="outlined"
                />
                {certificado.ativo && (
                  <IconButton
                    size="small"
                    title="Desativar certificado"
                    aria-label="Desativar certificado"
                    onClick={() => desativar(certificado)}
                    disabled={desativarCertificado.isLoading}
                  >
                    <Icon fontSize="small">delete_outline</Icon>
                  </IconButton>
                )}
              </Box>
            </Box>
          ))}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCertificadoDialog(false)}>Fechar</Button>
        </DialogActions>
      </Dialog>
    </Page>
  );
}

export default EmpresasPage;
