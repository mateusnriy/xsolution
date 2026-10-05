CREATE TABLE setor (
    id_setor BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    sigla VARCHAR(20) NOT NULL
);

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ATIVO', 'INATIVO')),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    id_setor BIGINT NOT NULL,
    CONSTRAINT fk_usuario_setor
        FOREIGN KEY(id_setor) 
        REFERENCES setor(id_setor)
);

CREATE TABLE usuario_perfil(
    id_usuario UUID NOT NULL,
    perfil VARCHAR(50) NOT NULL CHECK (perfil IN ('ADMINISTRADOR', 'TECNICO', 'SERVIDOR')),
    PRIMARY KEY (id_usuario, perfil),
    CONSTRAINT fk_usuario_perfil
        FOREIGN KEY(id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);


CREATE TABLE equipamento (
    id_equipamento UUID PRIMARY KEY,
    num_patrimonio VARCHAR(100) UNIQUE NOT NULL,
    num_serie VARCHAR(100) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,   
    status VARCHAR(50) NOT NULL CHECK (status IN ('EM_USO', 'ESTOQUE', 'EM_MANUTENCAO', 'BAIXADO')),
    
    data_aquisicao DATE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    id_setor BIGINT NOT NULL,
    id_usuario UUID,
    
    CONSTRAINT fk_equipamento_setor
        FOREIGN KEY(id_setor) 
        REFERENCES setor(id_setor)
        ON DELETE RESTRICT,
    CONSTRAINT fk_equipamento_usuario
        FOREIGN KEY(id_usuario) 
        REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT
);

CREATE TABLE chamado (
    id_chamado UUID PRIMARY KEY,
    protocolo VARCHAR(50) UNIQUE NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'PENDENTE', 'CONCLUIDO', 'CANCELADO')),
    
    data_abertura TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITH TIME ZONE,
    
    id_solicitante UUID NOT NULL,
    id_tecnico_responsavel UUID,
    id_equipamento UUID NOT NULL,

    CONSTRAINT fk_chamado_solicitante
        FOREIGN KEY(id_solicitante) 
        REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_responsavel
        FOREIGN KEY(id_tecnico_responsavel) 
        REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT,
    CONSTRAINT fk_chamado_equipamento
        FOREIGN KEY(id_equipamento) 
        REFERENCES equipamento(id_equipamento)
        ON DELETE RESTRICT
);

CREATE TABLE historico_chamado(
    id_historico UUID PRIMARY KEY,
    id_chamado UUID NOT NULL
        REFERENCES chamado(id_chamado)
        ON DELETE CASCADE,
    id_autor UUID NOT NULL
        REFERENCES usuario(id_usuario),
    mensagem TEXT NOT NULL,
    data_alteracao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE anexo (
    id_anexo UUID PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    extensao VARCHAR(100),
    id_chamado UUID NOT NULL,
    CONSTRAINT fk_anexo_chamado
        FOREIGN KEY(id_chamado) 
        REFERENCES chamado(id_chamado)
        ON DELETE CASCADE
);

CREATE TABLE notificacao (
    id_notificacao UUID PRIMARY KEY,
    mensagem TEXT NOT NULL,
    gatilho VARCHAR(100),
    tipo VARCHAR(50),
    data_envio TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status_envio VARCHAR(50) NOT NULL,

    id_destinatario UUID NOT NULL,
    lida BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_notificacao_destinatario
        FOREIGN KEY(id_destinatario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

