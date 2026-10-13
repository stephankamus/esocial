import { fireEvent, render, screen, within } from "@testing-library/react";
import { SnackbarProvider } from "notistack";
import { MemoryRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "react-query";
import EmpresasPage from "./EmpresasPage";

jest.mock("../../api/ESocialJTServiceApi", () => ({
  useEmpresas: () => ({
    data: [
      {
        id: 7,
        cnpj: "11222333000181",
        razaoSocial: "Empresa de Teste",
        ambiente: "PRODUCAO_RESTRITA",
        ativo: true
      }
    ],
    isLoading: false
  }),
  useSalvarEmpresa: () => ({
    isLoading: false,
    mutate: (_value, options) => options.onSuccess()
  }),
  useCertificadosEmpresa: () => ({
    data: [
      {
        id: 12,
        cnpjCertificado: "11222333000181",
        dtaValidade: "2030-01-01T00:00:00.000Z",
        alias: "teste",
        ativo: true
      }
    ],
    isLoading: false
  }),
  useEnviarCertificado: () => ({
    isLoading: false,
    mutate: (_value, options) => options.onSuccess()
  }),
  useDesativarCertificado: () => ({
    isLoading: false,
    mutate: (_value, options) => options.onSuccess()
  }),
  useESocialGovStatus: () => ({ data: { status: "UP" } }),
  useESocialJTStatus: () => ({ data: { status: "UP" } })
}));

function renderPage() {
  const client = new QueryClient();
  return render(
    <MemoryRouter>
      <SnackbarProvider>
        <QueryClientProvider client={client}>
          <EmpresasPage />
        </QueryClientProvider>
      </SnackbarProvider>
    </MemoryRouter>
  );
}

test("lista empresas e exibe os metadados dos certificados", async () => {
  renderPage();

  expect(screen.getByText("Empresa de Teste")).toBeInTheDocument();
  fireEvent.click(screen.getByRole("button", { name: "Certificados" }));

  expect(
    await screen.findByRole("heading", { name: /Certificados/ })
  ).toBeInTheDocument();
  expect(screen.getByText(/Validade:.*teste/)).toBeInTheDocument();
  expect(
    within(screen.getByRole("dialog")).getByText("Ativo")
  ).toBeInTheDocument();
});

test("abre o formulário de edição com CNPJ bloqueado", async () => {
  renderPage();
  fireEvent.click(
    screen.getByRole("button", { name: "Editar Empresa de Teste" })
  );

  const dialog = await screen.findByRole("dialog");
  const fields = within(dialog).getAllByRole("textbox");
  const cnpj = fields[0];
  expect(cnpj).toBeDisabled();
  expect(fields[1]).toHaveValue("Empresa de Teste");
});
