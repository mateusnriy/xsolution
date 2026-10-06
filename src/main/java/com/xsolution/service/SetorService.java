package xsolution.service;

import java.util.List;
import xsolution.dao.SetorDAO;
import xsolution.dao.SetorDAOImpl;
import xsolution.exception.DbException;
import xsolution.model.entity.Setor;

public class SetorService {

    private SetorDAO dao;

    public SetorService() {
        this.dao = new SetorDAOImpl();
    }

    public List<Setor> listarTodos() {
        return dao.findAll();
    }

    public Setor buscarPorId(Integer id) {
        if (id == null) {
            throw new DbException("ID do setor não pode ser nulo.");
        }
        return dao.findById(id);
    }
}