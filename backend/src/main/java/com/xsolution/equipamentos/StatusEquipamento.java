package com.xsolution.equipamentos;

public enum StatusEquipamento {
    EM_USO,
    EM_MANUTENCAO,
    ESTOQUE,
    BAIXA;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
}
