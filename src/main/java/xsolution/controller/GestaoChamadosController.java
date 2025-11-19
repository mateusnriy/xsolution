package xsolution.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage; // Importação corrigida
import xsolution.model.entity.Chamado;
import xsolution.model.enums.StatusChamado;
import xsolution.service.ChamadoService;
import xsolution.util.AlertUtils;

public class GestaoChamadosController implements Initializable {

    // --- Componentes da Tela ---
    @FXML private TextField filterProtocolo;
    @FXML private ComboBox<StatusChamado> filterStatus;
    @FXML private DatePicker filterDataInicio;
    @FXML private TableView<Chamado> chamadosTable;
    @FXML private Button btnRelatorio;
    
    // --- Colunas da Tabela ---
    @FXML private TableColumn<Chamado, String> colProtocolo;
    @FXML private TableColumn<Chamado, String> colTitulo;
    @FXML private TableColumn<Chamado, String> colStatus;
    @FXML private TableColumn<Chamado, String> colSolicitante;
    @FXML private TableColumn<Chamado, String> colTecnico;
    @FXML private TableColumn<Chamado, String> colDataAbertura;

    // --- Dependências ---
    private ChamadoService chamadoService;
    private ObservableList<Chamado> listaChamados; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chamadoService = new ChamadoService();
        this.listaChamados = FXCollections.observableArrayList();

        configurarTabela();
        configurarFiltros();
        carregarDados();

        // Evento de duplo clique
        chamadosTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && chamadosTable.getSelectionModel().getSelectedItem() != null) {
                abrirChamado(chamadosTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void abrirChamado(Chamado chamadoSelecionado) {
        try {
            // CORREÇÃO: Caminho aponta para ModalChamados.fxml (plural, conforme seu arquivo)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/ModalChamados.fxml"));
            Parent root = loader.load();

            // CORREÇÃO: Usa a classe correta DetalhesChamadoController
            DetalhesChamadoController controller = loader.getController();
            controller.setChamado(chamadoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Chamado - " + chamadoSelecionado.getProtocolo());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.showAndWait(); 

            if (controller.isSalvou()) {
                carregarDados(); // Recarrega a tabela se houve salvamento
            }

        } catch (IOException e) {
            AlertUtils.showError("Erro", "Falha ao abrir detalhes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void handleGerarRelatorio() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
        fileChooser.setInitialFileName("relatorio_chamados.csv");
        
        File file = fileChooser.showSaveDialog(chamadosTable.getScene().getWindow());
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Protocolo;Titulo;Status;Data Abertura;Solicitante;Tecnico\n");

                for (Chamado c : chamadosTable.getItems()) {
                    sb.append(c.getProtocolo()).append(";");
                    sb.append(c.getTitulo()).append(";");
                    sb.append(c.getStatus()).append(";");
                    sb.append(c.getDataAbertura()).append(";");
                    sb.append(c.getSolicitante() != null ? c.getSolicitante().getNome() : "").append(";");
                    sb.append(c.getTecnicoResponsavel() != null ? c.getTecnicoResponsavel().getNome() : "").append("\n");
                }
                
                writer.write(sb.toString());
                AlertUtils.showInfo("Sucesso", "Relatório gerado com sucesso!");
                
            } catch (Exception e) {
                AlertUtils.showError("Erro", "Falha ao gerar relatório: " + e.getMessage());
            }
        }
    }

    private void configurarTabela() {
        colProtocolo.setCellValueFactory(new PropertyValueFactory<>("protocolo"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colSolicitante.setCellValueFactory(cellData -> {
            Chamado c = cellData.getValue();
            if (c.getSolicitante() != null) {
                return new SimpleStringProperty(c.getSolicitante().getNome());
            }
            return new SimpleStringProperty("N/A");
        });

        colTecnico.setCellValueFactory(cellData -> {
            Chamado c = cellData.getValue();
            if (c.getTecnicoResponsavel() != null) {
                return new SimpleStringProperty(c.getTecnicoResponsavel().getNome());
            }
            return new SimpleStringProperty("Pendente");
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

    private void carregarDados() {
        try {
            List<Chamado> dadosBanco = chamadoService.listarTodos();
            listaChamados.clear();
            listaChamados.addAll(dadosBanco);
        } catch (Exception e) {
            AlertUtils.showError("Erro ao carregar dados", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFilter() {
        try {
            List<Chamado> todos = chamadoService.listarTodos();
            List<Chamado> filtrados = todos.stream()
                .filter(c -> {
                    if (filterProtocolo.getText() != null && !filterProtocolo.getText().isEmpty()) {
                        if (!c.getProtocolo().toLowerCase().contains(filterProtocolo.getText().toLowerCase())) 
                            return false;
                    }
                    if (filterStatus.getValue() != null) {
                        if (c.getStatus() != filterStatus.getValue()) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

            listaChamados.clear();
            listaChamados.addAll(filtrados);
        } catch (Exception e) {
            AlertUtils.showError("Erro ao filtrar", e.getMessage());
        }
    }
    
    @FXML
    public void handleLimparFiltros() {
        filterProtocolo.clear();
        filterStatus.getSelectionModel().clearSelection();
        filterDataInicio.setValue(null);
        carregarDados();
    }
}