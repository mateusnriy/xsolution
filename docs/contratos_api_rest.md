# Catálogo de Contratos da API REST — X Solution

**Projeto:** X Solution — Sistema de Gestão de Ativos de TI e Service Desk  
**Versão da API:** v1  
**Padrão Arquitetural:** RESTful / JSON / OpenAPI  
**Status:** Especificação Aprovada para Implementação  
**Data:** Outubro de 2026  

---

## 1. Convenções Globais e Padrões da API

### 1.1. URLs e Protocolo
* **Base URL:** `/api/v1`
* **Formato de Comunicação:** `application/json` (exceto endpoints de upload: `multipart/form-data`)
* **Encoding:** `UTF-8`
* **Identificadores Públicos:** Chaves primárias de negócio expostas na URL utilizam formato canônico **`UUID` (v7)** (ex: `01925b6a-9f4a-7bc3-9562-b432a4e8d35e`).
* **Datas e Horários:** Padrão **ISO-8601** em UTC (ex: `2026-10-06T14:30:00Z`).

---

### 1.2. Autenticação e Segurança
Todas as requisições protegidas devem enviar o cabeçalho HTTP:
```http
Authorization: Bearer <TOKEN_JWT>
```

#### Níveis de Autorização Declarativa (RBAC)
* `@PermitAll`: Público (login, cadastro inicial, esqueci senha).
* `@PreAuthorize("hasRole('SERVIDOR')")`: Qualquer usuário comum autenticado.
* `@PreAuthorize("hasRole('TECNICO')")`: Técnicos de TI e Administradores.
* `@PreAuthorize("hasRole('ADMINISTRADOR')")`: Exclusivo para Administradores.

---

### 1.3. Códigos de Status HTTP Padronizados

| Código | Significado | Aplicação no X Solution |
| :--- | :--- | :--- |
| **`200 OK`** | Sucesso | Retorno de consultas (`GET`), atualizações síncronas (`PUT`/`PATCH`). |
| **`201 Created`** | Criado com Sucesso | Retorno de criação (`POST`). Acompanha o header `Location: /api/v1/recurso/{id}`. |
| **`204 No Content`** | Sucesso sem Corpo | Remoção (`DELETE`) ou ações que não retornam dados no corpo. |
| **`400 Bad Request`** | Erro de Sintaxe / JSON | JSON malformado ou parâmetros inválidos na requisição. |
| **`401 Unauthorized`** | Não Autenticado | Token JWT ausente, inválido ou expirado. |
| **`403 Forbidden`** | Proibido / Sem Acesso | Usuário autenticado, mas sem o perfil RBAC necessário. |
| **`404 Not Found`** | Não Encontrado | O ID solicitado não existe no banco de dados. |
| **`409 Conflict`** | Conflito de Estado | Violação de unicidade (e-mail ou número de patrimônio já cadastrado). |
| **`422 Unprocessable`** | Regra de Negócio Violada | Dados sintaticamente válidos, mas violam regras (ex: chamado para ativo que não está `EM_USO`). |
| **`500 Internal Error`** | Erro do Servidor | Falha inesperada de infraestrutura. Não deve expor *stack traces* em produção. |

---

### 1.4. Tratamento de Erros Padronizado (RFC 7807 — Problem Details)
Todas as respostas de erro (`4xx` e `5xx`) seguem a especificação da RFC 7807 gerada pelo `@RestControllerAdvice`:

```json
{
  "type": "https://xsolution.com/erros/regra-de-negocio",
  "title": "Regra de Negócio Violada",
  "status": 422,
  "detail": "Não é possível abrir chamado para este equipamento. Status atual: ESTOQUE. Apenas equipamentos 'EM_USO' podem receber chamados.",
  "instance": "/api/v1/chamados",
  "timestamp": "2026-10-06T14:35:10Z",
  "camposInvalidos": null
}
```

