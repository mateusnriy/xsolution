package xsolution.model.entity;

import java.util.Objects;

public class Setor {

    private Integer id;
    private String nome;
    private String sigla;

    public Setor() {
    }

    public Setor(Integer id, String nome, String sigla) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Setor other = (Setor) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return this.sigla + " - " + this.nome;
    }
}