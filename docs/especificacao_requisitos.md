# Especificação de Requisitos de Software (SRS) — X Solution

**Projeto:** X Solution - Sistema integrado de gestão de ativos de TI e Service Desk  
**Versão do Documento:** 2.0
**Autores:** Mateus Neri, Lucas Lima, Kaio França  
**Data:** Outubro de 2026  
**Status:** Aprovado para desenvolvimento

---

## 1. Introdução

### 1.1. Objetivo do Documento
Este documento formaliza a Especificação de Requisitos de Software (SRS) revisada e modernizada para o sistema **X Solution**. O objetivo é servir como contrato técnico definitivo para o desenvolvimento do sistema em uma arquitetura distribuída e desacoplada, composta por **API RESTful em Spring Boot 4 (Java 21)** e **Frontend SPA em React 19 (TypeScript)**, utilizando **PostgreSQL** versionado com **Flyway**.

### 1.2. Escopo do Sistema (Release 1.0 — Core Robusto)
O X Solution centraliza a governança operacional de departamentos de Tecnologia da Informação em duas vertentes complementares:
1. **Gestão de Ativos de TI (ITAM):** Controle de inventário, ciclo de vida operacional, histórico de movimentação setorial, termos de responsabilidade e baixas patrimoniais.
2. **Service Desk (ITSM):** Central de atendimento a chamados com validação de ativos, categorização, priorização, timeline colaborativa de suporte e registro formal de soluções técnicas.

---

## 2. Atores e Perfis de Acesso (RBAC)

O controle de acesso é implementado através do padrão **Role-Based Access Control (RBAC)** por composição, permitindo que um usuário possua um ou múltiplos perfis simultâneos.

| Perfil | Nomenclatura Interna | Descrição de Responsabilidades |
| :--- | :--- | :--- |
| **Administrador** | `ROLE_ADMINISTRADOR` | Gestão irrestrita do sistema: controle de usuários e perfis, configuração de setores, supervisão total de chamados e equipamentos, extração de relatórios e auditoria. |
| **Técnico de Suporte** | `ROLE_TECNICO` | Operação do Service Desk: atendimento da fila de chamados, publicação de diagnósticos na timeline, designação de tarefas, atualização de status e cadastro/manutenção de ativos de hardware. |
| **Servidor** | `ROLE_SERVIDOR` | Usuário final da instituição: abertura de chamados técnicos para equipamentos alocados, consulta do histórico de seus tickets e autoatendimento de perfil. |

---

## 3. Requisitos Funcionais (RF)

### 3.1. Módulo de Identidade, Autenticação e Acesso

#### [RF01] Autenticação e Sessão Stateless
* **Descrição:** O sistema deve autenticar usuários mediante e-mail e senha, retornando um token de acesso **JWT (JSON Web Token)** assinado criptograficamente com tempo de expiração curto (ex: 60 minutos) e suporte a *Refresh Token*.
* **Atores:** Qualquer usuário cadastrado e ativo.
* **Critérios de Aceite:**
  * O endpoint `POST /api/v1/auth/login` valida credenciais comparando o hash BCrypt.
  * Usuários com status `INATIVO` são rejeitados com código HTTP 403 (*Forbidden*).
  * O token JWT emitido contém o `id_usuario`, `email` e as *claims* de perfis (`roles`).

#### [RF02] Auto-cadastro de Servidores
* **Descrição:** Novos servidores devem poder solicitar criação de conta informando nome completo, e-mail institucional, senha e setor de lotação.
* **Atores:** Usuário não autenticado.
* **Critérios de Aceite:**
  * Validação de unicidade de e-mail (HTTP 409 caso duplicado).
  * A conta é criada com perfil inicial `ROLE_SERVIDOR` e status `ATIVO` (ou pendente de ativação via e-mail).

