# X Solution - Sistema de Gestão de TI

## Visão Geral

O **X Solution** é um sistema de desktop desenvolvido para otimizar a gestão de departamentos de Tecnologia da Informação. A aplicação centraliza o controlo de inventário de equipamentos (hardware) e o gerenciamento de chamados de suporte técnico (Service Desk).

Este projeto foi construído com foco na aplicação de conceitos fundamentais de **Engenharia de Software**, **Orientação a Objetos** e **Banco de Dados**, utilizando uma arquitetura MVC (*Model-View-Controller*) pura, sem a utilização de frameworks de alto nível (como Spring ou Hibernate), para demonstrar domínio sobre a implementação de padrões de projeto e persistência via JDBC.

---

## Tecnologias e Arquitetura

A solução adota uma arquitetura em camadas bem definida para garantir a separação de responsabilidades:

* **View:** Interface gráfica construída com **JavaFX**, FXML e CSS.
* **Controller:** Gerenciamento de eventos e ligação entre a interface e a lógica de negócios.
* **Service:** Camada de regras de negócio, validações e segurança.
* **DAO (Data Access Object):** Camada de persistência responsável pela comunicação direta com o banco de dados via **JDBC**.
* **Model/Entity:** Representação dos objetos de domínio.

### Stack Tecnológica
* **Linguagem:** Java JDK 21 (LTS)
* **Gestor de Dependências:** Apache Maven 3.x
* **Interface Gráfica:** JavaFX 21
* **Banco de Dados:** PostgreSQL 14+
* **Segurança:** Biblioteca JBcrypt (para hash de senhas)

---

## Configuração do Ambiente

### 1. Pré-requisitos
Para compilar e executar este projeto, o ambiente deve possuir:
* Java JDK 21 instalado.
* Maven configurado nas variáveis de ambiente.
* Uma instância de PostgreSQL em execução.

### 2. Banco de Dados (Docker)
Recomendamos o uso do Docker para criar o ambiente de banco de dados isolado. Execute o seguinte comando no terminal **(substitua pelo nome do container, login, senha e nome do banco corretos)**:

```bash
docker run --name NOME_DO_CONTAINER \
  -p 5433:5432 \
  -e POSTGRES_USER=SEU_LOGIN \
  -e POSTGRES_PASSWORD=SUA_SENHA \
  -e POSTGRES_DB=NOME_DO_BANCO \
  -d postgres
```
**Nota: A aplicação conecta-se por padrão na porta 5433 para evitar conflitos com instalações locais do Postgres.**

### 3. Criação do Esquema (DDL)
Utilize um cliente SQL (sugiro o DBeaver) para conectar ao banco x_solution e executar o script de criação inicial:
#### Siga o passo a passo abaixo após criar o container do banco e conexão
- Copie o arquivo `modeloScriptDDL.sql` na raiz do projeto para uma pasta qualquer
- Remova o arquivo da raiz do projeto
- Rode o script no cliente SQL (**sugiro o DBeaver**)

### 4. Configuração da Aplicação (conexão com o banco e execução)
A aplicação utiliza um arquivo de propriedades para credenciais seguras.

Vá até ``src/main/resources``.

Renomeie ``db.properties.example`` para ``db.properties``.

Confirme se as credenciais batem com o seu container Docker. Exemplo:
```bash
db.url="jdbc:postgresql://localhost+PORTA+NOME_DO_BANCO"
db.user="root"
db.password="root"
```
#### Como Executar
Compilar o projeto
Na raiz do projeto, execute (no terminal):
```bash
mvn clean install
```
Rodar a aplicação
```bash
mvn javafx:run
```
Ou execute a classe principal xsolution.application.Main através da sua IDE.

---

## Funcionalidades Principais

O sistema está dividido em módulos estratégicos para atender diferentes perfis de utilizador:

### Módulo de Acesso e Perfil
* **Login Seguro:** Autenticação robusta com validação de credenciais e *hashing* de senhas.
* **Meu Perfil:** Funcionalidade de autoatendimento onde o utilizador pode atualizar os seus dados cadastrais e alterar a senha (com exigência de validação da senha atual para garantir a segurança da operação).

### Módulo de Equipamentos
* **Gestão de Ativos (CRUD):** Cadastro, leitura, atualização e remoção completa de ativos de TI.
* **Controlo de Ciclo de Vida:** Monitorização precisa do status dos equipamentos (*Em uso, Estoque, Manutenção*).
* **Localização:** Associação direta de cada ativo a um setor específico da organização.

### Módulo de Chamados
* **Service Desk:** Interface para abertura de *tickets* por utilizadores comuns (Servidores).
* **Regras de Negócio:** O sistema implementa validações estritas, como o bloqueio de abertura de chamados para equipamentos que não estejam com o status "Em Uso".
* **Workflow:** Gestão do fluxo de atendimento (*Aberto* &rarr; *Em Andamento* &rarr; *Concluído*).
* **Atribuição:** Permite designar técnicos responsáveis para cada solicitação.

### Módulo Administrativo
* **Gestão de Utilizadores:** Controlo total sobre contas e definição de perfis de acesso (*RBAC Simplificado*).
* **Relatórios:** Geração e exportação de dados gerenciais em formato **CSV** para análise externa.

---

## Padrões de Projeto Aplicados

A arquitetura do projeto segue rigorosamente padrões de engenharia de software para garantir manutenibilidade e escalabilidade:

* **Singleton:** Utilizado para garantir instâncias únicas em pontos críticos, como na gestão da conexão com o banco (`DB.java`) e no controlo da sessão do utilizador logado (`Sessao.java`).
* **DAO (Data Access Object):** Implementado para isolar a lógica de negócios da lógica de persistência, abstraindo completamente as operações SQL.
* **MVC (Model-View-Controller):** Estrutura base da aplicação, separando a interface (View/FXML), a lógica de controlo (Controller) e as regras de negócio/dados (Model/Service).
* **State (Implícito):** Aplicado na gestão das transições de status das entidades de *Chamado* e *Equipamento*.

---

## Desenvolvedores

| Membro | GitHub |
| :--- | :--- |
| **Lucas Lima** | [@lucasblima-dev](https://www.github.com/lucasblima-dev) |
| **Mateus Neri** | [@mateus](https://github.com/mateusnriy) |
| **Kaio França** | [@kaio](https://github.com/kaiofranca) |