Quando ocorrer erro de validação de formulário (Bean Validation `400 Bad Request`):
```json
{
  "type": "https://xsolution.com/erros/validacao",
  "title": "Dados de Entrada Inválidos",
  "status": 400,
  "detail": "Um ou mais campos contêm erros de validação.",
  "instance": "/api/v1/usuarios",
  "timestamp": "2026-10-06T14:35:10Z",
  "camposInvalidos": [
    {
      "campo": "email",
      "mensagem": "Formato de e-mail institucional inválido"
    },
    {
      "campo": "senha",
      "mensagem": "A senha deve conter no mínimo 8 caracteres, maiúscula, minúscula e caractere especial"
    }
  ]
}
```

---

## 2. Catálogo Detalhado de Endpoints

---

### 2.1. Módulo: Autenticação e Conta (`/api/v1/auth`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/auth/login` | Público | Autentica e emite token JWT | `200 OK` |
| `POST` | `/api/v1/auth/refresh` | Público | Renova access token expirado | `200 OK` |
| `POST` | `/api/v1/auth/register` | Público | Auto-cadastro de novos servidores | `201 Created` |
| `POST` | `/api/v1/auth/esqueci-senha` | Público | Solicita link de recuperação por e-mail | `200 OK` |
| `POST` | `/api/v1/auth/redefinir-senha` | Público | Redefine senha utilizando token de reset | `200 OK` |

#### DTOs do Módulo Auth (Java Records):

```java
// Request: POST /api/v1/auth/login
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String senha
) {}

// Response: Login bem-sucedido
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,       // "Bearer"
    long expiresInSeconds,  // ex: 3600
    UsuarioResumoResponse usuario
) {}

// Request: POST /api/v1/auth/register
public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 255) String nome,
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$") String senha,
    @NotNull Long idSetor
) {}
```

---

### 2.2. Módulo: Meu Perfil (`/api/v1/me`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/me` | Autenticado | Obtém dados do usuário logado | `200 OK` |
| `PUT` | `/api/v1/me` | Autenticado | Atualiza dados básicos (nome) | `200 OK` |
| `PUT` | `/api/v1/me/senha` | Autenticado | Altera senha validando a senha atual | `204 No Content` |

#### DTOs de Perfil:
```java
// Request: PUT /api/v1/me/senha
public record AlterarSenhaRequest(
    @NotBlank String senhaAtual,
    @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$") String novaSenha,
    @NotBlank String confirmacaoSenha
) {}
```

---

### 2.3. Módulo: Gestão Administrativa de Usuários (`/api/v1/usuarios`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/usuarios` | `ADMINISTRADOR` | Lista usuários paginados com filtros | `200 OK` |
| `GET` | `/api/v1/usuarios/{id}` | `ADMINISTRADOR` | Busca detalhes de um usuário por UUID | `200 OK` |
| `POST` | `/api/v1/usuarios` | `ADMINISTRADOR` | Cadastra usuário com perfis definidos | `201 Created` |
| `PATCH` | `/api/v1/usuarios/{id}/status` | `ADMINISTRADOR` | Alterna status (`ATIVO` / `INATIVO`) | `200 OK` |
| `PUT` | `/api/v1/usuarios/{id}/perfis` | `ADMINISTRADOR` | Atualiza lista de perfis RBAC | `200 OK` |

#### DTOs de Gestão de Usuários:
```java
public record UsuarioResponse(
    UUID idUsuario,
    String nome,
    String email,
    StatusUsuario status,
    Set<PerfilUsuario> perfis,
    Long idSetor,
    String nomeSetor,
    Instant criadoEm
) {}

public record AlterarStatusUsuarioRequest(
    @NotNull StatusUsuario status
) {}

public record AtualizarPerfisRequest(
    @NotEmpty Set<PerfilUsuario> perfis
) {}
```

---

