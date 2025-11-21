package xsolution.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import xsolution.service.ChamadoService;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.fxml.Initializable;


public class AbrirChamadoController implements Initializable {
    @FXML private TextField equipamentoField;
    @FXML private Button buscarEquipamentoButton;
    @FXML private TextField tituloField;
    @FXML private TextArea descricaoArea;
    @FXML private Button registrarChamadoButton;
    @FXML private Button cancelarButton;

    private ChamadoService chamadoService;

    public void initialize() {
        this.chamadoService = new ChamadoService();

    }


}
