package com.xsolution.setor;

import java.util.List;

import com.xsolution.model.entity.Setor;

public interface SetorDAO {
    List<Setor> findAll();
    Setor findById(Integer id);
}