package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import xsolution.dao.EquipamentoDAO;
import xsolution.dao.EquipamentoDAOImpl;
import xsolution.exception.DbException;
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Equipamento;
import xsolution.model.enums.StatusEquipamento;
import xsolution.service.ChamadoService;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;

public class AbrirChamadoController implements Initializable {

    @FXML private TextField txtPatrimonio;
    @FXML private Button btnBuscar;
    @FXML private Label lblInfoEquipamento;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;

    private ChamadoService chamadoService;
    private EquipamentoDAO equipamentoDAO;
    private Equipamento equipamentoSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chamadoService = new ChamadoService();
        this.equipamentoDAO = new EquipamentoDAOImpl(); 

        btnRegistrar.setDisable(true);
    }

    @FXML
    private void handleBuscarEquipamento(ActionEvent event) {
        String patrimonio = txtPatrimonio.getText().trim();

        if (patrimonio.isEmpty()) {
            AlertUtils.showError("Atenção", "Digite o número do patrimônio.");
            return;
        }

        lblInfoEquipamento.setText("Buscando...");
        lblInfoEquipamento.setTextFill(Color.BLACK);
        btnBuscar.setDisable(true);

        Task<Equipamento> task = new Task<>() {
            @Override
            protected Equipamento call() throws Exception {
                return equipamentoDAO.buscarPorPatrimonio(patrimonio);
            }
        };

        task.setOnSucceeded(e -> {
            btnBuscar.setDisable(false);
            Equipamento equip = task.getValue();

            if (equip == null) {
                lblInfoEquipamento.setText("Equipamento não encontrado.");
                lblInfoEquipamento.setTextFill(Color.RED);
                equipamentoSelecionado = null;
                btnRegistrar.setDisable(true);
            } else {
                validarEquipamento(equip);
            }
        });

        task.setOnFailed(e -> {
            btnBuscar.setDisable(false);
            lblInfoEquipamento.setText("Erro ao buscar.");
            AlertUtils.showError("Erro", "Falha na busca: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void validarEquipamento(Equipamento equip) {

        if (equip.getStatus() != StatusEquipamento.EM_USO) {
            lblInfoEquipamento.setText(equip.getTipo() + " " + equip.getModelo() + " - Status: " + equip.getStatus() + " (Indisponível)");
            lblInfoEquipamento.setTextFill(Color.RED);
            equipamentoSelecionado = null;
            btnRegistrar.setDisable(true);
        } else {
            lblInfoEquipamento.setText("Encontrado: " + equip.getTipo() + " " + equip.getModelo());
            lblInfoEquipamento.setTextFill(Color.GREEN);
            equipamentoSelecionado = equip;
            btnRegistrar.setDisable(false);
        }
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        if (equipamentoSelecionado == null) {
            AlertUtils.showError("Erro", "Busque e valide um equipamento primeiro.");
            return;
        }

        if (txtTitulo.getText().trim().isEmpty() || txtDescricao.getText().trim().isEmpty()) {
            AlertUtils.showError("Campos Obrigatórios", "Preencha o título e a descrição.");
            return;
        }

        try {
            Chamado novoChamado = new Chamado();
            novoChamado.setTitulo(txtTitulo.getText());
            novoChamado.setDescricao(txtDescricao.getText());
            novoChamado.setEquipamento(equipamentoSelecionado);
            novoChamado.setSolicitante(Sessao.getUsuarioLogado());

            chamadoService.abrirChamado(novoChamado);

            AlertUtils.showInfo("Sucesso", "Chamado registrado!\nProtocolo: " + novoChamado.getProtocolo());

            fecharModal(); 

        } catch (DbException e) {
            AlertUtils.showError("Erro de Regra", e.getMessage());
        } catch (Exception e) {
            AlertUtils.showError("Erro Crítico", "Falha ao registrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnRegistrar.getScene().getWindow();
        stage.close();
    }
}