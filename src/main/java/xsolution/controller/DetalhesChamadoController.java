package xsolution.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import xsolution.dao.UsuarioDAO; // Import do DAO criado
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.StatusChamado;
import xsolution.service.ChamadoService;
import xsolution.util.AlertUtils;

public class DetalhesChamadoController {

    @FXML private TextField txtProtocolo;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<StatusChamado> cbStatus;
    @FXML private ComboBox<Usuario> cbTecnico;

    private Chamado chamadoAtual;
    private ChamadoService chamadoService;
    private UsuarioDAO usuarioDAO; // Declaração do DAO
    private boolean salvou = false;

    @FXML
    public void initialize() {
        this.chamadoService = new ChamadoService();
        this.usuarioDAO = new UsuarioDAO(); // Inicialização do DAO

        // Popula os status
        cbStatus.setItems(FXCollections.observableArrayList(StatusChamado.values()));
        
        // Popula os técnicos usando o DAO
        try {
            cbTecnico.setItems(FXCollections.observableArrayList(usuarioDAO.findAllTecnicos()));
        } catch (Exception e) {
            AlertUtils.showError("Erro", "Não foi possível carregar a lista de técnicos.");
            e.printStackTrace();
        }
    }

    public void setChamado(Chamado chamado) {
        this.chamadoAtual = chamado;
        
        txtProtocolo.setText(chamado.getProtocolo());
        txtTitulo.setText(chamado.getTitulo());
        txtDescricao.setText(chamado.getDescricao());
        cbStatus.setValue(chamado.getStatus());
        
        // Seleciona o técnico atual no combo (se houver)
        cbTecnico.setValue(chamado.getTecnicoResponsavel());
    }

    @FXML
    public void handleSalvar() {
        try {
            // 1. Verifica mudança de Status
            if (cbStatus.getValue() != chamadoAtual.getStatus()) {
                chamadoService.atualizarStatus(chamadoAtual, cbStatus.getValue());
            }

            // 2. Verifica mudança de Técnico
            if (!cbTecnico.isDisabled() && cbTecnico.getValue() != null) {
                // Se não tinha técnico ou se mudou o técnico
                if (chamadoAtual.getTecnicoResponsavel() == null || 
                   !cbTecnico.getValue().equals(chamadoAtual.getTecnicoResponsavel())) {
                    
                    chamadoService.designarTecnico(chamadoAtual, cbTecnico.getValue());
                }
            }

            AlertUtils.showInfo("Sucesso", "Chamado atualizado com sucesso.");
            this.salvou = true;
            closeWindow();

        } catch (Exception e) {
            AlertUtils.showError("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    public void handleCancelar() {
        closeWindow();
    }

    private void closeWindow() {
        // Obtém a janela atual e fecha
        Stage stage = (Stage) txtProtocolo.getScene().getWindow();
        stage.close();
    }
    
    public boolean isSalvou() {
        return salvou;
    }
}