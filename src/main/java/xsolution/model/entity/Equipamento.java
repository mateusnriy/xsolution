package xsolution.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;
import xsolution.model.enums.TipoSetor;

public class Equipamento {
	private Integer id;
    private String numPatrimonio;
    private String numSerie;
    private String marca;
    private String modelo;
    private LocalDateTime dataCriacao;
    private TipoEquipamento tipo;
    private TipoSetor setor;
    private StatusEquipamento status;
    private Usuario responsavel;

    public Equipamento() { }

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
    
    public LocalDateTime getData() {
        return dataCriacao;
    }

    public void setData(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public TipoEquipamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipamento tipo) {
        this.tipo = tipo;
    }

    public TipoSetor getSetor() {
        return setor;
    }

    public void setSetor(TipoSetor setor) {
        this.setor = setor;
    }

    public StatusEquipamento getStatus() {
        return status;
    }

    public void setStatus(StatusEquipamento status) {
        this.status = status;
    }

    public void alterarStatus(StatusEquipamento novoStatus) {
        this.status = novoStatus;
    }
    
    public Usuario getResponsavel() { 
    	return responsavel; 
    }
    
    public void setResponsavel(Usuario responsavel) { 
    	this.responsavel = responsavel; 
    }
    
    @Override
    public String toString() {
        return tipo + " " + marca + " " + modelo + " (Pat: " + numPatrimonio + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipamento that = (Equipamento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
