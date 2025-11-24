package xsolution.dao;

import xsolution.model.entity.Equipamento;

public interface EquipamentoDAO {
    Equipamento buscarPorPatrimonio(String numPatrimonio);
}