#### [RF03] Recuperação de Senha Segura
* **Descrição:** Permitir a redefinição de senha através de token temporário enviado por e-mail com validade de 30 minutos.
* **Atores:** Usuário não autenticado.
* **Critérios de Aceite:**
  * Para evitar ataques de enumeração de e-mails, o sistema sempre retorna uma resposta genérica de sucesso (HTTP 200), mesmo que o e-mail não exista no banco.

#### [RF04] Gestão de Meu Perfil
* **Descrição:** O usuário autenticado deve poder visualizar seus dados, atualizar seu nome e alterar sua própria senha.
* **Atores:** Qualquer usuário autenticado.
* **Critérios de Aceite:**
  * Para alterar a senha, é obrigatório fornecer a senha atual válida.
  * A nova senha deve cumprir estritamente a política de senhas fortes (**RN06**).

#### [RF05] Gestão Administrativa de Usuários
* **Descrição:** O Administrador deve poder listar usuários com paginação e filtros (nome, perfil, status, setor), alterar papéis de acesso e alternar status (Ativar/Inativar).
* **Atores:** Administrador.
* **Critérios de Aceite:**
  * O sistema bloqueia sumariamente a inativação ou remoção do perfil de administrador caso seja o **último administrador ativo** do sistema (**RN05**).

---

### 3.2. Módulo de Gestão Patrimonial (Equipamentos)

#### [RF06] Gestão de Setores
* **Descrição:** O sistema deve manter o cadastro de setores organizacionais (identificador sequencial `id_setor`, nome único e sigla).
* **Atores:** Administrador.

#### [RF07] Inventário de Equipamentos
* **Descrição:** Cadastrar, editar, consultar e filtrar ativos de TI contendo: número de patrimônio (único), número de série, marca, modelo, categoria (`COMPUTADOR`, `NOTEBOOK`, `MONITOR`, `IMPRESSORA`, `NOBREAK`, `OUTRO`), status operacional, setor alocado e responsável.
* **Atores:** Administrador, Técnico.
* **Critérios de Aceite:**
  * Validação de unicidade do número de patrimônio (HTTP 409 em duplicidade).
  * O identificador gerado é um `UUID` versão 7 ordenado por tempo.

#### [RF08] Controle de Ciclo de Vida e Baixa Patrimonial
* **Descrição:** O sistema deve registrar as transições de status do equipamento (`EM_USO`, `ESTOQUE`, `EM_MANUTENCAO`, `BAIXADO`).
* **Atores:** Administrador, Técnico (baixa exclusiva para Administrador).
* **Critérios de Aceite:**
  * O status `BAIXADO` é terminal e irreversível.
  * Para efetivar a baixa, o Administrador deve obrigatoriamente anexar o **Termo de Baixa/Descarte** digitalizado (**RN04**).

#### [RF09] Movimentação e Transferência de Ativos
* **Descrição:** Permitir a transferência de um equipamento entre setores ou a alteração do usuário responsável, registrando o histórico de movimentação.
* **Atores:** Administrador, Técnico.

---

### 3.3. Módulo de Service Desk (Chamados)

#### [RF10] Abertura de Chamados Vinculada a Ativo
* **Descrição:** O servidor registra uma solicitação de suporte técnico informando o patrimônio do equipamento, categoria do chamado (`HARDWARE`, `SOFTWARE`, `REDE`, `ACESSO`), prioridade sugerida, título, descrição do defeito e anexos opcionais.
* **Atores:** Qualquer usuário autenticado.
* **Critérios de Aceite:**
  * O sistema valida se o equipamento existe e se seu status é estritamente `EM_USO` (**RN02**). Se não for, rejeita a abertura com HTTP 422 (*Unprocessable Entity*).
  * O sistema gera automaticamente um protocolo único legível no formato `AAAAMMDD-HHMMSS-XXX`.
  * Status inicial fixado em `ABERTO`.

#### [RF11] Gerenciamento da Fila de Chamados
* **Descrição:** Painel central para listar, buscar e filtrar chamados por status, prioridade, categoria, solicitante, técnico e período.
* **Atores:** Administrador, Técnico.
* **Critérios de Aceite:**
  * Permitir atribuição de técnico responsável (`id_tecnico_responsavel`). Ao atribuir, o chamado transiciona automaticamente de `ABERTO` para `EM_ANDAMENTO`.

