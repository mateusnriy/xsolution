package xsolution.model.entity;

import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public class Administrador extends Usuario {

    public Administrador() {
        this.perfil = PerfilUsuario.ADMINISTRADOR;
    }

    public Administrador(String id, String nome, String email, String senhaHash, StatusUsuario status) {
        super(id, nome, email, senhaHash, PerfilUsuario.ADMINISTRADOR, status);
    }

    public void resetarSenhaHash(Usuario usuario, String novaSenha) {
        usuario.setSenhaHash(novaSenha);
    }

    public void ativarUsuario(Usuario usuario) {
        usuario.setStatus(StatusUsuario.ATIVO);
    }

}
