package xsolution.model.enums;

public enum StatusChamado {
    ABERTO,
    EM_ANDAMENTO,
    PENDENTE,   
    CONCLUIDO,    
    CANCELADO;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
}
