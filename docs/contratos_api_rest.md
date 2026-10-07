# Catálogo de Contratos da API REST

* **Projeto:** X Solution - Sistema de gestão de ativos de TI e Service Desk
* **Versão do documento:** 2.0
* **Data:** Outubro de 2026

---

## 1. Convenções globais

### 1.1. Protocolo e formato
| Item | Convenção |
| :--- | :--- |
| **URL base** | `/api/v1` |
| **Formato** | JSON em UTF-8. Uploads usam `multipart/form-data`. Downloads devolvem o tipo do arquivo (PDF, imagem ou CSV). |
| **Nomes de campos** | camelCase, em português, sem acentos (ex.: `numPatrimonio`, `dataAbertura`). |
| **Enumerações** | Valores em MAIÚSCULAS com sublinhado (ex.: `EM_ANDAMENTO`). A comparação é exata; valor desconhecido resulta em 400. |
| **Identificadores** | Recursos de negócio usam UUID versão 7 em formato canônico (36 caracteres com hífens). Setores usam inteiro sequencial. |
| **Instantes** | ISO-8601 em UTC com sufixo `Z` (ex.: 6 de outubro de 2026, 14h30 UTC). |
| **Datas sem horário** | ISO-8601 no formato ano-mês-dia (usado em `dataAquisicao` e nos filtros de período). |
| **Campos nulos** | Campos opcionais sem valor são devolvidos com valor nulo (não são omitidos), para que o formato da resposta seja estável. |
| **Textos** | O servidor remove espaços nas extremidades de todos os campos de texto antes de validar. |

### 1.2. Paginação, ordenação e filtros
Todo endpoint de listagem marcado como **paginado** aceita os parâmetros de consulta abaixo:

| Parâmetro | Tipo | Padrão | Regra |
| :--- | :--- | :--- | :--- |
| `page` | inteiro | 0 | Índice da página, começando em zero. |
| `size` | inteiro | 20 | Itens por página. Máximo de 100; valores maiores são reduzidos a 100. |
| `sort` | texto | definido por endpoint | Formato "campo,direção" (direção `asc` ou `desc`). Pode ser repetido. Somente os campos listados em cada endpoint são aceitos; os demais resultam em 400. |

A resposta paginada tem sempre o mesmo envelope:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `conteudo` | lista | Itens da página atual. |
| `pagina` | inteiro | Índice da página devolvida. |
| `tamanho` | inteiro | Tamanho de página aplicado. |
| `totalElementos` | inteiro longo | Total de itens que atendem aos filtros. |
| `totalPaginas` | inteiro | Total de páginas. |
| `primeira` | booleano | Indica se é a primeira página. |
| `ultima` | booleano | Indica se é a última página. |

> O envelope é próprio do projeto: a serialização direta do objeto de página do framework não deve ser exposta, pois seu formato não é estável entre versões.

Filtros são parâmetros de consulta opcionais e combinados com "E" lógico. Filtros textuais marcados como **parcial** fazem busca por trecho, sem distinção de maiúsculas e de acentos.

### 1.3. Cabeçalhos
| Cabeçalho | Direção | Uso |
| :--- | :--- | :--- |
| `Authorization` | Requisição | Esquema `Bearer` seguido do access token. Obrigatório em endpoints protegidos. |
| `X-Correlation-Id` | Requisição e resposta | Opcional na requisição. Se ausente, o servidor gera um. Sempre devolvido na resposta e registrado nos logs. |
| `Location` | Resposta | Presente em toda resposta 201, com a URL do recurso criado. |
| `Content-Disposition` | Resposta | Presente em downloads, com o nome sugerido do arquivo. |

### 1.4. Cookie do refresh token
| Atributo | Valor |
| :--- | :--- |
| Nome | `xs_refresh` |
| `HttpOnly` | Sim (inacessível ao JavaScript) |
| `Secure` | Sim (exceto no ambiente local) |
| `SameSite` | `Strict` |
| `Path` | `/api/v1/auth` (enviado apenas aos endpoints de autenticação) |
| Validade | 7 dias |

> Como o refresh token trafega em cookie, o CORS do backend deve permitir credenciais e declarar explicitamente a origem do frontend (curinga é proibido nessa combinação). O frontend deve enviar as requisições de autenticação com a opção de inclusão de credenciais habilitada.

### 1.5. Concorrência otimista
As representações de **Equipamento** e **Chamado** incluem o campo `versao` (inteiro). As requisições de edição cadastral de equipamento devem reenviar a `versao` recebida. Se o registro tiver sido alterado por outra pessoa nesse intervalo, a API responde **409** com o tipo de erro `conflito-concorrencia`, e o frontend deve recarregar o recurso. Nas ações sobre chamados (designar, concluir etc.), o conflito é detectado no momento da gravação e tem o mesmo tratamento.

---

## 2. Segurança e níveis de acesso

### 2.1. Notação usada nas tabelas de endpoints
| Notação | Significado |
| :--- | :--- |
| **Público** | Não exige autenticação. |
| **Autenticado** | Qualquer usuário autenticado e ativo, independentemente do perfil. |
| **TEC** | Usuário que possui o perfil `TECNICO`. |
| **ADM** | Usuário que possui o perfil `ADMINISTRADOR`. |
| **TEC/ADM** | Usuário que possui pelo menos um dos dois perfis. |
| **Escopo RNxx** | Além do perfil, o backend verifica se o usuário tem direito sobre o recurso específico, conforme a regra indicada da especificação de requisitos. |

