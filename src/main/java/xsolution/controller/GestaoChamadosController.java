package xsolution.controller;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import xsolution.model.entity.Chamado;
import xsolution.model.enums.StatusChamado;
import xsolution.service.ChamadoService;
import xsolution.utils.AlertUtils;
import xsolution.utils.ViewUtils;

public class GestaoChamadosController implements Initializable {

    @FXML private TextField filterProtocolo;
    @FXML private ComboBox<StatusChamado> filterStatus;
    @FXML private DatePicker filterDataInicio;
    @FXML private TableView<Chamado> chamadosTable;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimparFiltros;
    @FXML private Button btnRelatorio;
    
    @FXML private TableColumn<Chamado, String> colProtocolo;
    @FXML private TableColumn<Chamado, String> colTitulo;
    @FXML private TableColumn<Chamado, String> colStatus;
    @FXML private TableColumn<Chamado, String> colSolicitante;
    @FXML private TableColumn<Chamado, String> colTecnico;
    @FXML private TableColumn<Chamado, String> colDataAbertura;

    private ChamadoService chamadoService;
    private ObservableList<Chamado> listaChamados; 
    private List<Chamado> todosOsChamadosCache; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chamadoService = new ChamadoService();
        this.listaChamados = FXCollections.observableArrayList();
        this.todosOsChamadosCache = new ArrayList<>();

        configurarTabela();
        configurarFiltros();
        carregarDados();

        chamadosTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && chamadosTable.getSelectionModel().getSelectedItem() != null) {
                handleAbrirDetalhes(chamadosTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void carregarDados() {
        loadingIndicator.setVisible(true);
        chamadosTable.setOpacity(0.5);
        
        Task<List<Chamado>> task = new Task<>() {
            @Override
            protected List<Chamado> call() throws Exception {
                return chamadoService.listarTodos();
            }
        };

        task.setOnSucceeded(event -> {
            List<Chamado> resultado = task.getValue();
            todosOsChamadosCache.clear();
            todosOsChamadosCache.addAll(resultado);
            listaChamados.setAll(resultado);
            loadingIndicator.setVisible(false);
            chamadosTable.setOpacity(1.0);
        });

        task.setOnFailed(event -> {
            loadingIndicator.setVisible(false);
            chamadosTable.setOpacity(1.0);
            Throwable erro = task.getException();
            AlertUtils.showError("Erro", "Falha ao carregar chamados: " + erro.getMessage());
            erro.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void configurarTabela() {
        colProtocolo.setCellValueFactory(new PropertyValueFactory<>("protocolo"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colSolicitante.setCellValueFactory(cellData -> {
            Chamado c = cellData.getValue();
            return new SimpleStringProperty(c.getSolicitante() != null ? c.getSolicitante().getNome() : "N/A");
        });

        colTecnico.setCellValueFactory(cellData -> {
            Chamado c = cellData.getValue();
            return new SimpleStringProperty(c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : "Pendente");
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDataAbertura.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDataAbertura() != null) {
                return new SimpleStringProperty(cellData.getValue().getDataAbertura().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        chamadosTable.setItems(listaChamados);
    }

    private void configurarFiltros() {
        filterStatus.setItems(FXCollections.observableArrayList(StatusChamado.values()));
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        try {
            if (todosOsChamadosCache.isEmpty()) {
                carregarDados();
                return;
            }
            List<Chamado> filtrados = todosOsChamadosCache.stream()
                    .filter(c -> {
                        boolean matchProtocolo = true;
                        if (filterProtocolo.getText() != null && !filterProtocolo.getText().trim().isEmpty()) {
                            String filtro = filterProtocolo.getText().toLowerCase().trim();
                            String proto = c.getProtocolo() != null ? c.getProtocolo().toLowerCase() : "";
                            matchProtocolo = proto.contains(filtro);
                        }
                        boolean matchStatus = filterStatus.getValue() == null || c.getStatus() == filterStatus.getValue();
                        boolean matchData = true;
                        if (filterDataInicio.getValue() != null && c.getDataAbertura() != null) {
                            matchData = c.getDataAbertura().toLocalDate().equals(filterDataInicio.getValue());
                        }
                        return matchProtocolo && matchStatus && matchData;
                    })
                    .collect(Collectors.toList());

            listaChamados.setAll(filtrados);
        } catch (Exception e) {
            AlertUtils.showError("Erro ao filtrar", e.getMessage());
        }
    }

    @FXML
    public void handleLimparFiltros(ActionEvent event) {
        filterProtocolo.clear();
        filterStatus.setValue(null);
        filterDataInicio.setValue(null);
        listaChamados.setAll(todosOsChamadosCache);
    }

    private void handleAbrirDetalhes(Chamado chamadoSelecionado) {

        DetalhesChamadoController controller = ViewUtils.abrirModal(
            "/xsolution/view/ModalChamados.fxml",
            "Detalhes - " + chamadoSelecionado.getProtocolo(),
            (detalhesCtrl) -> {
                detalhesCtrl.setChamado(chamadoSelecionado);
            },
            null 
        );

        if (controller != null && controller.isSalvou()) {
            carregarDados();
        }
    }

    @FXML
    public void handleGerarRelatorio(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
        fileChooser.setInitialFileName("relatorio_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(chamadosTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) { 
                StringBuilder sb = new StringBuilder();
                sb.append('\ufeff'); 
                sb.append("Protocolo;Titulo;Status;Data Abertura;Solicitante;Tecnico\n");

                for (Chamado c : chamadosTable.getItems()) {
                    sb.append(c.getProtocolo()).append(";")
                      .append(c.getTitulo()).append(";")
                      .append(c.getStatus()).append(";")
                      .append(c.getDataAbertura()).append(";")
                      .append(c.getSolicitante() != null ? c.getSolicitante().getNome() : "N/A").append(";")
                      .append(c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : "Pendente")
                      .append("\n");
                }
                writer.write(sb.toString());
                AlertUtils.showInfo("Sucesso", "Relatório salvo em: " + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtils.showError("Erro", "Falha ao gerar relatório: " + e.getMessage());
            }
        }
    }
}