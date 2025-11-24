package xsolution.service;

import java.time.LocalDateTime;
import java.util.List;

import xsolution.dao.ChamadoDAO;
import xsolution.dao.ChamadoDAOImpl;
import xsolution.exception.DbException;
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Equipamento;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusChamado;
import xsolution.model.enums.StatusEquipamento;

public class ChamadoService {

    private final ChamadoDAO chamadoDAO;

    public ChamadoService() {
        this.chamadoDAO = new ChamadoDAOImpl();
    }

    public void abrirChamado(Chamado chamado) {

        if (chamado.getTitulo() == null || chamado.getTitulo().trim().isEmpty()) {
            throw new DbException("O título do chamado é obrigatório.");
        }
        if (chamado.getDescricao() == null || chamado.getDescricao().trim().isEmpty()) {
            throw new DbException("A descrição do chamado é obrigatória.");
        }
        if (chamado.getSolicitante() == null) {
            throw new DbException("O chamado deve ter um solicitante vinculado.");
        }

        Equipamento equipamento = chamado.getEquipamento();
        if (equipamento == null) {
            throw new DbException("É obrigatório selecionar um equipamento.");
        }

        if (equipamento.getStatus() != StatusEquipamento.EM_USO) {
            throw new DbException("Não é possível abrir chamado para este equipamento. " +
                    "Status atual: " + equipamento.getStatus()
                    + ". Apenas equipamentos 'EM USO' podem receber chamados.");
        }

        chamado.setStatus(StatusChamado.ABERTO); 
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setDataFechamento(null); // Garantir que não nasça fechado
        // Lógica para gerar um protocolo utilizando o seguinte formato: ANO-MES-DIA-HORA-MINUTO-SEGUNDO + número aleatório
        if (chamado.getProtocolo() == null || chamado.getProtocolo().isEmpty()) {
            LocalDateTime agora = LocalDateTime.now();
            int randomSuffix = 100 + (int) (Math.random() * 900);
            String protocoloGerado = String.format("%tY%<tm%<td-%<tH%<tM%<tS-%d", agora, randomSuffix);
            chamado.setProtocolo(protocoloGerado);
        }
        
        chamadoDAO.criar(chamado);
    }

    public List<Chamado> listarTodos() {
        return chamadoDAO.listarChamados();
    }

    public void atualizarStatus(Chamado chamado, StatusChamado novoStatus) {
        if (chamado == null || novoStatus == null) {
            throw new DbException("Chamado e novo status são obrigatórios.");
        }

        StatusChamado statusAtual = chamado.getStatus();

        if (statusAtual == StatusChamado.CONCLUIDO || statusAtual == StatusChamado.CANCELADO) {
            throw new DbException("O chamado está " + statusAtual + " e não pode ser alterado.");
        }

        chamado.setStatus(novoStatus);

        if (novoStatus == StatusChamado.CONCLUIDO || novoStatus == StatusChamado.CANCELADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        } else {
            chamado.setDataFechamento(null);
        }

        chamadoDAO.atualizar(chamado);
    }

    public void designarTecnico(Chamado chamado, Usuario tecnico) {
        if (chamado == null || tecnico == null) {
            throw new DbException("Chamado e Técnico são obrigatórios.");
        }

        if (tecnico.getPerfil() != PerfilUsuario.TECNICO && tecnico.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new DbException("O usuário selecionado não possui perfil de Técnico ou Administrador.");
        }

        chamado.setTecnicoResponsavel(tecnico);

        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }

        chamadoDAO.atualizar(chamado);
    }

    public List<Chamado> buscarPorSolicitante(Usuario solicitante) {
        if (solicitante == null)
            throw new DbException("Solicitante é obrigatório para busca de chamados.");
        return chamadoDAO.listarPorSolicitante(solicitante);
    }
}