> Não existe hierarquia implícita: um Administrador acessa endpoints TEC/ADM porque esses endpoints declaram ambos os perfis, e não porque "Administrador herda Técnico".

### 2.2. Recursos fora do escopo
Quando um usuário autenticado solicita um recurso que existe mas está fora do seu escopo (RN12, RN13, notificação de outro usuário), a API responde **404**, e não 403, para não confirmar a existência do recurso.

---

## 3. Códigos de status HTTP

| Código | Significado | Uso no X Solution |
| :--- | :--- | :--- |
| **200 OK** | Sucesso com corpo | Consultas, edições e ações que devolvem o recurso atualizado. |
| **201 Created** | Recurso criado | Criações, sempre com o cabeçalho `Location`. |
| **204 No Content** | Sucesso sem corpo | Remoções, logout, alteração de senha e marcação de notificações. |
| **400 Bad Request** | Requisição malformada ou inválida | JSON inválido, campo obrigatório ausente, formato ou tamanho inválido, enumeração desconhecida, parâmetro de ordenação não permitido. |
| **401 Unauthorized** | Não autenticado | Token ausente, inválido ou expirado. Credenciais de login incorretas. |
| **403 Forbidden** | Sem permissão | Autenticado, mas sem o perfil exigido, ou usuário inativo tentando autenticar. |
| **404 Not Found** | Não encontrado | Recurso inexistente ou fora do escopo do usuário. |
| **409 Conflict** | Conflito de estado | Violação de unicidade, remoção bloqueada por vínculos, conflito de concorrência. |
| **413 Payload Too Large** | Arquivo grande demais | Upload acima de 10 MB. |
| **415 Unsupported Media Type** | Tipo não suportado | Arquivo com tipo diferente de PDF, PNG ou JPG; corpo com tipo de conteúdo errado. |
| **422 Unprocessable Content** | Regra de negócio violada | Dados válidos no formato, mas que violam uma regra (transição de status inválida, ativo não elegível, último administrador etc.). |
| **429 Too Many Requests** | Limite excedido | Excesso de tentativas de login ou de recuperação de senha (RN16). Acompanha o cabeçalho `Retry-After`, em segundos. |
| **500 Internal Server Error** | Falha inesperada | Erro não previsto. Nunca expõe detalhes internos (pilha de chamadas, SQL, nomes de classes). |

---

## 4. Formato de erro 

### 4.1. Campos
Toda resposta 4xx e 5xx usa o tipo de conteúdo `application/problem+json` e contém:

| Campo | Tipo | Obrigatório | Descrição |
| :--- | :--- | :---: | :--- |
| `type` | URI | Sim | Identificador estável do tipo de erro, no padrão `https://xsolution.com/erros/<codigo>`. |
| `title` | texto | Sim | Resumo legível e fixo para o tipo de erro. |
| `status` | inteiro | Sim | Código HTTP repetido no corpo. |
| `detail` | texto | Sim | Explicação específica da ocorrência, em português, apta a ser exibida ao usuário. |
| `instance` | texto | Sim | Caminho da requisição que originou o erro. |
| `timestamp` | instante | Sim | Momento da ocorrência. |
| `correlationId` | texto | Sim | Mesmo valor do cabeçalho `X-Correlation-Id`. |
| `camposInvalidos` | lista | Não | Presente apenas em erros de validação. Cada item tem `campo` (nome do campo no contrato) e `mensagem`. |

**Exemplo descritivo:** ao tentar abrir um chamado para um equipamento em estoque, a resposta tem status 422, `type` terminando em `regra-de-negocio`, `title` "Regra de negócio violada" e `detail` "Não é possível abrir chamado para este equipamento. Status atual: ESTOQUE. Apenas equipamentos em uso podem receber chamados."

### 4.2. Catálogo de tipos de erro
| Código (`type`) | Status | Quando ocorre |
| :--- | :---: | :--- |
| `validacao` | 400 | Falha de validação de campos. Preenche `camposInvalidos`. |
| `requisicao-malformada` | 400 | JSON ilegível, tipo incompatível, parâmetro inválido. |
| `credenciais-invalidas` | 401 | Login com e-mail ou senha incorretos. |
| `nao-autenticado` | 401 | Token ausente, inválido, expirado ou revogado. |
| `usuario-inativo` | 403 | Login de usuário inativo. |
| `acesso-negado` | 403 | Perfil insuficiente para a operação. |
| `recurso-nao-encontrado` | 404 | Recurso inexistente ou fora de escopo. |
| `recurso-duplicado` | 409 | Violação de unicidade (e-mail, patrimônio, nome ou sigla de setor). |
| `recurso-em-uso` | 409 | Remoção bloqueada por vínculos. |
| `conflito-concorrencia` | 409 | Registro alterado por outra operação simultânea. |
| `arquivo-muito-grande` | 413 | Upload acima do limite. |
| `tipo-arquivo-nao-suportado` | 415 | Arquivo fora dos tipos permitidos. |
| `regra-de-negocio` | 422 | Violação de regra de negócio (RN). O `detail` cita a regra. |
| `transicao-invalida` | 422 | Transição de status não permitida pela máquina de estados. O `detail` informa o status atual e os destinos permitidos. |
| `limite-tentativas` | 429 | Bloqueio temporário por excesso de tentativas. |
| `erro-interno` | 500 | Falha inesperada. |

