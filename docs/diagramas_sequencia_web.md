# Diagramas de Sequência Web

* **Projeto:** X Solution - Sistema de gestão de ativos de TI e Service Desk
* **Versão do documento:** 2.0
* **Arquitetura:** distribuída, REST e JWT
* **Data:** Outubro de 2026

---

## 1. Visão geral

No sistema legado desktop, as telas chamavam as classes de acesso a dados diretamente, na mesma memória. Na arquitetura atual, toda interação atravessa a rede via **HTTP com JSON**, a autenticação é **stateless** (cada requisição carrega seu próprio token), cada caso de uso é uma **transação atômica** delimitada no service e os efeitos colaterais (notificações e e-mails) ocorrem **de forma assíncrona após o commit**.

Este documento descreve a colaboração temporal entre os componentes nos fluxos críticos. Os diagramas usam linguagem de negócio; os nomes de componentes seguem o documento de arquitetura e os endpoints seguem o catálogo de contratos.

### 1.1. Participantes recorrentes
| Participante | Papel |
| :--- | :--- |
| **SPA (React)** | Interface do usuário. Mantém o access token em memória e trata respostas 401 renovando as credenciais. |
| **Filtro JWT** | Valida o access token de cada requisição e identifica o usuário. |
| **Controller** | Valida o DTO, verifica o perfil, delega ao service e escolhe o status HTTP. |
| **Service** | Delimita a transação, aplica escopo e autoria, orquestra entidades e repositórios. |
| **Entidade** | Aplica as regras de transição de estado e registra os eventos na timeline. |
| **Repository** | Persiste e consulta dados no PostgreSQL. |
| **Tratador global** | Converte exceções em respostas Problem Details. |
| **Notificação (assíncrona)** | Cria notificações e envia e-mails após o commit. |

### 1.2. Índice
| ID | Fluxo | Requisitos |
| :--- | :--- | :--- |
| D1 | Autenticação e emissão de credenciais | RF01 |
| D2 | Validação de requisições protegidas | RF01, RNF05 |
| D3 | Renovação de sessão com rotação e logout | RF02 |
| D4 | Recuperação de senha | RF04 |
| D5 | Abertura de chamado | RF12 |
| D6 | Designação, pendência e retomada | RF13, RF14 |
| D7 | Conclusão com solução técnica | RF16 |
| D8 | Cancelamento | RF18 |
| D9 | Interação na timeline | RF15 |
| D10 | Transferência de equipamento | RF11 |
| D11 | Baixa patrimonial com termo | RF10 |
| D12 | Notificação após o commit | RF19 |

### 1.3. Comportamento comum a todos os fluxos autenticados
Para não repetir em cada diagrama:
* Toda requisição protegida passa antes pelo fluxo D2. Se o token estiver ausente, inválido ou expirado, a resposta é 401 e o fluxo de negócio não é executado.
* Toda falha de validação do DTO resulta em 400 com a lista de campos inválidos, antes de chegar ao service.
* Todo perfil insuficiente resulta em 403, antes de chegar ao service.
* Toda exceção é convertida pelo tratador global em Problem Details com o identificador de correlação.
* Se uma transação falhar, nada é gravado e nenhuma notificação é gerada.

---

