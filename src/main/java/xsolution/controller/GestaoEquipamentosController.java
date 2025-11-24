package xsolution.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xsolution.model.entity.Equipamento;
import xsolution.service.EquipamentoService;
import xsolution.utils.AlertUtils;

public class GestaoEquipamentosController implements Initializable {

    @FXML private TableView<Equipamento> tabelaEquipamentos;
    
    private EquipamentoService service;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.service = new EquipamentoService();
            
            if (tabelaEquipamentos == null) {
                System.err.println("ERRO GRAVE: tabelaEquipamentos é NULL! Verifique o fx:id no FXML.");
                return;
            }

            configurarTabela();
            carregarDados();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro de Inicialização", "Falha ao iniciar módulo de equipamentos: " + e.getMessage());
        }
    }

    private void configurarTabela() {
        tabelaEquipamentos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tabelaEquipamentos.getSelectionModel().getSelectedItem() != null) {
                abrirModal(tabelaEquipamentos.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void carregarDados() {
        try {
            tabelaEquipamentos.setItems(FXCollections.observableArrayList(service.listarTodos()));
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro", "Falha ao carregar equipamentos do banco.\nVerifique a conexão.\n" + e.getMessage());
        }
    }

    @FXML
    public void handleNovoEquipamento(ActionEvent event) {
        abrirModal(null);
    }
    
    private void abrirModal(Equipamento equipamento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/ModalEquipamento.fxml"));
            Parent root = loader.load();
            
            ModalEquipamentoController controller = loader.getController();
            controller.setEquipamento(equipamento); 

            Stage stage = new Stage();
            String titulo = (equipamento == null) ? "Novo Equipamento" : "Editar Equipamento - " + equipamento.getNumPatrimonio();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
            
            if (controller.isSalvou()) {
                carregarDados();
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erro", "Falha ao abrir janela de equipamento: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleRemover() {
        Equipamento selecionado = tabelaEquipamentos.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            try {
                service.remover(selecionado);
                AlertUtils.showInfo("Sucesso", "Equipamento removido.");
                carregarDados();
            } catch (Exception e) {
                 AlertUtils.showError("Erro", e.getMessage());
            }
        } else {
            AlertUtils.showError("Atenção", "Selecione um equipamento para remover.");
        }
    }
    
    @FXML
    public void handleVoltarDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/MainDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tabelaEquipamentos.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("X Solution - Dashboard");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}