### 2.4. Módulo: Setores (`/api/v1/setores`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/setores` | Autenticado | Lista todos os setores organizacionais | `200 OK` |
| `POST` | `/api/v1/setores` | `ADMINISTRADOR` | Cria novo setor | `201 Created` |
| `PUT` | `/api/v1/setores/{id}` | `ADMINISTRADOR` | Edita dados do setor | `200 OK` |
| `DELETE` | `/api/v1/setores/{id}` | `ADMINISTRADOR` | Remove setor (bloqueado se em uso) | `204 No Content` |

#### DTOs de Setor:
```java
public record CriarSetorRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Size(max = 20) String sigla
) {}

public record SetorResponse(
    Long idSetor,
    String nome,
    String sigla
) {}
```

---

### 2.5. Módulo: Gestão Patrimonial / Equipamentos (`/api/v1/equipamentos`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/equipamentos` | `TECNICO`, `ADMIN` | Consulta paginada com filtros dinâmicos | `200 OK` |
| `GET` | `/api/v1/equipamentos/{id}` | Autenticado | Detalhes completos do equipamento | `200 OK` |
| `GET` | `/api/v1/equipamentos/patrimonio/{num}` | Autenticado | Busca rápida por tombamento | `200 OK` |
| `POST` | `/api/v1/equipamentos` | `TECNICO`, `ADMIN` | Cadastra novo ativo de hardware | `201 Created` |
| `PUT` | `/api/v1/equipamentos/{id}` | `TECNICO`, `ADMIN` | Edita dados cadastrais | `200 OK` |
| `PATCH` | `/api/v1/equipamentos/{id}/status` | `TECNICO`, `ADMIN` | Transiciona status do ciclo de vida | `200 OK` |
| `POST` | `/api/v1/equipamentos/{id}/baixa` | `ADMINISTRADOR` | Realiza baixa com upload do termo | `200 OK` |
| `POST` | `/api/v1/equipamentos/{id}/transferir` | `TECNICO`, `ADMIN` | Transfere para novo setor/responsável | `200 OK` |

#### DTOs de Equipamentos:
```java
public record CriarEquipamentoRequest(
    @NotBlank @Size(max = 100) String numPatrimonio,
    @NotBlank @Size(max = 100) String numSerie,
    @NotBlank @Size(max = 100) String marca,
    @NotBlank @Size(max = 100) String modelo,
    @NotNull TipoEquipamento tipo,
    @NotNull StatusEquipamento status,
    LocalDate dataAquisicao,
    @NotNull Long idSetor,
    UUID idUsuarioResponsavel
) {}

public record EquipamentoResponse(
    UUID idEquipamento,
    String numPatrimonio,
    String numSerie,
    String marca,
    String modelo,
    TipoEquipamento tipo,
    StatusEquipamento status,
    LocalDate dataAquisicao,
    Long idSetor,
    String nomeSetor,
    UUID idUsuarioResponsavel,
    String nomeResponsavel,
    Instant criadoEm
) {}

public record TransferirEquipamentoRequest(
    @NotNull Long novoIdSetor,
    UUID novoIdUsuarioResponsavel,
    String motivo
) {}
```

---

### 2.6. Módulo: Service Desk / Chamados (`/api/v1/chamados`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/chamados` | `TECNICO`, `ADMIN` | Fila geral de chamados (paginada com filtros) | `200 OK` |
| `GET` | `/api/v1/chamados/meus` | `SERVIDOR` | Consulta apenas os chamados do autor logado | `200 OK` |
| `GET` | `/api/v1/chamados/{id}` | Autenticado | Detalhes do ticket com timeline e anexos | `200 OK` |
| `POST` | `/api/v1/chamados` | Autenticado | Abertura de ticket vinculada a equipamento | `201 Created` |
| `PATCH` | `/api/v1/chamados/{id}/designar` | `TECNICO`, `ADMIN` | Atribui técnico responsável ao chamado | `200 OK` |
| `POST` | `/api/v1/chamados/{id}/concluir` | `TECNICO`, `ADMIN` | Encerra chamado com **Solução Técnica** | `200 OK` |
| `POST` | `/api/v1/chamados/{id}/cancelar` | Autenticado | Cancela chamado com justificativa | `200 OK` |

