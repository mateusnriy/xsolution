package xsolution.model.entity;

import java.util.Objects;

import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusUsuario;

public abstract class Usuario {

    private String id;
    private String nome;
    private String email;
    private String senhaHash;
    protected PerfilUsuario perfil;
    private StatusUsuario status;
    private Setor setor;

    public Usuario() {
    }

    public Usuario(String id, String nome, String email, String senhaHash, PerfilUsuario perfil,
            StatusUsuario status) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public boolean validarSenha(String senhaFornecida) {
        return this.senhaHash != null && this.senhaHash.equals(senhaFornecida);
    }

    public boolean validarEmail(String emailFornecido) {
        return this.email != null && this.email.equalsIgnoreCase(emailFornecido);
    }

    public void inativar() {
        this.status = StatusUsuario.INATIVO;
    }

    @Override
    public String toString() {
        return nome + " (" + email + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Usuario usuario = (Usuario) o;

        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
