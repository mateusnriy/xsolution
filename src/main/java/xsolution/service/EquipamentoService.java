package xsolution.service;

import java.util.List;
import xsolution.dao.EquipamentoDAO;
import xsolution.dao.EquipamentoDAOImpl;
import xsolution.exception.NegocioException;
import xsolution.model.entity.Equipamento;

public class EquipamentoService {

    private EquipamentoDAO dao = new EquipamentoDAOImpl();

    public void salvar(Equipamento equipamento) {
        validarCamposObrigatorios(equipamento);

        Equipamento existente = dao.buscarPorPatrimonio(equipamento.getNumPatrimonio());
        
        if (existente != null && !existente.equals(equipamento)) {
             throw new NegocioException("Já existe um equipamento com este número de patrimônio.");
        }
        if (equipamento.getId() == null && existente != null) {
            throw new NegocioException("Já existe um equipamento com este número de patrimônio.");
        }

        if (equipamento.getId() == null) {
            dao.salvar(equipamento);
        } else {
            dao.atualizar(equipamento);
        }
    }

    public List<Equipamento> listarTodos() {
        return dao.listarTodos();
    }
    
    public void remover(Equipamento equipamento) {
        if (equipamento == null || equipamento.getId() == null) {
            throw new NegocioException("Equipamento inválido para remoção.");
        }
        dao.deletar(equipamento.getId());
    }

    private void validarCamposObrigatorios(Equipamento e) {
        if (e.getNumPatrimonio() == null || e.getNumPatrimonio().trim().isEmpty()) {
            throw new NegocioException("O número de patrimônio é obrigatório.");
        }
        if (e.getMarca() == null || e.getMarca().trim().isEmpty()) {
            throw new NegocioException("A marca é obrigatória.");
        }
        if (e.getModelo() == null || e.getModelo().trim().isEmpty()) {
            throw new NegocioException("O modelo é obrigatório.");
        }
        if (e.getTipo() == null) {
            throw new NegocioException("O tipo do equipamento é obrigatório.");
        }
        if (e.getStatus() == null) {
            throw new NegocioException("O status do equipamento é obrigatório.");
        }
    }
}