package xsolution.model.entity;

import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public class Servidor extends Usuario {

    public Servidor() {
        this.perfil = PerfilUsuario.COMUM;
    }

    public Servidor(String id, String nome, String email, String senha, StatusUsuario status) {
        super(id, nome, email, senha, PerfilUsuario.COMUM, status);
    }
}
