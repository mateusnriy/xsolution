package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import xsolution.utils.AlertUtils;
import xsolution.utils.ScreenUtils;
import xsolution.utils.Sessao;

public class MainDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button navAbrirChamadosButton;

    @FXML
    private Button navMeusChamadosButton;

    @FXML
    private Button navGestaoChamadosButton;

    @FXML
    private Button navGestaoEquipamentosButton;

    @FXML
    private Button navLogoutButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (Sessao.getUsuarioLogado() == null) {
            System.out.println("ALERTA: Nenhum usuário na sessão. Redirecionando para login seria o ideal.");
        }
    }

    private void carregarTela(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                AlertUtils.showError("Erro Crítico", "Arquivo FXML não encontrado: " + fxmlPath);
                return;
            }

            if (contentArea == null) {
                AlertUtils.showError("Erro Crítico",
                        "O container 'contentArea' não foi injetado. Verifique o fx:id no MainDashboard.fxml.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro de Navegação",
                    "Falha ao carregar a tela: " + fxmlPath + "\n\nErro: " + e.getMessage() + "\nCausa: "
                            + e.getCause());
        }
    }

    @FXML
    public void handleNavGestaoChamados(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoChamados.fxml");
    }

    @FXML
    public void handleNavGestaoEquipamentos(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoEquipamentos.fxml");
    }

    @FXML
    public void handleNavAbrirChamados(ActionEvent event) {
        carregarTela("/xsolution/view/AbrirChamado.fxml");
    }

    @FXML
    public void handleNavMeusChamados(ActionEvent event) {
        carregarTela("/xsolution/view/MeusChamados.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        ScreenUtils.changeScreen(event, "/xsolution/view/Login.fxml", "X Solution - Login");
    }
}