## 2. D1 - Autenticação e emissão de credenciais

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant SPA as SPA (React)
    participant AC as AuthController
    participant AS as AuthService
    participant LIM as Limitador de tentativas
    participant UR as UsuarioRepository
    participant TS as Serviço de tokens
    participant TR as TokenRefreshRepository

    U->>SPA: Informa e-mail e senha e clica em Entrar
    SPA->>AC: POST /auth/login com e-mail e senha
    AC->>AS: Autenticar com e-mail normalizado e senha
    AS->>LIM: Verificar bloqueio para o e-mail
    alt E-mail bloqueado por excesso de tentativas
        LIM-->>AS: Bloqueado
        AS-->>AC: Exceção de limite de tentativas
        AC-->>SPA: 429 limite-tentativas com Retry-After
        SPA-->>U: Aguarde alguns minutos para tentar novamente
    else Não bloqueado
        AS->>UR: Buscar usuário por e-mail com perfis
        UR-->>AS: Usuário ou vazio
        AS->>AS: Comparar senha com o hash BCrypt
        alt Usuário inexistente ou senha incorreta
            AS->>LIM: Registrar falha
            AS-->>AC: Exceção de credenciais inválidas
            AC-->>SPA: 401 credenciais-invalidas
            SPA-->>U: E-mail ou senha inválidos
        else Senha correta e usuário INATIVO
            AS-->>AC: Exceção de usuário inativo
            AC-->>SPA: 403 usuario-inativo
            SPA-->>U: Usuário inativo, procure o administrador
        else Senha correta e usuário ATIVO
            AS->>LIM: Zerar contador de falhas
            AS->>TS: Gerar access token com id, e-mail e perfis
            TS-->>AS: Access token assinado com validade de 15 minutos
            AS->>TS: Gerar refresh token aleatório
            TS-->>AS: Valor do refresh token
            AS->>TR: Persistir hash do refresh token com validade de 7 dias
            AS-->>AC: Credenciais e resumo do usuário
            AC-->>SPA: 200 com access token e usuário, Set-Cookie xs_refresh
            SPA->>SPA: Guarda o access token apenas em memória
            SPA-->>U: Redireciona para a tela inicial conforme o perfil
        end
    end
```

**Observações**
* Quando o usuário não existe, o sistema ainda executa uma comparação de hash fictícia, para que o tempo de resposta não revele se o e-mail está cadastrado.
* O access token nunca é gravado em armazenamento persistente do navegador (local ou de sessão), para reduzir o impacto de ataques de injeção de script.
* Ao recarregar a página, a SPA perde o access token e o recupera chamando o fluxo D3.

---

## 3. D2 - Validação de requisições protegidas

```mermaid
sequenceDiagram
    autonumber
    participant SPA as SPA (React)
    participant F as Filtro JWT
    participant TS as Serviço de tokens
    participant CTX as Contexto de segurança
    participant C as Controller
    participant S as Service
    participant H as Tratador global

    SPA->>F: Requisição com cabeçalho Authorization Bearer
    alt Cabeçalho ausente ou fora do formato Bearer
        F->>C: Segue sem autenticação
        C-->>SPA: 401 nao-autenticado (endpoint protegido)
    else Token presente
        F->>TS: Validar assinatura e expiração
        alt Token inválido ou expirado
            TS-->>F: Token rejeitado
            F-->>SPA: 401 nao-autenticado
            Note over SPA: A SPA executa o fluxo D3 e repete a requisição uma única vez
        else Token válido
            TS-->>F: Identificador do usuário e perfis
            F->>CTX: Registrar usuário autenticado e seus perfis
            F->>C: Encaminhar requisição
            C->>C: Verificar perfil exigido pelo endpoint
            alt Perfil insuficiente
                C-->>SPA: 403 acesso-negado
            else Perfil adequado
                C->>S: Executar caso de uso
                S->>CTX: Obter usuário logado
                S->>S: Verificar escopo sobre o recurso (RN12, RN13)
                alt Recurso fora do escopo
                    S-->>H: Exceção de recurso não encontrado
                    H-->>SPA: 404 recurso-nao-encontrado
                else Dentro do escopo
                    S-->>C: Resultado
                    C-->>SPA: 2xx com o DTO de resposta
                end
            end
        end
    end
