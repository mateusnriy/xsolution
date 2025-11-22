package xsolution.dao;

import java.util.List;
import xsolution.model.entity.Equipamento;

public interface EquipamentoDAO {
    void salvar(Equipamento equipamento);
    void atualizar(Equipamento equipamento);
    void deletar(int id);
    List<Equipamento> listarTodos();
    Equipamento buscarPorPatrimonio(String numPatrimonio);
    Equipamento buscarPorId(int id);
}