---

## 5. Representações (recursos)

As representações abaixo são reutilizadas pelos endpoints. "Resumo" é usado em listagens; "Detalhe" é usado na consulta individual.

### 5.1. UsuarioResumo
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idUsuario` | UUID | Identificador. |
| `nome` | texto | Nome completo. |
| `email` | texto | E-mail normalizado. |
| `perfis` | lista de enumeração | Perfis do usuário (`ADMINISTRADOR`, `TECNICO`, `SERVIDOR`). |

### 5.2. UsuarioDetalhe
Todos os campos de **UsuarioResumo** e mais:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `status` | enumeração | `ATIVO` ou `INATIVO`. |
| `setor` | SetorResumo | Setor de lotação. |
| `criadoEm` | instante | Data de cadastro. |
| `atualizadoEm` | instante | Data da última alteração. |

### 5.3. SetorResumo
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idSetor` | inteiro longo | Identificador sequencial. |
| `nome` | texto | Nome do setor. |
| `sigla` | texto | Sigla em maiúsculas. |

### 5.4. EquipamentoResumo
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idEquipamento` | UUID | Identificador. |
| `numPatrimonio` | texto | Número de patrimônio. |
| `tipo` | enumeração | Tipo do equipamento. |
| `marca` | texto | Marca. |
| `modelo` | texto | Modelo. |
| `status` | enumeração | Status operacional. |
| `setor` | SetorResumo | Setor de alocação. |
| `responsavel` | UsuarioReferencia ou nulo | Responsável (identificador e nome). |

**UsuarioReferencia** é uma forma mínima com apenas `idUsuario` e `nome`, usada sempre que um usuário aparece dentro de outro recurso.

### 5.5. EquipamentoDetalhe
Todos os campos de **EquipamentoResumo** e mais:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `numSerie` | texto | Número de série. |
| `dataAquisicao` | data ou nulo | Data de aquisição. |
| `possuiTermoBaixa` | booleano | Indica se há termo de baixa anexado. |
| `motivoBaixa` | texto ou nulo | Motivo informado na baixa. |
| `dataBaixa` | instante ou nulo | Data da baixa. |
| `criadoEm` | instante | Data de cadastro. |
| `atualizadoEm` | instante | Data da última alteração. |
| `versao` | inteiro | Versão para concorrência otimista. |

### 5.6. Movimentacao
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idMovimentacao` | UUID | Identificador. |
| `tipo` | enumeração | `CADASTRO`, `TRANSFERENCIA`, `MUDANCA_STATUS` ou `BAIXA`. |
| `setorOrigem` / `setorDestino` | SetorResumo ou nulo | Setores antes e depois. |
| `responsavelOrigem` / `responsavelDestino` | UsuarioReferencia ou nulo | Responsáveis antes e depois. |
| `statusAnterior` / `statusNovo` | enumeração ou nulo | Status antes e depois. |
| `motivo` | texto ou nulo | Motivo informado. |
| `autor` | UsuarioReferencia | Quem executou. |
| `dataMovimentacao` | instante | Quando ocorreu. |

### 5.7. ChamadoResumo
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idChamado` | UUID | Identificador. |
| `protocolo` | texto | Protocolo legível. |
| `titulo` | texto | Título. |
| `status` | enumeração | Status do chamado. |
| `categoria` | enumeração | Categoria. |
| `prioridade` | enumeração | Prioridade. |
| `numPatrimonio` | texto | Patrimônio do equipamento vinculado. |
| `solicitante` | UsuarioReferencia | Quem abriu. |
| `tecnicoResponsavel` | UsuarioReferencia ou nulo | Técnico designado. |
| `dataAbertura` | instante | Data de abertura. |
| `dataFechamento` | instante ou nulo | Data de conclusão ou cancelamento. |

### 5.8. ChamadoDetalhe
Todos os campos de **ChamadoResumo** e mais:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `descricao` | texto | Descrição do problema. |
| `equipamento` | EquipamentoResumo | Equipamento vinculado. |
| `setorSolicitante` | SetorResumo | Setor do solicitante no momento da consulta. |
| `solucaoTecnica` | texto ou nulo | Preenchido na conclusão. |
| `justificativaCancelamento` | texto ou nulo | Preenchido no cancelamento. |
| `quantidadeAnexos` | inteiro | Total de anexos. |
| `acoesPermitidas` | lista de texto | Ações que o usuário logado pode executar agora (ex.: `DESIGNAR`, `PENDENCIAR`, `RETOMAR`, `CONCLUIR`, `CANCELAR`, `COMENTAR`, `ANEXAR`). Permite ao frontend exibir apenas os botões válidos sem duplicar regras. |
| `versao` | inteiro | Versão para concorrência otimista. |

A timeline e os anexos **não** são incluídos no detalhe; são consultados em endpoints próprios e paginados.

### 5.9. RegistroTimeline
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idHistorico` | UUID | Identificador. |
| `tipo` | enumeração | `MENSAGEM` (escrita por pessoa) ou `EVENTO` (gerado pelo sistema). |
| `evento` | enumeração ou nulo | Para registros do tipo `EVENTO`: `ABERTURA`, `DESIGNACAO`, `RECLASSIFICACAO`, `PENDENCIA`, `RETOMADA`, `CONCLUSAO`, `CANCELAMENTO`, `ANEXO`. |
| `autor` | UsuarioReferencia | Quem gerou o registro. |
| `mensagem` | texto | Conteúdo (para eventos, um texto descritivo gerado pelo sistema). |
| `dataRegistro` | instante | Momento do registro. |