```

**Observações**
* O filtro **não consulta o banco** em cada requisição: confia nos perfis contidos no token. Por isso o access token tem vida curta (15 minutos): uma alteração de perfil ou inativação leva no máximo esse tempo para ter efeito completo, enquanto a renovação (D3) já é bloqueada imediatamente.
* A verificação de escopo é a defesa real contra IDOR; o UUID apenas dificulta a adivinhação.

---

## 4. D3 - Renovação de sessão com rotação e logout

```mermaid
sequenceDiagram
    autonumber
    participant SPA as SPA (React)
    participant AC as AuthController
    participant AS as AuthService
    participant TR as TokenRefreshRepository
    participant TS as Serviço de tokens

    SPA->>AC: POST /auth/refresh com o cookie xs_refresh
    AC->>AS: Renovar a partir do valor do cookie
    AS->>TR: Buscar pelo hash do token
    alt Token inexistente ou expirado
        AS-->>AC: Exceção de não autenticado
        AC-->>SPA: 401 nao-autenticado e remoção do cookie
        SPA->>SPA: Limpa o estado e redireciona para o login
    else Token já revogado (reutilização)
        AS->>TR: Revogar todos os tokens ativos do usuário
        AS-->>AC: Exceção de não autenticado
        AC-->>SPA: 401 nao-autenticado e remoção do cookie
        Note over AS: Registro em log de segurança com o identificador do usuário
    else Token válido e usuário ATIVO
        AS->>TS: Gerar novo access token e novo refresh token
        AS->>TR: Revogar o token atual apontando para o novo
        AS->>TR: Persistir hash do novo refresh token
        AS-->>AC: Novas credenciais
        AC-->>SPA: 200 com novo access token e novo cookie xs_refresh
    end

    Note over SPA,TS: Logout
    SPA->>AC: POST /auth/logout com o cookie xs_refresh
    AC->>AS: Encerrar sessão
    AS->>TR: Revogar o token, se existir
    AC-->>SPA: 204 e remoção do cookie
    SPA->>SPA: Descarta o access token da memória
```

**Observações**
* Quando várias requisições recebem 401 ao mesmo tempo, a SPA deve executar **uma única** renovação e enfileirar as demais até a conclusão; caso contrário, renovações paralelas com o mesmo token disparariam a detecção de reutilização.
* Se o usuário tiver sido inativado, a renovação também é rejeitada com 401.

---

## 5. D4 - Recuperação de senha

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant SPA as SPA (React)
    participant AC as AuthController
    participant AS as AuthService
    participant UR as UsuarioRepository
    participant RR as TokenRedefinicaoSenhaRepository
    participant MAIL as Envio de e-mail (assíncrono)

    U->>SPA: Informa o e-mail em Esqueci minha senha
    SPA->>AC: POST /auth/esqueci-senha
    AC->>AS: Solicitar redefinição
    AS->>UR: Buscar usuário por e-mail
    opt Usuário existe e está ATIVO
        AS->>RR: Invalidar tokens pendentes do usuário
        AS->>RR: Persistir hash de novo token com validade de 30 minutos
        AS-)MAIL: Enviar link com o token após o commit
    end
    AC-->>SPA: 202 com mensagem genérica
    SPA-->>U: Se o e-mail estiver cadastrado, você receberá as instruções

    U->>SPA: Abre o link, informa nova senha e confirmação
    SPA->>AC: POST /auth/redefinir-senha com token e nova senha
    AC->>AS: Redefinir senha
    AS->>RR: Buscar pelo hash do token
    alt Token inválido, expirado ou já utilizado
        AC-->>SPA: 422 regra-de-negocio
        SPA-->>U: Link inválido ou expirado, solicite um novo
    else Token válido
        AS->>AS: Validar política de senha (RN06)
        AS->>UR: Gravar novo hash de senha
        AS->>RR: Marcar token como utilizado
        AS->>AS: Revogar todos os refresh tokens do usuário
        AC-->>SPA: 204
        SPA-->>U: Senha alterada, faça login
    end
```

**Observação:** o tempo de resposta da solicitação deve ser equivalente com ou sem usuário encontrado; por isso o envio de e-mail é assíncrono.

---

## 6. D5 - Abertura de chamado

