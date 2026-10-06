# Diagramas de Sequência Web — X Solution

**Projeto:** X Solution — Sistema de Gestão de Ativos de TI e Service Desk  
**Versão:** 2.0 (Arquitetura Distribuída / RESTful & JWT)  
**Status:** Especificação Aprovada para Desenvolvimento  
**Data:** Outubro de 2026  

---

## 1. Visão Geral dos Fluxos Dinâmicos

Diferente do sistema legado desktop, onde chamadas ocorriam em memória local entre classes FXML e DAOs, a arquitetura moderna opera sobre o protocolo **HTTP**, com tráfego **JSON**, autenticação **Stateless via JWT**, transações atômicas gerenciadas pelo Spring (`@Transactional`) e disparos assíncronos (`@Async`).

Este documento especifica o comportamento temporal e a colaboração entre os componentes para os cenários operacionais mais críticos.

---

## 2. Diagrama 1: Autenticação Stateless e Emissão de Token JWT

Demonstra o fluxo de login a partir do formulário React até a emissão das chaves criptográficas.

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as Usuário (Navegador)
    participant React as React (SPA)
    participant AuthCtrl as AuthController
    participant AuthMgr as AuthenticationManager (Spring)
    participant UserDetails as UserDetailsServiceImpl
    participant JwtSvc as JwtService
    participant DB as PostgreSQL

    Usuario->>React: Preenche email e senha e clica "Entrar"
    React->>AuthCtrl: POST /api/v1/auth/login {email, senha}
    
    AuthCtrl->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>UserDetails: loadUserByUsername(email)
    UserDetails->>DB: SELECT * FROM usuario WHERE email = ?
    DB-->>UserDetails: Retorna registro do usuário com hash BCrypt
    
    alt Credenciais Inválidas ou Usuário Inativo
        AuthMgr-->>AuthCtrl: BadCredentialsException / DisabledException
        AuthCtrl-->>React: HTTP 401 Unauthorized (ProblemDetail RFC 7807)
        React-->>Usuario: Exibe mensagem: "E-mail ou senha incorretos"
    else Credenciais Válidas e Usuário Ativo
        AuthMgr-->>AuthCtrl: Authentication (Sucesso)
        AuthCtrl->>JwtSvc: gerarAccessToken(usuario)
        JwtSvc-->>AuthCtrl: Retorna JWT assinado com claims (roles)
        AuthCtrl->>JwtSvc: gerarRefreshToken(usuario)
        JwtSvc-->>AuthCtrl: Retorna Refresh Token
        AuthCtrl-->>React: HTTP 200 OK {accessToken, refreshToken, usuario}
        React->>React: Armazena token em memória/Cookie HTTP-Only
        React-->>Usuario: Redireciona para o Dashboard principal
    end
```

---

## 3. Diagrama 2: Pipeline de Segurança — Validação de Requisições com JWT

Ilustra como cada requisição protegida enviada pelo React é interceptada e validada pelo Spring Security antes de atingir qualquer `@RestController`.

```mermaid
sequenceDiagram
    autonumber
    participant React as React (Frontend)
    participant JwtFilter as JwtAuthenticationFilter
    participant JwtSvc as JwtService
    participant SecContext as SecurityContextHolder
    participant Endpoint as RestController / Service

    React->>JwtFilter: HTTP Request + Header: Authorization: Bearer <TOKEN>
    
    alt Header Ausente ou Formato Inválido
        JwtFilter->>Endpoint: Continua na cadeia (anônimo)
        Endpoint-->>React: HTTP 401 Unauthorized
    else Token Presente
        JwtFilter->>JwtSvc: extrairUsername(token) & validarToken(token)
        alt Token Expirado ou Assinatura Inválida
            JwtSvc-->>JwtFilter: TokenException
            JwtFilter-->>React: HTTP 401 Unauthorized (Token Expirado)
        else Token Válido
            JwtFilter->>JwtSvc: extrairRoles(token)
            JwtFilter->>SecContext: setAuthentication(UsernamePasswordAuthToken)
            Note over SecContext: Usuário e permissões (ROLE_*) disponíveis na Thread
            JwtFilter->>Endpoint: Executa endpoint protegido com @PreAuthorize
            Endpoint-->>React: HTTP 200 OK {dados}
        end
    end
