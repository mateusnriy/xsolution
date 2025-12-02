package xsolution.service;

import java.util.List;
import xsolution.dao.EquipamentoDAO;
import xsolution.dao.EquipamentoDAOImpl;
import xsolution.exception.DbException;
import xsolution.model.entity.Equipamento;

public class EquipamentoService {

    private EquipamentoDAO dao = new EquipamentoDAOImpl();

    public void salvar(Equipamento equipamento) {
        validarCamposObrigatorios(equipamento);

        Equipamento existente = dao.buscarPorPatrimonio(equipamento.getNumPatrimonio());

        if (existente != null && !existente.equals(equipamento)) {
            throw new DbException("Já existe um equipamento com este número de patrimônio.");
        }
        if (equipamento.getId() == null && existente != null) {
            throw new DbException("Já existe um equipamento com este número de patrimônio.");
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

    public List<Equipamento> listarParaNovoChamado() {
        return dao.listarDisponiveisParaChamado();
    }

    public Equipamento buscarPorPatrimonio(String numPatrimonio) {
        if (numPatrimonio == null || numPatrimonio.trim().isEmpty()) {
            throw new DbException("O número do patrimônio não pode ser vazio para busca.");
        }
        return dao.buscarPorPatrimonio(numPatrimonio);
    }

    public void remover(Equipamento equipamento) {
        if (equipamento == null || equipamento.getId() == null) {
            throw new DbException("Equipamento inválido para remoção.");
        }
        dao.deletar(equipamento.getId());
    }

    private void validarCamposObrigatorios(Equipamento e) {
        if (e.getNumPatrimonio() == null || e.getNumPatrimonio().trim().isEmpty()) {
            throw new DbException("O número de patrimônio é obrigatório.");
        }
        if (e.getMarca() == null || e.getMarca().trim().isEmpty()) {
            throw new DbException("A marca é obrigatória.");
        }
        if (e.getModelo() == null || e.getModelo().trim().isEmpty()) {
            throw new DbException("O modelo é obrigatório.");
        }
        if (e.getTipo() == null) {
            throw new DbException("O tipo do equipamento é obrigatório.");
        }
        if (e.getStatus() == null) {
            throw new DbException("O status do equipamento é obrigatório.");
        }
    }
}