### 5.10. Anexo
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idAnexo` | UUID | Identificador. |
| `nomeOriginal` | texto | Nome do arquivo enviado. |
| `tipoConteudo` | texto | Tipo do arquivo (PDF, PNG ou JPEG). |
| `tamanhoBytes` | inteiro longo | Tamanho. |
| `autor` | UsuarioReferencia | Quem enviou. |
| `dataUpload` | instante | Quando foi enviado. |
| `urlDownload` | texto | Caminho relativo do endpoint de download. |

### 5.11. Notificacao
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `idNotificacao` | UUID | Identificador. |
| `gatilho` | enumeração | Evento de origem (ver seção 7). |
| `mensagem` | texto | Texto exibido ao usuário. |
| `idChamado` | UUID ou nulo | Chamado relacionado, para navegação. |
| `protocoloChamado` | texto ou nulo | Protocolo do chamado relacionado. |
| `lida` | booleano | Se já foi lida. |
| `dataCriacao` | instante | Quando foi gerada. |

---

## 6. Catálogo de endpoints

### 6.1. Autenticação (`/api/v1/auth`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| POST | `/auth/login` | Público | Autentica e emite credenciais | 200 |
| POST | `/auth/refresh` | Público (cookie) | Renova as credenciais com rotação | 200 |
| POST | `/auth/logout` | Público (cookie) | Revoga o refresh token e remove o cookie | 204 |
| POST | `/auth/register` | Público | Auto-cadastro de servidor | 201 |
| POST | `/auth/esqueci-senha` | Público | Solicita link de redefinição | 202 |
| POST | `/auth/redefinir-senha` | Público | Redefine a senha com o token recebido | 204 |

#### POST `/auth/login` - RF01
**Corpo da requisição**
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `email` | texto | Sim | Formato de e-mail, até 255 caracteres. |
| `senha` | texto | Sim | Não vazio, até 64 caracteres. |

**Resposta 200:** corpo com `accessToken` (texto), `tipoToken` (sempre "Bearer"), `expiraEmSegundos` (inteiro, 900) e `usuario` (UsuarioDetalhe). O cabeçalho `Set-Cookie` define o cookie `xs_refresh`.

**Erros:** 400 `validacao` · 401 `credenciais-invalidas` · 403 `usuario-inativo` · 429 `limite-tentativas`.

#### POST `/auth/refresh` - RF02
**Requisição:** sem corpo. O navegador envia o cookie `xs_refresh`.
**Resposta 200:** mesmo formato do login, com novo access token e novo cookie (o refresh anterior é revogado).
**Erros:** 401 `nao-autenticado` (cookie ausente, token expirado, revogado ou reutilizado - neste último caso, todas as sessões do usuário são revogadas).

#### POST `/auth/logout` - RF02
**Requisição:** sem corpo. **Resposta 204:** cookie removido (validade zerada). A operação é idempotente: responde 204 mesmo sem cookie válido.

#### POST `/auth/register` - RF03
**Corpo da requisição**
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `nome` | texto | Sim | 3 a 255 caracteres. |
| `email` | texto | Sim | Formato de e-mail, até 255 caracteres. Normalizado em minúsculas. |
| `senha` | texto | Sim | Política RN06. |
| `confirmacaoSenha` | texto | Sim | Igual a `senha`. |
| `idSetor` | inteiro longo | Sim | Setor existente. |

**Resposta 201:** UsuarioDetalhe, com `Location` apontando para `/api/v1/me`. Perfis sempre igual a `SERVIDOR`.
**Erros:** 400 `validacao` · 409 `recurso-duplicado` (e-mail) · 422 `regra-de-negocio` (setor inexistente).

#### POST `/auth/esqueci-senha` - RF04
**Corpo:** `email` (texto, obrigatório).
**Resposta 202:** corpo com uma mensagem genérica ("Se o e-mail estiver cadastrado, você receberá as instruções em instantes."), **sempre igual**, exista o e-mail ou não.
**Erros:** 400 `validacao` · 429 `limite-tentativas`.

#### POST `/auth/redefinir-senha` - RF04
**Corpo da requisição**
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `token` | texto | Sim | Token recebido por e-mail. |
| `novaSenha` | texto | Sim | Política RN06. |
| `confirmacaoSenha` | texto | Sim | Igual a `novaSenha`. |

**Resposta 204.** **Erros:** 400 `validacao` · 422 `regra-de-negocio` (token inválido, expirado ou já utilizado).

---

### 6.2. Meu perfil (`/api/v1/me`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/me` | Autenticado | Dados do usuário logado | 200 |
| PUT | `/me` | Autenticado | Atualiza o próprio nome | 200 |
| PUT | `/me/senha` | Autenticado | Altera a própria senha | 204 |

