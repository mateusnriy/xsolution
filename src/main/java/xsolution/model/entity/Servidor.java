package xsolution.model.entity;

import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public class Servidor extends Usuario {

    private String lotacao;

    public Servidor() {
        this.perfil = PerfilUsuario.COMUM;
    }

    public Servidor(String id, String nome, String email, String senha, String lotacao, StatusUsuario status) {
        super(id, nome, email, senha, PerfilUsuario.COMUM, status);
        this.lotacao = lotacao;
    }

    public String getLotacao() {
        return lotacao;
    }

    public void setLotacao(String lotacao) {
        this.lotacao = lotacao;
    }

}