```

---

## 4. Diagrama 3: Abertura de Chamado com Validação de Ativo Elegível

Representa a criação de um ticket vinculada à validação estrita da regra de negócio **RN02** e ao disparo de evento de notificação assíncrono.

```mermaid
sequenceDiagram
    autonumber
    actor Servidor as Servidor (Solicitante)
    participant React as React (Novo Chamado)
    participant ChamadoCtrl as ChamadoController
    participant ChamadoSvc as ChamadoService
    participant EquipRepo as EquipamentoRepository
    participant ChamadoRepo as ChamadoRepository
    participant NotifSvc as NotificacaoService (@Async)
    participant DB as PostgreSQL

    Servidor->>React: Informa número do patrimônio, título e descrição
    React->>ChamadoCtrl: POST /api/v1/chamados {titulo, descricao, idEquipamento}
    
    Note over ChamadoCtrl: Valida Bean Validation (@NotBlank, @Size)
    ChamadoCtrl->>ChamadoSvc: criarChamado(dto, usuarioLogado)
    
    ChamadoSvc->>EquipRepo: findById(idEquipamento)
    EquipRepo->>DB: SELECT * FROM equipamento WHERE id = ?
    DB-->>EquipRepo: Retorna Equipamento
    
    alt Equipamento Não Encontrado
        ChamadoSvc-->>ChamadoCtrl: RecursoNaoEncontradoException
        ChamadoCtrl-->>React: HTTP 404 Not Found (ProblemDetail)
        React-->>Servidor: "Equipamento não localizado"
    else Equipamento Localizado
        ChamadoSvc->>ChamadoSvc: equipamento.estaDisponivelParaChamado()
        
        alt Status do Equipamento != 'EM_USO' (RN02)
            ChamadoSvc-->>ChamadoCtrl: RegraDeNegocioException("Equipamento indisponível")
            ChamadoCtrl-->>React: HTTP 422 Unprocessable Entity
            React-->>Servidor: "Ação bloqueada: equipamento não está em uso"
        else Status == 'EM_USO'
            ChamadoSvc->>ChamadoSvc: Gerar protocolo (AAAAMMDD-HHMMSS-XXX)
            ChamadoSvc->>ChamadoSvc: new Chamado(dados, solicitante, equipamento)
            ChamadoSvc->>ChamadoRepo: save(chamado)
            ChamadoRepo->>DB: INSERT INTO chamado (...)
            DB-->>ChamadoRepo: Registro persistido com sucesso
            
            ChamadoSvc->>NotifSvc: dispararNotificacaoNovoChamado(chamado)
            Note over NotifSvc: Execução assíncrona (@Async): grava na tabela notificacao e envia e-mail
            
            ChamadoSvc-->>ChamadoCtrl: Chamado criado
            ChamadoCtrl-->>React: HTTP 201 Created (Header Location + Body DTO)
            React-->>Servidor: Exibe mensagem de sucesso com o número do protocolo
        end
    end
```

---

## 5. Diagrama 4: Conclusão de Chamado com Solução Técnica Obrigatória

Modela o encerramento formal do ticket com validação das regras **RN03** (imutabilidade) e **RN07** (parecer obrigatório de solução técnica).

```mermaid
sequenceDiagram
    autonumber
    actor Tecnico as Técnico de TI
    participant React as React (Detalhes do Chamado)
    participant ChamadoCtrl as ChamadoController
    participant ChamadoSvc as ChamadoService
    participant ChamadoRepo as ChamadoRepository
    participant NotifSvc as NotificacaoService (@Async)
    participant DB as PostgreSQL

    Tecnico->>React: Informa o parecer de solução e clica "Concluir Chamado"
    React->>ChamadoCtrl: POST /api/v1/chamados/{id}/concluir {solucaoTecnica}
    
    Note over ChamadoCtrl: Verifica @PreAuthorize("hasAnyRole('TECNICO', 'ADMINISTRADOR')")
    ChamadoCtrl->>ChamadoSvc: concluirChamado(idChamado, solucaoTecnica, usuarioLogado)
    
    ChamadoSvc->>ChamadoRepo: findById(idChamado)
    ChamadoRepo->>DB: SELECT * FROM chamado WHERE id = ?
    DB-->>ChamadoRepo: Retorna Chamado
    
    alt Chamado já Concluído ou Cancelado (RN03)
        ChamadoSvc-->>ChamadoCtrl: RegraDeNegocioException("Chamado já finalizado")
        ChamadoCtrl-->>React: HTTP 422 Unprocessable Entity
        React-->>Tecnico: "Operação inválida: chamado já está fechado"
    else Chamado em Atendimento
        ChamadoSvc->>ChamadoSvc: chamado.concluir(solucaoTecnica)
        Note over ChamadoSvc: Valida solução >= 10 caracteres (RN07) e define dataFechamento
        
        ChamadoSvc->>ChamadoRepo: save(chamado)
        ChamadoRepo->>DB: UPDATE chamado SET status = 'CONCLUIDO', solucao_tecnica = ?, data_fechamento = ?
        DB-->>ChamadoRepo: Commit efetuado
        
        ChamadoSvc->>NotifSvc: notificarConclusaoChamado(chamado)
        ChamadoSvc-->>ChamadoCtrl: Chamado atualizado
        ChamadoCtrl-->>React: HTTP 200 OK (ChamadoResponse)
        React-->>Tecnico: "Chamado concluído com sucesso!"
    end
