package com.xsolution.chamados;

import java.sql.Timestamp;
import java.util.List;

import com.xsolution.usuarios.Usuario;

public interface ChamadoDAO {

    List<Chamado> listarChamados();

    List<Chamado> listarPorSolicitante(Usuario solicitante);

    List<Chamado> listarPorFiltros(String protocolo, StatusChamado status, Timestamp dataInicio);

    void criar(Chamado chamado);

    void atualizar(Chamado chamado);

}