-- SCRIPT DDL - X SOLUTION (Versão Atualizada)

-- 1. Limpeza de tabelas existentes
DROP TABLE IF EXISTS Usuario_notificacao CASCADE;
DROP TABLE IF EXISTS Anexo CASCADE;
DROP TABLE IF EXISTS Notificacao CASCADE;
DROP TABLE IF EXISTS Chamado CASCADE;
DROP TABLE IF EXISTS Equipamento CASCADE;
DROP TABLE IF EXISTS Usuario CASCADE;
DROP TABLE IF EXISTS Setor CASCADE;
-- Removendo tabelas antigas que não serão mais usadas
DROP TABLE IF EXISTS Registro_chamado CASCADE;
DROP TABLE IF EXISTS Registro_equipamentos CASCADE;

-- 2. CRIAÇÃO DAS TABELAS

-- Tabela Setor
CREATE TABLE Setor (
    idSetor SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sigla VARCHAR(20) NOT NULL
);

-- Tabela Notificacao
CREATE TABLE Notificacao (
    idNotificacao SERIAL PRIMARY KEY,
    mensagem TEXT NOT NULL,
    gatilho VARCHAR(100),
    tipo VARCHAR(50),
    dataEnvio TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    statusEnvio VARCHAR(50)
);

-- Tabela Usuario
-- O ID é VARCHAR(50) para permitir prefixos como 'A001', 'T001', 'S001'
CREATE TABLE Usuario (
    idUsuario VARCHAR(50) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ATIVO', 'INATIVO')),
    tipoUsuario VARCHAR(50) NOT NULL,
    idSetor INT,
    CONSTRAINT fk_usuario_setor
        FOREIGN KEY(idSetor) 
        REFERENCES Setor(idSetor)
);

-- Tabela Equipamento
CREATE TABLE Equipamento (
    idEquipamento SERIAL PRIMARY KEY,
    numPatrimonio VARCHAR(100) NOT NULL UNIQUE,
    numSerie VARCHAR(100),
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,   
    status VARCHAR(50) NOT NULL,
    
    dataCriacao TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    data_aquisicao DATE,
    
    idSetor INT,
    idUsuario VARCHAR(50),
    
    CONSTRAINT fk_equipamento_setor
        FOREIGN KEY(idSetor) 
        REFERENCES Setor(idSetor),
    CONSTRAINT fk_equipamento_usuario
        FOREIGN KEY(idUsuario) 
        REFERENCES Usuario(idUsuario)
);

-- Tabela Chamado
CREATE TABLE Chamado (
    idChamado SERIAL PRIMARY KEY,
    protocolo VARCHAR(50) UNIQUE,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    
    dataAbertura TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    dataFechamento TIMESTAMPTZ,
    
    idUsuarioCriador VARCHAR(50),
    idUsuarioResponsavel VARCHAR(50),
    idEquipamento INT,
    
    CONSTRAINT fk_chamado_criador
        FOREIGN KEY(idUsuarioCriador) 
        REFERENCES Usuario(idUsuario),
    CONSTRAINT fk_chamado_responsavel
        FOREIGN KEY(idUsuarioResponsavel) 
        REFERENCES Usuario(idUsuario),
    CONSTRAINT fk_chamado_equipamento
        FOREIGN KEY(idEquipamento) 
        REFERENCES Equipamento(idEquipamento)
);

-- Tabela Anexo
CREATE TABLE Anexo (
    idAnexo SERIAL PRIMARY KEY,
    nomeArquivo VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    idChamado INT,
    CONSTRAINT fk_anexo_chamado
        FOREIGN KEY(idChamado) 
        REFERENCES Chamado(idChamado)
);

-- Tabela de Junção para Notificações
CREATE TABLE Usuario_notificacao (
    idUsuario VARCHAR(50),
    idNotificacao INT,
    CONSTRAINT pk_usuario_notificacao
        PRIMARY KEY (idUsuario, idNotificacao),
    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY(idUsuario) 
        REFERENCES Usuario(idUsuario),
    CONSTRAINT fk_notificacao_not
        FOREIGN KEY(idNotificacao) 
        REFERENCES Notificacao(idNotificacao)
);

-- 3. CARGA DE DADOS INICIAIS (Obrigatório para o sistema funcionar)

-- Inserir Setores Padrão
INSERT INTO Setor (nome, sigla) VALUES 
('Departamento de Tecnologia da Informação', 'DTI'),
('Recursos Humanos', 'RH'),
('Secretaria Administrativa', 'SEC'),
('Ouvidoria', 'OUV'),
('Almoxarifado', 'ALM');

-- Setor genérico para criação de contas
INSERT INTO Setor (idSetor, nome, sigla) VALUES (999, 'Em Análise', 'N/A');

-- --------------------------------------------------------------------------
-- APÓS RODAR OS COMANDOS ACIMA, RODE O CÓDIGO ABAIXO, SUBSTITUINDO O E-MAIL 
-- PARA O DO USUÁRIO DESEJADO CRIADO DIRETAMENTE NO APP, PARA TORNA-LO ADMIN:

--UPDATE Usuario 
--SET tipoUsuario = 'ADMINISTRADOR' 
--WHERE email = 'seuemail@mail.com';