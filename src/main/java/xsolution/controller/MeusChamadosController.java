package xsolution.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import xsolution.model.entity.Chamado;
import xsolution.service.ChamadoService;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;
import xsolution.utils.ViewUtils;

public class MeusChamadosController implements Initializable {

    @FXML
    private TableView<Chamado> tabelaMeusChamados;
    @FXML
    private TableColumn<Chamado, String> colProtocolo;
    @FXML
    private TableColumn<Chamado, String> colTitulo;
    @FXML
    private TableColumn<Chamado, String> colStatus;
    @FXML
    private TableColumn<Chamado, String> colDataAbertura;
    @FXML
    private TableColumn<Chamado, String> colTecnico;
    @FXML
    private Button btnNovoChamado;
    @FXML
    private ProgressIndicator loadingIndicator;

    private ChamadoService chamadoService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chamadoService = new ChamadoService();
        configurarTabela();
        carregarMeusChamados();
    }

    private void configurarTabela() {
        colProtocolo.setCellValueFactory(new PropertyValueFactory<>("protocolo"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDataAbertura.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataAbertura() != null) {
                return new SimpleStringProperty(cellData.getValue().getDataAbertura().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        colTecnico.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTecnicoResponsavel() != null) {
                return new SimpleStringProperty(cellData.getValue().getTecnicoResponsavel().getNome());
            }
            return new SimpleStringProperty("-");
        });
    }

    private void carregarMeusChamados() {
        loadingIndicator.setVisible(true);

        Task<List<Chamado>> task = new Task<>() {
            @Override
            protected List<Chamado> call() throws Exception {
                return chamadoService.buscarPorSolicitante(Sessao.getUsuarioLogado());
            }
        };

        task.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            tabelaMeusChamados.setItems(FXCollections.observableArrayList(task.getValue()));
        });

        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            AlertUtils.showError("Erro", "Falha ao carregar seus chamados: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleNovoChamado(ActionEvent event) {
        String fxmlPath = "/xsolution/view/ModalNovoChamado.fxml";
        boolean sucesso = ViewUtils.abrirModal(fxmlPath, "Abrir Novo Chamado", event);
        if (sucesso) {
            carregarMeusChamados();
        }
    }
}