#### DTOs de Chamados:
```java
// Request: POST /api/v1/chamados
public record CriarChamadoRequest(
    @NotBlank @Size(min = 5, max = 255) String titulo,
    @NotBlank @Size(min = 10) String descricao,
    @NotNull UUID idEquipamento
) {}

// Request: POST /api/v1/chamados/{id}/concluir
public record ConcluirChamadoRequest(
    @NotBlank @Size(min = 10, message = "A solução técnica deve conter pelo menos 10 caracteres.") 
    String solucaoTecnica
) {}

// Request: POST /api/v1/chamados/{id}/cancelar
public record CancelarChamadoRequest(
    @NotBlank @Size(min = 5, message = "Informe a justificativa do cancelamento.") 
    String justificativa
) {}

// Request: PATCH /api/v1/chamados/{id}/designar
public record DesignarTecnicoRequest(
    @NotNull UUID idTecnico
) {}

// Response: Resumo de Chamado para Grid/Tabela
public record ChamadoResumoResponse(
    UUID idChamado,
    String protocolo,
    String titulo,
    StatusChamado status,
    String numPatrimonio,
    String nomeSolicitante,
    String nomeTecnicoResponsavel,
    Instant dataAbertura,
    Instant dataFechamento
) {}
```

---

### 2.7. Módulo: Timeline e Mensagens (`/api/v1/chamados/{id}/mensagens`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/chamados/{id}/mensagens` | Autenticado | Lista a timeline cronológica do chamado | `200 OK` |
| `POST` | `/api/v1/chamados/{id}/mensagens` | Autenticado | Adiciona novo comentário na timeline | `201 Created` |

#### DTOs da Timeline:
```java
public record NovaMensagemRequest(
    @NotBlank @Size(min = 2) String mensagem
) {}

public record HistoricoChamadoResponse(
    UUID idHistorico,
    UUID idChamado,
    UUID idAutor,
    String nomeAutor,
    String mensagem,
    Instant dataAlteracao
) {}
```

---

### 2.8. Módulo: Central de Notificações (`/api/v1/notificacoes`)

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/notificacoes` | Autenticado | Lista notificações do usuário logado | `200 OK` |
| `GET` | `/api/v1/notificacoes/nao-lidas/count` | Autenticado | Retorna a quantidade de não lidas (sininho) | `200 OK` |
| `PATCH` | `/api/v1/notificacoes/{id}/lida` | Autenticado | Marca uma notificação como lida | `204 No Content` |
| `PATCH` | `/api/v1/notificacoes/todas-lidas` | Autenticado | Marca todas como lidas em lote | `204 No Content` |

#### DTOs de Notificação:
```java
public record NotificacaoResponse(
    UUID idNotificacao,
    String mensagem,
    String gatilho,
    String tipo,
    boolean lida,
    Instant dataEnvio
) {}

public record ContadorNaoLidasResponse(
    long totalNaoLidas
) {}
```

---

### 2.9. Módulo: Dashboard & Relatórios Gerenciais

| Método | Endpoint | Perfil | Descrição | Status Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/dashboard/metricas` | `ADMIN`, `TECNICO` | Contadores dos cards da tela inicial | `200 OK` |
| `GET` | `/api/v1/relatorios/chamados` | `ADMINISTRADOR` | Exporta CSV/PDF de chamados (período obrigatório) | `200 OK` |
| `GET` | `/api/v1/relatorios/equipamentos` | `ADMINISTRADOR` | Exporta CSV/PDF de inventário | `200 OK` |

#### DTO do Dashboard:
```java
public record DashboardMetricasResponse(
    long totalChamadosAbertos,
    long totalChamadosEmAndamento,
    long totalChamadosConcluidosMes,
    long totalEquipamentosEmUso,
    long totalEquipamentosManutencao,
    Map<String, Long> chamadosPorSetor
) {}
```