* **PUT `/me`** - corpo: `nome` (3 a 255 caracteres). Resposta: UsuarioDetalhe.
* **PUT `/me/senha`** - corpo: `senhaAtual` (obrigatória), `novaSenha` (RN06, diferente da atual), `confirmacaoSenha` (igual à nova). Erros: 400 `validacao` · 422 `regra-de-negocio` (senha atual incorreta ou nova igual à atual).

---

### 6.3. Gestão de usuários (`/api/v1/usuarios`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/usuarios` | ADM | Lista paginada com filtros | 200 |
| GET | `/usuarios/tecnicos` | TEC/ADM | Lista técnicos ativos (sem paginação) | 200 |
| GET | `/usuarios/{idUsuario}` | ADM | Detalhe do usuário | 200 |
| POST | `/usuarios` | ADM | Cadastra usuário com perfis | 201 |
| PUT | `/usuarios/{idUsuario}` | ADM | Edita nome e setor | 200 |
| PATCH | `/usuarios/{idUsuario}/status` | ADM | Ativa ou inativa | 200 |
| PUT | `/usuarios/{idUsuario}/perfis` | ADM | Substitui o conjunto de perfis | 200 |
| POST | `/usuarios/{idUsuario}/anonimizar` | ADM | Anonimização LGPD (irreversível) | 200 |

**GET `/usuarios`** - paginado. Filtros: `nome` (parcial), `email` (parcial), `perfil`, `status`, `idSetor`. Ordenação permitida: `nome`, `email`, `criadoEm`; padrão `nome,asc`. Itens: UsuarioDetalhe.

**GET `/usuarios/tecnicos`** - devolve lista de UsuarioReferencia dos usuários ativos com perfil `TECNICO`, em ordem alfabética.

**POST `/usuarios`** - corpo:
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `nome` | texto | Sim | 3 a 255 caracteres. |
| `email` | texto | Sim | Formato de e-mail, único. |
| `senha` | texto | Sim | Política RN06. |
| `idSetor` | inteiro longo | Sim | Setor existente. |
| `perfis` | lista de enumeração | Sim | Ao menos um perfil, sem repetição. |

Resposta 201: UsuarioDetalhe. Erros: 400 · 409 (e-mail) · 422 (setor inexistente).

**PUT `/usuarios/{idUsuario}`** - corpo: `nome`, `idSetor`. Resposta: UsuarioDetalhe.

**PATCH `/usuarios/{idUsuario}/status`** - corpo: `status` (`ATIVO` ou `INATIVO`). Erros 422: último administrador ativo (RN05), autoinativação. A inativação revoga as sessões do usuário.

**PUT `/usuarios/{idUsuario}/perfis`** - corpo: `perfis` (lista com ao menos um item). Erros: 400 (lista vazia) · 422 (remoção do perfil Administrador do último administrador ativo).

**POST `/usuarios/{idUsuario}/anonimizar`** - corpo: `protocoloSolicitacao` (texto, obrigatório, até 100 caracteres) e `confirmacao` (booleano, deve ser verdadeiro). Resposta: UsuarioDetalhe já anonimizado. Erros: 422 (RN05, usuário já anonimizado).

---

### 6.4. Setores (`/api/v1/setores`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/setores` | Público | Lista todos os setores em ordem alfabética (sem paginação) | 200 |
| GET | `/setores/{idSetor}` | Autenticado | Detalhe do setor | 200 |
| POST | `/setores` | ADM | Cria setor | 201 |
| PUT | `/setores/{idSetor}` | ADM | Edita nome e sigla | 200 |
| DELETE | `/setores/{idSetor}` | ADM | Remove setor sem vínculos | 204 |

* **Corpo de criação e edição:** `nome` (1 a 100 caracteres, único sem distinção de maiúsculas) e `sigla` (2 a 20 caracteres, única, convertida para maiúsculas).
* **Resposta:** SetorResumo.
* **Erros:** 409 `recurso-duplicado` (nome ou sigla) · 409 `recurso-em-uso` (remoção com usuários ou equipamentos vinculados; o `detail` informa as quantidades).
* O endpoint de listagem é público porque alimenta o formulário de auto-cadastro.

---