```mermaid
sequenceDiagram
    autonumber
    actor U as Solicitante
    participant SPA as SPA (Novo chamado)
    participant CC as ChamadoController
    participant CS as ChamadoService
    participant ER as EquipamentoRepository
    participant CH as Entidade Chamado
    participant CR as ChamadoRepository
    participant EV as Publicador de eventos

    U->>SPA: Abre o formulário de novo chamado
    SPA->>CC: GET /equipamentos/elegiveis-chamado
    CC-->>SPA: Equipamentos em uso no escopo do usuário
    U->>SPA: Seleciona o equipamento e informa título, descrição, categoria e prioridade
    SPA->>CC: POST /chamados
    CC->>CS: Abrir chamado com os dados do formulário
    CS->>ER: Buscar equipamento por identificador
    alt Equipamento inexistente
        CS-->>CC: Exceção de recurso não encontrado
        CC-->>SPA: 404 recurso-nao-encontrado
    else Encontrado
        CS->>CS: Verificar escopo do solicitante (RN12)
        alt Fora do escopo
            CS-->>CC: Exceção de recurso não encontrado
            CC-->>SPA: 404 recurso-nao-encontrado
        else Dentro do escopo
            CS->>CS: Verificar se o equipamento está disponível para chamado
            alt Status diferente de EM_USO (RN02)
                CS-->>CC: Exceção de regra de negócio com o status atual
                CC-->>SPA: 422 regra-de-negocio
                SPA-->>U: Equipamento não está em uso e não pode receber chamados
            else Status EM_USO
                CS->>CS: Gerar protocolo com data, hora e sufixo aleatório
                CS->>CH: Criar chamado ABERTO com solicitante e equipamento
                CH->>CH: Registrar evento ABERTURA na timeline
                CS->>CR: Salvar chamado
                alt Colisão de protocolo detectada pela restrição de unicidade
                    CS->>CS: Gerar novo sufixo e tentar novamente, até 3 vezes
                end
                CS->>EV: Publicar evento chamado aberto
                CS-->>CC: Detalhe do chamado
                CC-->>SPA: 201 com Location e detalhe do chamado
                SPA-->>U: Chamado aberto com o protocolo e opção de anexar arquivos
            end
        end
    end
    Note over EV: Após o commit, o fluxo D12 notifica os técnicos ativos
```

**Observação:** a nova tentativa após colisão de protocolo precisa ocorrer em uma nova transação (ou com verificação prévia de existência), porque uma violação de restrição invalida a transação corrente.

---

## 7. D6 - Designação, pendência e retomada

```mermaid
sequenceDiagram
    autonumber
    actor T as Técnico
    participant SPA as SPA (Fila de chamados)
    participant CC as ChamadoController
    participant CS as ChamadoService
    participant UR as UsuarioRepository
    participant CR as ChamadoRepository
    participant CH as Entidade Chamado
    participant EV as Publicador de eventos

    T->>SPA: Seleciona o chamado e escolhe o técnico (ou Assumir)
    SPA->>CC: PATCH /chamados/id/designar com o técnico
    CC->>CS: Designar técnico
    CS->>CR: Buscar chamado
    CS->>UR: Buscar técnico
    alt Técnico inativo ou sem perfil TECNICO
        CC-->>SPA: 422 regra-de-negocio
    else Técnico válido
        CS->>CH: Designar técnico com o autor da ação
        alt Chamado finalizado
            CH-->>CS: Exceção de transição inválida
            CC-->>SPA: 422 transicao-invalida
        else Chamado ABERTO
            CH->>CH: Definir técnico e mudar para EM_ANDAMENTO
            CH->>CH: Registrar evento DESIGNACAO
        else Chamado EM_ANDAMENTO ou PENDENTE
            CH->>CH: Trocar técnico mantendo o status
            CH->>CH: Registrar evento DESIGNACAO com técnico anterior e novo
        end
        CS->>CR: Salvar com verificação de versão
        CS->>EV: Publicar evento chamado designado
        CC-->>SPA: 200 com detalhe atualizado
    end

    Note over T,EV: Pendência
    T->>SPA: Clica em Pendenciar e informa o motivo
    SPA->>CC: POST /chamados/id/pendenciar com motivo
    CC->>CS: Pendenciar
    CS->>CS: Verificar se o usuário é o técnico responsável ou administrador
    alt Não é responsável nem administrador
        CC-->>SPA: 403 acesso-negado
    else Autorizado
        CS->>CH: Pendenciar com motivo
        alt Status diferente de EM_ANDAMENTO
            CC-->>SPA: 422 transicao-invalida
        else EM_ANDAMENTO
            CH->>CH: Mudar para PENDENTE e registrar evento PENDENCIA com o motivo
            CS->>EV: Publicar evento status alterado
            CC-->>SPA: 200 com detalhe atualizado
        end
    end

    Note over T,EV: Retomada
    T->>SPA: Clica em Retomar atendimento
    SPA->>CC: POST /chamados/id/retomar
    CC->>CS: Retomar
    CS->>CH: Retomar
    CH->>CH: Validar PENDENTE, mudar para EM_ANDAMENTO e registrar evento RETOMADA
    CS->>EV: Publicar evento status alterado
    CC-->>SPA: 200 com detalhe atualizado
```

