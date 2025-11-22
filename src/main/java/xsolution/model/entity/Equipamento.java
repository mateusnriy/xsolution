package xsolution.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;

public class Equipamento {

    private Integer id;
    private String numPatrimonio;
    private String numSerie;
    private String marca;
    private String modelo;
    private LocalDateTime dataCriacao;
    private LocalDate dataAquisicao;
    private TipoEquipamento tipo;
    private StatusEquipamento status;
    private Setor setor;
    private Usuario responsavel;

    public Equipamento() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumPatrimonio() {
        return numPatrimonio;
    }

    public void setNumPatrimonio(String numPatrimonio) {
        this.numPatrimonio = numPatrimonio;
    }

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public TipoEquipamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipamento tipo) {
        this.tipo = tipo;
    }

    public StatusEquipamento getStatus() {
        return status;
    }

    public void setStatus(StatusEquipamento status) {
        this.status = status;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Usuario responsavel) {
        this.responsavel = responsavel;
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
        Equipamento other = (Equipamento) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public String toString() {
        return marca + " " + modelo + " - " + numPatrimonio;
    }
}