### 6.5. Equipamentos (`/api/v1/equipamentos`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/equipamentos` | TEC/ADM | Inventário paginado com filtros | 200 |
| GET | `/equipamentos/elegiveis-chamado` | Autenticado, escopo RN12 | Equipamentos `EM_USO` nos quais o usuário pode abrir chamado | 200 |
| GET | `/equipamentos/{idEquipamento}` | Autenticado, escopo RN12 | Detalhe do equipamento | 200 |
| GET | `/equipamentos/patrimonio/{numPatrimonio}` | Autenticado, escopo RN12 | Busca exata por patrimônio | 200 |
| POST | `/equipamentos` | TEC/ADM | Cadastra equipamento | 201 |
| PUT | `/equipamentos/{idEquipamento}` | TEC/ADM | Edita dados cadastrais | 200 |
| PATCH | `/equipamentos/{idEquipamento}/status` | TEC/ADM | Transição de status (exceto baixa) | 200 |
| POST | `/equipamentos/{idEquipamento}/transferir` | TEC/ADM | Transfere setor e/ou responsável | 200 |
| POST | `/equipamentos/{idEquipamento}/baixa` | ADM | Baixa patrimonial com termo | 200 |
| GET | `/equipamentos/{idEquipamento}/termo-baixa` | TEC/ADM | Download do termo de baixa | 200 |
| GET | `/equipamentos/{idEquipamento}/movimentacoes` | TEC/ADM | Histórico de movimentações (paginado) | 200 |
| GET | `/equipamentos/{idEquipamento}/chamados` | TEC/ADM | Chamados do equipamento (paginado) | 200 |

**GET `/equipamentos`** - paginado. Filtros: `numPatrimonio` (parcial), `numSerie` (parcial), `marca` (parcial), `tipo`, `status` (pode ser repetido), `idSetor`, `idResponsavel`. Sem filtro de status, os equipamentos `BAIXADO` são excluídos. Ordenação permitida: `numPatrimonio`, `tipo`, `status`, `criadoEm`; padrão `numPatrimonio,asc`. Itens: EquipamentoResumo.

**GET `/equipamentos/elegiveis-chamado`** - paginado. Filtro: `busca` (parcial em patrimônio, marca ou modelo). Para usuários apenas Servidor, devolve os equipamentos do seu setor ou sob sua responsabilidade; para TEC/ADM, todos os equipamentos `EM_USO`. Itens: EquipamentoResumo.

**POST `/equipamentos`** - corpo:
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `numPatrimonio` | texto | Sim | Até 100 caracteres, único (normalizado em maiúsculas). |
| `numSerie` | texto | Sim | Até 100 caracteres. |
| `marca` | texto | Sim | Até 100 caracteres. |
| `modelo` | texto | Sim | Até 100 caracteres. |
| `tipo` | enumeração | Sim | Valor de TipoEquipamento. |
| `status` | enumeração | Sim | Apenas `ESTOQUE` ou `EM_USO`. |
| `dataAquisicao` | data | Não | Não pode ser futura. |
| `idSetor` | inteiro longo | Sim | Setor existente. |
| `idResponsavel` | UUID | Não | Usuário ativo. Ignorado se o status for `ESTOQUE`. |

Resposta 201: EquipamentoDetalhe. Gera a movimentação do tipo `CADASTRO`. Erros: 400 · 409 (patrimônio) · 422 (status inicial inválido, setor ou responsável inválido).

**PUT `/equipamentos/{idEquipamento}`** - corpo: `numSerie`, `marca`, `modelo`, `tipo`, `dataAquisicao`, `versao`. Patrimônio, status, setor e responsável não são aceitos aqui. Erros: 409 `conflito-concorrencia` · 422 (equipamento baixado).

**PATCH `/equipamentos/{idEquipamento}/status`** - corpo: `status` (destino: `EM_USO`, `ESTOQUE` ou `EM_MANUTENCAO`) e `motivo` (opcional, até 500 caracteres). A transição para `BAIXADO` não é aceita aqui (usar o endpoint de baixa). Erros: 422 `transicao-invalida`.

**POST `/equipamentos/{idEquipamento}/transferir`** - corpo: `idSetorDestino` (obrigatório), `idResponsavelDestino` (opcional, usuário ativo) e `motivo` (opcional, até 500 caracteres). Erros: 422 (equipamento baixado, destino idêntico à situação atual, responsável inativo).

**POST `/equipamentos/{idEquipamento}/baixa`** - `multipart/form-data` com as partes:
| Parte | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `termo` | arquivo | Sim | PDF, PNG ou JPEG, até 10 MB. |
| `motivo` | texto | Sim | 10 a 1.000 caracteres. |

Resposta 200: EquipamentoDetalhe com status `BAIXADO`. Erros: 413 · 415 · 422 (já baixado; chamados não terminais vinculados - RN11).

**GET `/equipamentos/{idEquipamento}/movimentacoes`** - paginado, ordenado por data decrescente. Itens: Movimentacao.

---

### 6.6. Chamados (`/api/v1/chamados`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/chamados` | TEC/ADM | Fila geral paginada com filtros | 200 |
| GET | `/chamados/meus` | Autenticado | Chamados abertos pelo usuário logado (paginado) | 200 |
| GET | `/chamados/{idChamado}` | Autenticado, escopo RN13 | Detalhe do chamado | 200 |
| GET | `/chamados/protocolo/{protocolo}` | Autenticado, escopo RN13 | Busca exata por protocolo | 200 |
| POST | `/chamados` | Autenticado, escopo RN12 | Abre chamado | 201 |
| PATCH | `/chamados/{idChamado}/designar` | TEC/ADM | Designa ou redesigna o técnico | 200 |
| PATCH | `/chamados/{idChamado}/classificacao` | TEC/ADM | Altera categoria e/ou prioridade | 200 |
| POST | `/chamados/{idChamado}/pendenciar` | Técnico responsável ou ADM | Coloca em `PENDENTE` | 200 |
| POST | `/chamados/{idChamado}/retomar` | Técnico responsável ou ADM | Volta para `EM_ANDAMENTO` | 200 |
| POST | `/chamados/{idChamado}/concluir` | Técnico responsável ou ADM | Conclui com solução técnica | 200 |
| POST | `/chamados/{idChamado}/cancelar` | Autenticado, regra RN15 | Cancela com justificativa | 200 |

