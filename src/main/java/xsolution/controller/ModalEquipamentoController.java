package xsolution.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import xsolution.model.entity.Equipamento;
import xsolution.model.entity.Setor;
import xsolution.model.enums.StatusEquipamento;
import xsolution.model.enums.TipoEquipamento;
import xsolution.service.EquipamentoService;
import xsolution.service.SetorService;
import xsolution.utils.AlertUtils;

public class ModalEquipamentoController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField txtPatrimonio;
    @FXML private TextField txtSerie;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private ComboBox<TipoEquipamento> cbTipo;
    @FXML private ComboBox<StatusEquipamento> cbStatus;
    @FXML private ComboBox<Setor> cbSetor; 
    @FXML private DatePicker dpDataAquisicao;

    private EquipamentoService service;
    private SetorService setorService;
    private Equipamento equipamentoAtual;
    private boolean salvou = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.service = new EquipamentoService();
        this.setorService = new SetorService();
        
        cbTipo.setItems(FXCollections.observableArrayList(TipoEquipamento.values()));
        cbStatus.setItems(FXCollections.observableArrayList(StatusEquipamento.values()));
        cbStatus.setValue(StatusEquipamento.ESTOQUE);
        
        configurarComboSetor();
    }

    private void configurarComboSetor() {
        cbSetor.setConverter(new StringConverter<Setor>() {
            @Override
            public String toString(Setor setor) {
                if (setor == null) return null;
                return setor.getSigla() + " - " + setor.getNome();
            }

            @Override
            public Setor fromString(String string) {
                return null;
            }
        });

        try {
            cbSetor.setItems(FXCollections.observableArrayList(setorService.listarTodos()));
        } catch (Exception e) {
            AlertUtils.showError("Erro", "Não foi possível carregar a lista de setores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamentoAtual = equipamento;
        
        if (equipamento != null) {
            lblTitulo.setText("Editar Equipamento");
            txtPatrimonio.setText(equipamento.getNumPatrimonio());
            txtPatrimonio.setDisable(true);
            
            txtSerie.setText(equipamento.getNumSerie());
            txtMarca.setText(equipamento.getMarca());
            txtModelo.setText(equipamento.getModelo());
            
            cbTipo.setValue(equipamento.getTipo());
            cbStatus.setValue(equipamento.getStatus());
            
            if (equipamento.getSetor() != null) {
                cbSetor.setValue(equipamento.getSetor());
            }
            
            if (equipamento.getDataAquisicao() != null) {
                dpDataAquisicao.setValue(equipamento.getDataAquisicao());
            }
            
        } else {
            lblTitulo.setText("Novo Equipamento");
            txtPatrimonio.setDisable(false);
            this.equipamentoAtual = new Equipamento();
            this.equipamentoAtual.setDataCriacao(LocalDateTime.now());
            
            dpDataAquisicao.setValue(LocalDate.now());
        }
    }

    @FXML
    public void handleSalvar(ActionEvent event) {
        try {
            if (txtPatrimonio.getText().isEmpty() || cbTipo.getValue() == null || cbStatus.getValue() == null) {
                AlertUtils.showError("Campos Obrigatórios", "Preencha Patrimônio, Tipo e Status.");
                return;
            }

            equipamentoAtual.setNumPatrimonio(txtPatrimonio.getText());
            equipamentoAtual.setNumSerie(txtSerie.getText());
            equipamentoAtual.setMarca(txtMarca.getText());
            equipamentoAtual.setModelo(txtModelo.getText());
            equipamentoAtual.setTipo(cbTipo.getValue());
            equipamentoAtual.setStatus(cbStatus.getValue());
            equipamentoAtual.setSetor(cbSetor.getValue());
            equipamentoAtual.setDataAquisicao(dpDataAquisicao.getValue());

            service.salvar(equipamentoAtual);
            
            AlertUtils.showInfo("Sucesso", "Equipamento salvo com sucesso!");
            this.salvou = true;
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro ao Salvar", e.getMessage());
        }
    }

    @FXML
    public void handleCancelar(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtPatrimonio.getScene().getWindow();
        stage.close();
    }

    public boolean isSalvou() {
        return salvou;
    }
}