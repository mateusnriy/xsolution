package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import xsolution.utils.Sessao;
import xsolution.utils.ViewUtils;

public class MainDashboardController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button navAbrirChamadosButton;
    @FXML private Button navMeusChamadosButton;
    @FXML private Button navGestaoChamadosButton;
    @FXML private Button navLogoutButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Sessao.getUsuarioLogado() == null) {
            System.out.println("ALERTA: Nenhum usuário na sessão.");
        }
    }

    private void carregarTela(String fxmlPath) {
        Parent view = ViewUtils.carregarView(fxmlPath);
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    @FXML
    public void handleNavGestaoChamados(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoChamados.fxml");
    }

    @FXML
    public void handleNavMeusChamados(ActionEvent event) {
        carregarTela("/xsolution/view/MeusChamados.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        Sessao.logout();
        ViewUtils.carregarCena(event, "/xsolution/view/Login.fxml", "X Solution - Login");
    }
}