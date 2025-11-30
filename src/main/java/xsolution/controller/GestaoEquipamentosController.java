package xsolution.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xsolution.model.entity.Equipamento;
import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;
import xsolution.service.EquipamentoService;
import xsolution.utils.AlertUtils;
import xsolution.utils.FilterHelper;

public class GestaoEquipamentosController implements Initializable {

    @FXML private TextField filterBusca;
    @FXML private ComboBox<TipoEquipamento> filterTipo;
    @FXML private ComboBox<StatusEquipamento> filterStatus;

    @FXML private TableView<Equipamento> tabelaEquipamentos;
    
    private EquipamentoService service;
    
    private ObservableList<Equipamento> listaEquipamentosView;
    private List<Equipamento> todosEquipamentosCache;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        try {
            this.service = new EquipamentoService();
            
            this.listaEquipamentosView = FXCollections.observableArrayList();
            this.todosEquipamentosCache = new ArrayList<>();

            if (tabelaEquipamentos == null) {
                System.err.println("ERRO GRAVE: tabelaEquipamentos é NULL!");
                return;
            }

            filterTipo.setItems(FXCollections.observableArrayList(TipoEquipamento.values()));
            filterStatus.setItems(FXCollections.observableArrayList(StatusEquipamento.values()));

            configurarTabela();
            carregarDados();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro de Inicialização", "Falha ao iniciar: " + e.getMessage());
        }
    }

    private void configurarTabela() {
        tabelaEquipamentos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tabelaEquipamentos.getSelectionModel().getSelectedItem() != null) {
                abrirModal(tabelaEquipamentos.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void carregarDados(){
        try {
            List<Equipamento> listaBanco = service.listarTodos();
            
            todosEquipamentosCache.clear();
            todosEquipamentosCache.addAll(listaBanco);
            listaEquipamentosView.setAll(listaBanco);
            tabelaEquipamentos.setItems(listaEquipamentosView);
            
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro", "Falha ao carregar equipamentos.\n" + e.getMessage());
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

    @FXML
    public void handleFiltrar(ActionEvent event) {
        List<Equipamento> filtrados = todosEquipamentosCache.stream()
            .filter(e -> {
                String busca = filterBusca.getText();
                boolean matchTexto = true;
                if (busca != null && !busca.isEmpty()) {
                    matchTexto = FilterHelper.matchString(e.getNumPatrimonio(), busca) ||
                                 FilterHelper.matchString(e.getMarca(), busca) ||
                                 FilterHelper.matchString(e.getModelo(), busca);
                }
                
                boolean matchTipo = FilterHelper.matchEquals(e.getTipo(), filterTipo.getValue());
                
                boolean matchStatus = FilterHelper.matchEquals(e.getStatus(), filterStatus.getValue());

                return matchTexto && matchTipo && matchStatus;
            })
            .collect(Collectors.toList());

        listaEquipamentosView.setAll(filtrados);
    }

    @FXML
    public void handleLimparFiltros(ActionEvent event) {
        filterBusca.clear();
        filterTipo.setValue(null);
        filterStatus.setValue(null);
        
        listaEquipamentosView.setAll(todosEquipamentosCache);
    }
}