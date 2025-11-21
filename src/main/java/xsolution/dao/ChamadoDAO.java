package xsolution.dao;

import xsolution.model.entity.Usuario;
import xsolution.model.entity.Chamado;
import xsolution.model.enums.StatusChamado;
import java.sql.Timestamp;
import java.util.List;

public interface ChamadoDAO {

    List<Chamado> listarChamados();

    List<Chamado> listarPorSolicitante(Usuario solicitante);

    List<Chamado> listarPorFiltros(String protocolo, StatusChamado status, Timestamp dataInicio);

    void criar(Chamado chamado);

    void atualizar(Chamado chamado);

}