#### [RF12] Timeline de Atendimento e Interações
* **Descrição:** Cada chamado possui uma timeline colaborativa imutável (`historico_chamado`), onde solicitante e técnicos trocam mensagens, pareceres de diagnóstico e novos anexos.
* **Atores:** Solicitante do chamado, Técnico responsável, Administrador.
* **Critérios de Aceite:**
  * Não é permitida edição ou exclusão física de mensagens já publicadas na timeline (garantia de auditoria).

#### [RF13] Conclusão com Registro de Solução Técnica
* **Descrição:** Ao encerrar um chamado (status `CONCLUIDO`), o técnico responsável deve obrigatoriamente registrar um parecer formal de **Solução Técnica** descrevendo a ação corretiva realizada (**RN07**).
* **Atores:** Técnico, Administrador.
* **Critérios de Aceite:**
  * O campo `solucao_tecnica` é obrigatório na transição para `CONCLUIDO`.
  * Preenchimento automático de `data_fechamento`.
  * Um chamado concluído torna-se imutável (**RN03**).

#### [RF14] Cancelamento com Justificativa
* **Descrição:** Permitir o cancelamento de um chamado antes de sua conclusão, exigindo o preenchimento de justificativa formal (**RN08**).
* **Atores:** Solicitante (apenas se status for `ABERTO`) ou Administrador/Técnico.

---

### 3.4. Módulo de Notificações e Relatórios

#### [RF15] Central de Notificações Multicanal
* **Descrição:** O sistema deve disparar notificações automáticas in-app e por e-mail nos eventos críticos: abertura de ticket, atualização de status, nova mensagem na timeline e designação técnica.
* **Critérios de Aceite:**
  * Falhas no servidor SMTP de e-mail não devem abortar a transação principal do banco (processamento assíncrono `@Async`).

#### [RF16] Dashboard Operacional e Relatórios Gerenciais
* **Descrição:** Exibição de métricas em tempo real na tela inicial (Total de Chamados Abertos, Em Andamento, Concluídos no Mês, Equipamentos em Manutenção) e exportação de relatórios tabulares em PDF/CSV com filtro obrigatório de período (**RN09**).
* **Atores:** Administrador.

---

## 4. Requisitos Não-Funcionais (RNF)

| ID | Categoria | Especificação Técnica |
| :--- | :--- | :--- |
| **RNF01** | **Arquitetura** | Arquitetura cliente-servidor desatrelada com API RESTful em **Spring Boot 4 (Java 21)** e Frontend SPA em **React 19 (TypeScript + Vite)**. Comunicação estritamente via JSON. |
| **RNF02** | **Padronização de Erros** | Todas as exceções da API devem retornar respostas padronizadas conforme a **RFC 7807 (Problem Details for HTTP APIs)** via `@RestControllerAdvice`. |
| **RNF03** | **Segurança & Autenticação** | Comunicação sob HTTPS. Autenticação stateless via JWT Bearer Token. Senhas criptografadas com **BCrypt** (fator de custo 10 ou 12). Configuração estrita de **CORS**. |
| **RNF04** | **Proteção contra IDOR** | Identificadores públicos de recursos de negócio devem utilizar **UUIDv7** (128 bits com ordenação temporal), eliminando vulnerabilidades de enumeração direta em URLs. |
| **RNF05** | **Performance & Conexões** | Uso de pool de conexões de alta performance **HikariCP**. Tempo de resposta dos endpoints de leitura inferior a 250ms em percentil 95 (P95). |
| **RNF06** | **Versionamento de Schema** | 100% da evolução estrutural do PostgreSQL deve ser gerenciada pelo **Flyway**, garantindo reprodutibilidade automatizada em ambientes de desenvolvimento, teste e produção. |
| **RNF07** | **Conformidade LGPD** | Anonimização de dados cadastrais mediante solicitação formal e retenção protegida de logs de auditoria por período legal. |

