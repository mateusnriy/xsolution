package xsolution.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import xsolution.exception.DbException;
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Equipamento;
import xsolution.service.ChamadoService;
import xsolution.service.EquipamentoService;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;

public class AbrirChamadoController implements Initializable {

    @FXML private ComboBox<Equipamento> cbEquipamento;
    @FXML private Label lblInfoEquipamento;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;

    private ChamadoService chamadoService;
    private EquipamentoService equipamentoService;
    
    private ObservableList<Equipamento> listaOriginal;
    private ObservableList<Equipamento> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.chamadoService = new ChamadoService();
        this.equipamentoService = new EquipamentoService();

        PesquisarEquipamentos();
        carregarEquipamentos();
    }

    private void carregarEquipamentos() {
        try {
            List<Equipamento> equipamentos = equipamentoService.listarParaNovoChamado();
            
            listaOriginal = FXCollections.observableArrayList(equipamentos);
            listaFiltrada = FXCollections.observableArrayList(equipamentos);
            
            cbEquipamento.setItems(listaFiltrada);
            
        } catch (Exception e) {
            AlertUtils.showError("Erro", "Falha ao carregar lista de equipamentos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void PesquisarEquipamentos() {

        cbEquipamento.setConverter(new StringConverter<Equipamento>() {
            @Override
            public String toString(Equipamento eq) {
                if (eq == null) return null;
                return eq.getNumPatrimonio() + " - " + eq.getTipo() + " " + eq.getMarca() + " " + eq.getModelo();
            }

            @Override
            public Equipamento fromString(String string) {
                return cbEquipamento.getItems().stream()
                        .filter(e -> this.toString(e).equals(string))
                        .findFirst().orElse(null);
            }
        });

        cbEquipamento.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {

            if (cbEquipamento.getSelectionModel().getSelectedItem() != null) {
                String display = cbEquipamento.getConverter().toString(cbEquipamento.getSelectionModel().getSelectedItem());
                if (display != null && display.equals(newValue)) {
                    return;
                }
            }
            
            if (newValue == null || newValue.isEmpty()) {
                listaFiltrada.setAll(listaOriginal);
            } else {
                String upperVal = newValue.toUpperCase();
                List<Equipamento> filtro = listaOriginal.stream()
                    .filter(e -> e.getNumPatrimonio().toUpperCase().contains(upperVal) 
                              || (e.getModelo() != null && e.getModelo().toUpperCase().contains(upperVal))
                              || (e.getMarca() != null && e.getMarca().toUpperCase().contains(upperVal)))
                    .collect(Collectors.toList());
                listaFiltrada.setAll(filtro);
            }
            
            if (!cbEquipamento.isShowing() && !listaFiltrada.isEmpty()) {
                cbEquipamento.show();
            }
        });
        
        cbEquipamento.setOnAction(e -> {
            Equipamento selecionado = cbEquipamento.getValue();
            if (selecionado != null) {
                String setor = (selecionado.getSetor() != null) ? selecionado.getSetor().getNome() : "Sem Setor";
                lblInfoEquipamento.setText("Setor: " + setor);
            } else {
                lblInfoEquipamento.setText("");
            }
        });
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        Equipamento equipamentoSelecionado = cbEquipamento.getValue();

        if (equipamentoSelecionado == null) {

             String textoDigitado = cbEquipamento.getEditor().getText();
             if (!textoDigitado.isEmpty()) {
                 AlertUtils.showError("Equipamento Inválido", "O equipamento '" + textoDigitado + "' não foi encontrado ou não foi selecionado na lista.");
             } else {
                 AlertUtils.showError("Campo Obrigatório", "Por favor, selecione um equipamento.");
             }
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