**Observação:** se dois técnicos tentarem assumir o mesmo chamado ao mesmo tempo, a verificação de versão garante que apenas o primeiro seja gravado; o segundo recebe 409 `conflito-concorrencia` e a SPA recarrega o chamado.

---

## 8. D7 - Conclusão com solução técnica

```mermaid
sequenceDiagram
    autonumber
    actor T as Técnico responsável
    participant SPA as SPA (Detalhe do chamado)
    participant CC as ChamadoController
    participant CS as ChamadoService
    participant CR as ChamadoRepository
    participant CH as Entidade Chamado
    participant EV as Publicador de eventos

    T->>SPA: Informa a solução técnica e clica em Concluir
    SPA->>CC: POST /chamados/id/concluir com a solução técnica
    Note over CC: Perfil TEC ou ADM e solução com 10 a 5.000 caracteres
    CC->>CS: Concluir chamado
    CS->>CR: Buscar chamado
    alt Inexistente
        CC-->>SPA: 404 recurso-nao-encontrado
    else Encontrado
        CS->>CS: Verificar se o usuário é o técnico responsável ou administrador (RN14)
        alt Outro técnico
            CC-->>SPA: 403 acesso-negado
        else Autorizado
            CS->>CH: Concluir com a solução e o autor
            alt Chamado finalizado (RN03) ou fora de EM_ANDAMENTO
                CH-->>CS: Exceção de transição inválida
                CC-->>SPA: 422 transicao-invalida com status atual
                SPA-->>T: Operação não permitida no status atual
            else EM_ANDAMENTO
                CH->>CH: Validar solução (RN07)
                CH->>CH: Gravar solução, data de fechamento e status CONCLUIDO
                CH->>CH: Registrar evento CONCLUSAO na timeline
                CS->>CR: Salvar com verificação de versão
                CS->>EV: Publicar evento chamado concluído
                CC-->>SPA: 200 com detalhe atualizado
                SPA-->>T: Chamado concluído e tela em modo somente leitura
            end
        end
    end
```

---

## 9. D8 - Cancelamento

```mermaid
sequenceDiagram
    autonumber
    actor A as Solicitante ou Técnico
    participant SPA as SPA (Detalhe do chamado)
    participant CC as ChamadoController
    participant CS as ChamadoService
    participant CR as ChamadoRepository
    participant CH as Entidade Chamado
    participant EV as Publicador de eventos

    A->>SPA: Clica em Cancelar e informa a justificativa
    SPA->>CC: POST /chamados/id/cancelar com justificativa
    CC->>CS: Cancelar chamado
    CS->>CR: Buscar chamado
    CS->>CS: Verificar escopo (RN13)
    alt Fora do escopo
        CC-->>SPA: 404 recurso-nao-encontrado
    else Usuário apenas Servidor e chamado diferente de ABERTO (RN15)
        CC-->>SPA: 403 acesso-negado
        SPA-->>A: Após o início do atendimento, solicite o cancelamento ao técnico
    else Autorizado
        CS->>CH: Cancelar com justificativa e autor
        alt Chamado finalizado
            CC-->>SPA: 422 transicao-invalida
        else ABERTO, EM_ANDAMENTO ou PENDENTE
            CH->>CH: Validar justificativa (RN08)
            CH->>CH: Gravar justificativa, data de fechamento e status CANCELADO
            CH->>CH: Registrar evento CANCELAMENTO
            CS->>CR: Salvar com verificação de versão
            CS->>EV: Publicar evento chamado cancelado
            CC-->>SPA: 200 com detalhe atualizado
        end
    end
```

