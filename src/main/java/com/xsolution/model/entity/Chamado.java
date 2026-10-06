package xsolution.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import xsolution.model.enums.StatusChamado;

public class Chamado {

    private Integer id;
    private String protocolo;
    private String titulo;
    private String descricao;
    private StatusChamado status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String anexoPath; 

    private Usuario solicitante;
    private Usuario tecnicoResponsavel;
    private Equipamento equipamento;

    
    public Chamado() {}

    public Integer getId() { 
    	return id; 
    }
    
    public void setId(Integer id) { 
    	this.id = id; 
    }
    
    public String getProtocolo() { 
    	return protocolo; 
    }
    
    public void setProtocolo(String protocolo) { 
    	this.protocolo = protocolo; 
    }
    
    public String getTitulo() { 
    	return titulo; 
    }
    
    public void setTitulo(String titulo) { 
    	this.titulo = titulo; 
    }
    
    public String getDescricao() { 
    	return descricao; 
    }
    
    public void setDescricao(String descricao) { 
    	this.descricao = descricao;
    }
    
    public StatusChamado getStatus() { 
    	return status; 
    }
    
    public void setStatus(StatusChamado status) { 
    	this.status = status; 
    }
    
    public LocalDateTime getDataAbertura() { 
    	return dataAbertura; 
    }
    
    public void setDataAbertura(LocalDateTime dataAbertura) { 
    	this.dataAbertura = dataAbertura; 
    }
    
    public LocalDateTime getDataFechamento() { 
    	return dataFechamento; 
    }
    
    public void setDataFechamento(LocalDateTime dataFechamento) { 
    	this.dataFechamento = dataFechamento; 
    }
    
    public String getAnexoPath() { 
    	return anexoPath; 
    }
    
    public void setAnexoPath(String anexoPath) { 
    	this.anexoPath = anexoPath; 
    }
    
    public Usuario getSolicitante() { 
    	return solicitante;
    }
    
    public void setSolicitante(Usuario solicitante) { 
    	this.solicitante = solicitante; 
    }
    
    public Usuario getTecnicoResponsavel() { 
    	return tecnicoResponsavel; 
    }
    
    public void setTecnicoResponsavel(Usuario tecnicoResponsavel) { 
    	this.tecnicoResponsavel = tecnicoResponsavel; 
    }
    
    public Equipamento getEquipamento() { 
    	return equipamento; 
    }
    
    public void setEquipamento(Equipamento equipamento) { 
    	this.equipamento = equipamento; 
    }
    
    @Override
    public boolean equals(Object o) {
    	
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chamado chamado = (Chamado) o;
        return Objects.equals(id, chamado.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
