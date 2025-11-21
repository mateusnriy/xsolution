package xsolution.dao;

import xsolution.model.entity.Chamado;
import xsolution.model.enums.StatusChamado;
import java.sql.Timestamp;
import java.util.List;

public interface ChamadoDAO {
    void create(Chamado chamado);
    List<Chamado> findAll();
    Chamado findById(int id);
    void update(Chamado chamado);
    List<Chamado> findByFilters(String protocolo, StatusChamado status, Timestamp dataInicio);

}