---

## 10. D9 - Interação na timeline

```mermaid
sequenceDiagram
    autonumber
    actor A as Participante
    participant SPA as SPA (Timeline)
    participant TC as TimelineController
    participant CS as ChamadoService
    participant CR as ChamadoRepository
    participant CH as Entidade Chamado
    participant HR as HistoricoChamadoRepository
    participant EV as Publicador de eventos

    A->>SPA: Abre o chamado
    SPA->>TC: GET /chamados/id/mensagens paginado
    TC->>CS: Listar timeline
    CS->>CS: Verificar escopo (RN13)
    CS->>HR: Consultar registros do chamado em ordem cronológica
    TC-->>SPA: 200 com página de registros

    A->>SPA: Digita uma mensagem e envia
    SPA->>TC: POST /chamados/id/mensagens
    TC->>CS: Adicionar mensagem
    CS->>CR: Buscar chamado
    CS->>CS: Verificar escopo (RN13)
    alt Fora do escopo
        TC-->>SPA: 404 recurso-nao-encontrado
    else Participante válido
        CS->>CH: Adicionar mensagem com autor e texto
        alt Chamado finalizado (RN03)
            CH-->>CS: Exceção de regra de negócio
            TC-->>SPA: 422 regra-de-negocio
        else Chamado ativo
            CH->>CH: Criar registro do tipo MENSAGEM
            CS->>CR: Salvar (registro persistido em cascata)
            CS->>EV: Publicar evento nova mensagem
            TC-->>SPA: 201 com o registro criado
            SPA->>SPA: Acrescenta o registro ao final da timeline
        end
    end
```

**Observação:** na Release 2.0 não há atualização em tempo real; a SPA recarrega a timeline ao abrir o chamado, após cada ação e quando o usuário acessa uma notificação relacionada (atualização por evento do servidor está no backlog, PB09).

---

## 11. D10 - Transferência de equipamento

```mermaid
sequenceDiagram
    autonumber
    actor T as Técnico ou Administrador
    participant SPA as SPA (Gestão de ativos)
    participant EC as EquipamentoController
    participant ES as EquipamentoService
    participant ER as EquipamentoRepository
    participant SR as SetorRepository
    participant UR as UsuarioRepository
    participant EQ as Entidade Equipamento
    participant MR as MovimentacaoEquipamentoRepository

    T->>SPA: Escolhe o setor de destino, o responsável e o motivo
    SPA->>EC: POST /equipamentos/id/transferir
    EC->>ES: Transferir equipamento
    ES->>ER: Buscar equipamento
    ES->>SR: Buscar setor de destino
    opt Responsável informado
        ES->>UR: Buscar responsável de destino
    end
    alt Setor inexistente ou responsável inativo
        EC-->>SPA: 422 regra-de-negocio
    else Dados válidos
        ES->>ES: Guardar setor e responsável de origem
        ES->>EQ: Transferir para setor e responsável
        alt Equipamento BAIXADO ou destino igual à situação atual
            EQ-->>ES: Exceção de regra de negócio
            EC-->>SPA: 422 regra-de-negocio
        else Transferência válida
            EQ->>EQ: Atualizar setor e responsável
            ES->>ER: Salvar com verificação de versão
            ES->>MR: Registrar movimentação TRANSFERENCIA com origem, destino, motivo e autor
            EC-->>SPA: 200 com detalhe atualizado
        end
    end
```

---