**GET `/chamados`** - paginado. Filtros:
| Parâmetro | Descrição |
| :--- | :--- |
| `status` | Pode ser repetido para vários status. |
| `prioridade`, `categoria` | Enumerações. |
| `idSolicitante`, `idTecnico`, `idEquipamento` | Identificadores. |
| `idSetor` | Setor do equipamento. |
| `atribuidosAMim` | Booleano: apenas chamados do técnico logado. |
| `semTecnico` | Booleano: apenas chamados sem técnico designado. |
| `protocolo` | Parcial. |
| `texto` | Parcial em título e descrição. |
| `dataInicio`, `dataFim` | Período de abertura (datas inclusivas). |

Ordenação permitida: `prioridade`, `dataAbertura`, `status`, `protocolo`. Padrão: prioridade decrescente (da `CRITICA` para a `BAIXA`) e, em seguida, `dataAbertura` crescente. Itens: ChamadoResumo.

**GET `/chamados/meus`** - paginado. Filtro: `status` (pode ser repetido). Padrão: `dataAbertura,desc`. Itens: ChamadoResumo.

**POST `/chamados`** - corpo:
| Campo | Tipo | Obrigatório | Validação |
| :--- | :--- | :---: | :--- |
| `titulo` | texto | Sim | 5 a 255 caracteres. |
| `descricao` | texto | Sim | 10 a 5.000 caracteres. |
| `idEquipamento` | UUID | Sim | Equipamento `EM_USO` (RN02) e no escopo (RN12). |
| `categoria` | enumeração | Sim | Valor de CategoriaChamado. |
| `prioridade` | enumeração | Não | Valor de PrioridadeChamado; padrão `MEDIA`. |

Resposta 201: ChamadoDetalhe com status `ABERTO` e protocolo gerado. Erros: 400 · 404 (equipamento inexistente ou fora do escopo) · 422 `regra-de-negocio` (RN02).

**PATCH `/chamados/{idChamado}/designar`** - corpo: `idTecnico` (UUID, obrigatório). Se o chamado estiver `ABERTO`, passa a `EM_ANDAMENTO`. Erros: 422 (técnico inativo ou sem perfil `TECNICO`; chamado terminal).

**PATCH `/chamados/{idChamado}/classificacao`** - corpo: `categoria` e/ou `prioridade` (ao menos um). Erros: 400 (nenhum campo) · 422 (chamado terminal).

**POST `/chamados/{idChamado}/pendenciar`** - corpo: `motivo` (10 a 2.000 caracteres). Erros: 403 (técnico não responsável) · 422 `transicao-invalida`.

**POST `/chamados/{idChamado}/retomar`** - sem corpo. Erros: 403 · 422 `transicao-invalida`.

**POST `/chamados/{idChamado}/concluir`** - corpo: `solucaoTecnica` (10 a 5.000 caracteres). Erros: 403 (RN14) · 422 `transicao-invalida` (status diferente de `EM_ANDAMENTO`).

**POST `/chamados/{idChamado}/cancelar`** - corpo: `justificativa` (10 a 2.000 caracteres). Erros: 403 (solicitante tentando cancelar fora de `ABERTO` - RN15) · 404 (fora do escopo) · 422 `transicao-invalida`.

Todas as ações devolvem o ChamadoDetalhe atualizado, geram o evento correspondente na timeline e, após a confirmação da transação, as notificações previstas no RF19. Todas podem retornar 409 `conflito-concorrencia`.

---

### 6.7. Timeline (`/api/v1/chamados/{idChamado}/mensagens`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/chamados/{idChamado}/mensagens` | Autenticado, escopo RN13 | Timeline completa (mensagens e eventos), paginada | 200 |
| POST | `/chamados/{idChamado}/mensagens` | Autenticado, escopo RN13 | Publica uma mensagem | 201 |

* **GET:** ordenação padrão `dataRegistro,asc`. Filtro opcional `tipo` (`MENSAGEM` ou `EVENTO`). Itens: RegistroTimeline.
* **POST:** corpo `mensagem` (2 a 5.000 caracteres). Resposta: RegistroTimeline. Erros: 404 (fora do escopo) · 422 (chamado terminal).
* Não existem endpoints de edição nem de exclusão de registros.

---

