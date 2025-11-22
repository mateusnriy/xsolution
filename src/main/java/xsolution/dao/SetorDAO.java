package xsolution.dao;

import java.util.List;
import xsolution.model.entity.Setor;

public interface SetorDAO {
    List<Setor> findAll();
    Setor findById(Integer id);
}