## 12. D11 - Baixa patrimonial com termo

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant SPA as SPA (Gestão de ativos)
    participant EC as EquipamentoController
    participant ES as EquipamentoService
    participant ER as EquipamentoRepository
    participant CR as ChamadoRepository
    participant AS as AnexoService
    participant ST as Armazenamento de arquivos
    participant EQ as Entidade Equipamento
    participant MR as MovimentacaoEquipamentoRepository

    A->>SPA: Seleciona Baixar, anexa o termo e informa o motivo
    SPA->>EC: POST /equipamentos/id/baixa em multipart com termo e motivo
    Note over EC: Perfil ADM, arquivo até 10 MB, motivo com 10 a 1.000 caracteres
    EC->>ES: Baixar equipamento
    ES->>ER: Buscar equipamento
    alt Equipamento já BAIXADO
        EC-->>SPA: 422 transicao-invalida
    else Equipamento ativo
        ES->>CR: Existem chamados não terminais para o equipamento
        alt Existem (RN11)
            EC-->>SPA: 422 regra-de-negocio com a quantidade de chamados
            SPA-->>A: Conclua ou cancele os chamados vinculados antes da baixa
        else Nenhum
            ES->>AS: Validar e armazenar o termo como TERMO_BAIXA
            AS->>AS: Verificar tipo real do conteúdo e tamanho
            alt Tipo não permitido
                EC-->>SPA: 415 tipo-arquivo-nao-suportado
            else Arquivo válido
                AS->>ST: Salvar com chave interna gerada
                ST-->>AS: Chave de armazenamento
                AS-->>ES: Anexo registrado
                ES->>EQ: Baixar com termo e motivo
                EQ->>EQ: Mudar para BAIXADO, desvincular responsável, registrar data
                ES->>ER: Salvar com verificação de versão
                ES->>MR: Registrar movimentação BAIXA
                EC-->>SPA: 200 com detalhe atualizado
                SPA-->>A: Equipamento baixado do inventário
            end
        end
    end
```

**Observação:** o arquivo é gravado no armazenamento antes do commit. Se a transação falhar depois disso, o arquivo fica órfão; a implementação deve removê-lo na reversão da transação ou uma rotina periódica deve limpar arquivos sem registro correspondente.

---

## 13. D12 - Notificação após o commit

Fluxo genérico disparado pelos eventos publicados em D5 a D9.

```mermaid
sequenceDiagram
    autonumber
    participant S as Service de origem
    participant TX as Gerenciador de transação
    participant EV as Publicador de eventos
    participant NS as NotificacaoService (assíncrono)
    participant UR as UsuarioRepository
    participant NR as NotificacaoRepository
    participant MAIL as Servidor SMTP

    S->>EV: Publicar evento de domínio com o identificador do chamado e o autor
    S->>TX: Fim do caso de uso
    alt Transação revertida
        TX-->>EV: Evento descartado
        Note over EV: Nenhuma notificação é criada
    else Transação confirmada
        TX-->>EV: Commit concluído
        EV-)NS: Entregar evento em outra thread
        NS->>UR: Determinar destinatários conforme a tabela do RF19
        NS->>NS: Remover o próprio autor da lista de destinatários
        NS->>NR: Gravar uma notificação PENDENTE por destinatário em nova transação
        loop Para cada destinatário
            NS->>MAIL: Enviar e-mail
            alt Envio bem-sucedido
                NS->>NR: Atualizar status para ENVIADO
            else Falha no SMTP
                NS->>NR: Atualizar status para FALHA
                Note over NS: Registra em log sem afetar a operação original (RN10)
            end
        end
    end
```

**Observações**
* O evento carrega apenas identificadores e dados simples, nunca entidades gerenciadas, porque o processamento ocorre fora da transação original.
* A notificação in-app (registro na tabela) é independente do e-mail: mesmo com falha de SMTP, o usuário vê a notificação no sistema.
* A SPA consulta periodicamente a contagem de não lidas (por exemplo, a cada 60 segundos) e ao trocar de tela.