```

---

## 6. Diagrama 5: Interação Colaborativa na Timeline de Atendimento

Mostra a inclusão de mensagens e pareceres intermediários entre o servidor e o técnico responsável (`historico_chamado`).

```mermaid
sequenceDiagram
    autonumber
    actor Ator as Usuário / Técnico
    participant React as React (Timeline)
    participant MsgCtrl as ChamadoController
    participant ChamadoSvc as ChamadoService
    participant HistRepo as HistoricoChamadoRepository
    participant DB as PostgreSQL

    Ator->>React: Digita mensagem na conversa do chamado e envia
    React->>MsgCtrl: POST /api/v1/chamados/{id}/mensagens {mensagem}
    
    MsgCtrl->>ChamadoSvc: adicionarMensagem(idChamado, mensagem, autorLogado)
    
    ChamadoSvc->>ChamadoSvc: Validar permissão (Autor é solicitante, técnico ou admin?)
    ChamadoSvc->>ChamadoSvc: Validar se chamado não está FECHADO/CANCELADO
    
    ChamadoSvc->>HistRepo: save(novoHistorico)
    HistRepo->>DB: INSERT INTO historico_chamado (id, id_chamado, id_autor, mensagem, data_alteracao)
    DB-->>HistRepo: Registro persistido
    
    ChamadoSvc-->>MsgCtrl: HistoricoChamado criado
    MsgCtrl-->>React: HTTP 201 Created (HistoricoChamadoResponse)
    React->>React: Atualiza timeline na tela em tempo real
    React-->>Ator: Mensagem exibida na conversa
```

---

## 7. Diagrama 6: Baixa Patrimonial com Upload de Termo de Descarte

Mapeia o fluxo de exclusão lógica irreversível de um ativo (`BAIXADO`), com envio de arquivo via `multipart/form-data` (**RN04**).

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador
    participant React as React (Gestão de Ativos)
    participant EquipCtrl as EquipamentoController
    participant EquipSvc as EquipamentoService
    participant StorageSvc as StorageService (Arquivos)
    participant EquipRepo as EquipamentoRepository
    participant DB as PostgreSQL

    Admin->>React: Seleciona "Baixar Equipamento", anexa PDF do termo e confirma
    React->>EquipCtrl: POST /api/v1/equipamentos/{id}/baixa (multipart/form-data: termo.pdf, motivo)
    
    Note over EquipCtrl: Verifica @PreAuthorize("hasRole('ADMINISTRADOR')")
    EquipCtrl->>EquipSvc: baixarEquipamento(idEquipamento, arquivoTermo, motivo, adminLogado)
    
    EquipSvc->>EquipRepo: findById(idEquipamento)
    EquipRepo->>DB: SELECT * FROM equipamento WHERE id = ?
    DB-->>EquipRepo: Retorna Equipamento
    
    alt Equipamento já Baixado
        EquipSvc-->>EquipCtrl: RegraDeNegocioException("Equipamento já baixado")
        EquipCtrl-->>React: HTTP 422 Unprocessable Entity
    else Equipamento Ativo
        EquipSvc->>StorageSvc: salvarArquivo(arquivoTermo)
        StorageSvc-->>EquipSvc: Retorna caminho_storage do PDF
        
        EquipSvc->>EquipSvc: equipamento.baixar()
        EquipSvc->>EquipRepo: save(equipamento)
        EquipRepo->>DB: UPDATE equipamento SET status = 'BAIXADO', id_usuario = NULL WHERE id = ?
        DB-->>EquipRepo: Commit da baixa
        
        EquipSvc-->>EquipCtrl: Equipamento baixado
        EquipCtrl-->>React: HTTP 200 OK (EquipamentoResponse)
        React-->>Admin: "Equipamento baixado do inventário com sucesso!"
    end
```