### 6.8. Anexos

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/chamados/{idChamado}/anexos` | Autenticado, escopo RN13 | Lista anexos do chamado | 200 |
| POST | `/chamados/{idChamado}/anexos` | Autenticado, escopo RN13 | Envia um anexo | 201 |
| GET | `/anexos/{idAnexo}/download` | Autenticado, escopo do chamado de origem | Baixa o arquivo | 200 |

* **POST:** `multipart/form-data` com a parte `arquivo` (PDF, PNG ou JPEG, até 10 MB). Resposta: Anexo. Erros: 413 · 415 · 422 (chamado terminal ou limite de 10 anexos atingido).
* **Download:** devolve o conteúdo binário com o tipo do arquivo e `Content-Disposition` contendo o nome original.

---

### 6.9. Notificações (`/api/v1/notificacoes`)

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/notificacoes` | Autenticado | Notificações do usuário logado (paginado) | 200 |
| GET | `/notificacoes/nao-lidas/contagem` | Autenticado | Total de não lidas | 200 |
| PATCH | `/notificacoes/{idNotificacao}/lida` | Autenticado (dono) | Marca como lida | 204 |
| PATCH | `/notificacoes/lidas` | Autenticado | Marca todas como lidas | 204 |

* **GET `/notificacoes`:** filtro opcional `lida` (booleano). Ordenação padrão `dataCriacao,desc`. Itens: Notificacao.
* **Contagem:** corpo com o campo `totalNaoLidas` (inteiro longo).
* As marcações são idempotentes. Notificação de outro usuário: 404.

---

### 6.10. Dashboard e relatórios

| Método | Endpoint | Acesso | Descrição | Sucesso |
| :--- | :--- | :--- | :--- | :---: |
| GET | `/dashboard/metricas` | TEC/ADM | Indicadores da tela inicial | 200 |
| GET | `/relatorios/chamados` | ADM | Exporta relatório de chamados | 200 |
| GET | `/relatorios/equipamentos` | ADM | Exporta inventário | 200 |

**Resposta de `/dashboard/metricas`**
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `chamadosAbertos` | inteiro longo | Total em `ABERTO`. |
| `chamadosEmAndamento` | inteiro longo | Total em `EM_ANDAMENTO`. |
| `chamadosPendentes` | inteiro longo | Total em `PENDENTE`. |
| `chamadosConcluidosMes` | inteiro longo | Concluídos no mês corrente (fuso da instituição). |
| `chamadosAtribuidosAMim` | inteiro longo | Não terminais do usuário logado. |
| `equipamentosEmUso` | inteiro longo | Total em `EM_USO`. |
| `equipamentosEmManutencao` | inteiro longo | Total em `EM_MANUTENCAO`. |
| `chamadosPorSetor` | lista | Itens com `setor` (SetorResumo) e `total`, para chamados não terminais. |
| `chamadosPorPrioridade` | lista | Itens com `prioridade` e `total`, para chamados não terminais. |
| `geradoEm` | instante | Momento do cálculo. |

**GET `/relatorios/chamados`** - parâmetros: `dataInicio` e `dataFim` (obrigatórios - RN09), `formato` (`CSV` ou `PDF`, obrigatório), e filtros opcionais `status`, `categoria`, `prioridade`, `idSetor`, `idTecnico`. Resposta: arquivo para download. Erros: 400 (parâmetros ausentes) · 422 (período inválido ou superior a 365 dias).

**GET `/relatorios/equipamentos`** - parâmetros: `formato` (obrigatório) e filtros opcionais `status`, `tipo`, `idSetor`. Resposta: arquivo para download.

---

## 7. Enumerações

| Enumeração | Valores |
| :--- | :--- |
| **PerfilUsuario** | `ADMINISTRADOR`, `TECNICO`, `SERVIDOR` |
| **StatusUsuario** | `ATIVO`, `INATIVO` |
| **TipoEquipamento** | `COMPUTADOR`, `NOTEBOOK`, `MONITOR`, `IMPRESSORA`, `NOBREAK`, `ESTABILIZADOR`, `OUTRO` |
| **StatusEquipamento** | `EM_USO`, `ESTOQUE`, `EM_MANUTENCAO`, `BAIXADO` |
| **TipoMovimentacao** | `CADASTRO`, `TRANSFERENCIA`, `MUDANCA_STATUS`, `BAIXA` |
| **StatusChamado** | `ABERTO`, `EM_ANDAMENTO`, `PENDENTE`, `CONCLUIDO`, `CANCELADO` |
| **CategoriaChamado** | `HARDWARE`, `SOFTWARE`, `REDE`, `ACESSO`, `OUTRO` |
| **PrioridadeChamado** | `BAIXA`, `MEDIA`, `ALTA`, `CRITICA` |
| **TipoRegistroTimeline** | `MENSAGEM`, `EVENTO` |
| **EventoTimeline** | `ABERTURA`, `DESIGNACAO`, `RECLASSIFICACAO`, `PENDENCIA`, `RETOMADA`, `CONCLUSAO`, `CANCELAMENTO`, `ANEXO` |
| **GatilhoNotificacao** | `CHAMADO_ABERTO`, `CHAMADO_DESIGNADO`, `CHAMADO_STATUS_ALTERADO`, `CHAMADO_NOVA_MENSAGEM`, `CHAMADO_CONCLUIDO`, `CHAMADO_CANCELADO` |
| **StatusEnvioNotificacao** | `PENDENTE`, `ENVIADO`, `FALHA` |