---

## 5. Regras de Negócio (RN)

* **RN01 — Composição de Perfis (RBAC):** Um usuário pode acumular múltiplos perfis (ex: um Servidor que também é Técnico). As permissões nos endpoints são verificadas de forma granular via `@PreAuthorize`.
* **RN02 — Validação de Ativo para Chamado:** A abertura de chamado só é aceita se o equipamento estiver cadastrado e com o status `EM_USO`. Equipamentos em `ESTOQUE`, `EM_MANUTENCAO` ou `BAIXADO` são sumariamente rejeitados.
* **RN03 — Máquina de Estados e Imutabilidade do Chamado:** O ciclo de vida segue o fluxo: `ABERTO` $\rightarrow$ `EM_ANDAMENTO` $\rightarrow$ `PENDENTE` $\rightarrow$ `CONCLUIDO` (ou `CANCELADO`). Chamados em status terminal (`CONCLUIDO` ou `CANCELADO`) são imutáveis e não podem ser reabertos.
* **RN04 — Obrigatoriedade de Termo de Baixa:** A baixa lógica de um equipamento (`BAIXADO`) exige obrigatoriamente a anexação de documento comprobatório em PDF/imagem.
* **RN05 — Proteção do Último Administrador:** O sistema não permite inativar, deletar ou revogar o perfil `ROLE_ADMINISTRADOR` do último usuário administrador ativo.
* **RN06 — Política de Senhas Fortes:** As senhas devem possuir no mínimo 8 caracteres, contendo pelo menos 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial, não podendo coincidir com o nome ou e-mail.
* **RN07 — Solução Técnica Obrigatória:** Nenhum chamado pode transicionar para o status `CONCLUIDO` sem que o técnico preencha o campo de parecer com no mínimo 10 caracteres.
* **RN08 — Justificativa de Cancelamento:** O cancelamento de chamado exige justificativa obrigatória registrada na timeline.
* **RN09 — Intervalo Obrigatório em Relatórios:** A emissão de relatórios históricos exige intervalo explícito de datas (início e fim) de no máximo 365 dias para evitar degradação de performance do banco.
* **RN10 — Resiliência de Notificações:** Falhas no envio de notificações externas (e-mail) devem ser registradas em log, mas nunca devem realizar *rollback* da operação principal que disparou o evento.

---

## 6. Product Backlog — Visão Comercial (Release 2.0 / SaaS)

Funcionalidades priorizadas para evolução futura do produto comercial:

| ID | Épico | História / Funcionalidade | Critério Comercial |
| :--- | :--- | :--- | :--- |
| **PB01** | SLA Engine | Motor de SLA dinâmico com cálculo de prazos por prioridade e pausa automática em status `PENDENTE`. | Indispensável para contratos corporativos com metas de atendimento. |
| **PB02** | CSAT | Pesquisa de Satisfação ao término do atendimento (avaliação de 1 a 5 estrelas e comentário). | Avaliação de desempenho de equipes técnicas e NPS interno. |
| **PB03** | QR Code Asset | Geração automática de etiquetas com QR Code e leitura via câmera para inventário rápido. | Reduz em 80% o tempo de abertura de chamados presenciais. |
| **PB04** | Knowledge Base | Base de Conhecimento com busca inteligente e sugestão automática de artigos no formulário de abertura. | Redução de até 30% no volume de abertura de tickets repetitivos. |
| **PB05** | Enterprise SSO | Autenticação via Single Sign-On (OAuth2 / SAML) com Google Workspace e Microsoft Azure AD. | Exigência técnica de grandes instituições e empresas privadas. |
| **PB06** | Financeiro ITAM | Cálculo automático de TCO (Custo Total de Posse) e depreciação contábil linear de equipamentos. | Conexão do setor de TI com o setor financeiro e contábil. |
