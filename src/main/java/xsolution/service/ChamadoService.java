package xsolution.service;

import java.time.LocalDateTime;
import java.util.List;

import xsolution.dao.ChamadoDAO;
import xsolution.dao.ChamadoDAOImpl;
import xsolution.exception.NegocioException;
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
            throw new NegocioException("O título do chamado é obrigatório.");
        }
        if (chamado.getDescricao() == null || chamado.getDescricao().trim().isEmpty()) {
            throw new NegocioException("A descrição do chamado é obrigatória.");
        }
        if (chamado.getSolicitante() == null) {
            throw new NegocioException("O chamado deve ter um solicitante vinculado.");
        }

        Equipamento equipamento = chamado.getEquipamento();
        if (equipamento == null) {
            throw new NegocioException("É obrigatório selecionar um equipamento.");
        }

        if (equipamento.getStatus() != StatusEquipamento.EM_USO) {
            throw new NegocioException("Não é possível abrir chamado para este equipamento. " +
                    "Status atual: " + equipamento.getStatus()
                    + ". Apenas equipamentos 'EM_USO' podem receber chamados.");
        }

        chamado.setStatus(StatusChamado.ABERTO); // Status inicial
        chamado.setDataAbertura(LocalDateTime.now()); // Data atual
        chamado.setDataFechamento(null); // Garantir que não nasça fechado

        if (chamado.getProtocolo() == null || chamado.getProtocolo().isEmpty()) {
            String protocoloGerado = String.format("%tY%<tm%<td-%<tH%<tM%<tS", LocalDateTime.now());
            chamado.setProtocolo(protocoloGerado);
        }

        chamadoDAO.create(chamado);
    }

    public List<Chamado> listarTodos() {
        return chamadoDAO.findAll();
    }

    public void atualizarStatus(Chamado chamado, StatusChamado novoStatus) {
        if (chamado == null || novoStatus == null) {
            throw new NegocioException("Chamado e novo status são obrigatórios.");
        }

        StatusChamado statusAtual = chamado.getStatus();

        if (statusAtual == StatusChamado.CONCLUIDO || statusAtual == StatusChamado.CANCELADO) {
            throw new NegocioException("O chamado está " + statusAtual + " e não pode ser alterado.");
        }

        chamado.setStatus(novoStatus);

        if (novoStatus == StatusChamado.CONCLUIDO || novoStatus == StatusChamado.CANCELADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        } else {
            chamado.setDataFechamento(null);
        }

        chamadoDAO.update(chamado);
    }

    public void designarTecnico(Chamado chamado, Usuario tecnico) {
        if (chamado == null || tecnico == null) {
            throw new NegocioException("Chamado e Técnico são obrigatórios.");
        }

        if (tecnico.getPerfil() != PerfilUsuario.TECNICO && tecnico.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new NegocioException("O usuário selecionado não possui perfil de Técnico ou Administrador.");
        }

        chamado.setTecnicoResponsavel(tecnico);

        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }

        chamadoDAO.update(chamado);
    }

    public Chamado buscarPorId(Integer id) {
        if (id == null) 
            return null;
        return chamadoDAO.findById(id);
    }
}