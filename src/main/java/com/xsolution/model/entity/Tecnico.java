package xsolution.model.entity;

import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public class Tecnico extends Usuario {

    public Tecnico() {
        this.perfil = PerfilUsuario.TECNICO;
    }

    public Tecnico(String id, String nome, String email, String senha, StatusUsuario status) {
        super(id, nome, email, senha, PerfilUsuario.TECNICO, status);
    }

}
