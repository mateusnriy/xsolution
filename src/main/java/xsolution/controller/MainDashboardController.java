package xsolution.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import xsolution.util.AlertUtils;

public class MainDashboardController implements Initializable {

    @FXML
    private StackPane contentArea; // Área central onde as telas mudam

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Opcional: Carregar uma tela inicial ao abrir o Dashboard
        // carregarTela("/xsolution/view/GestaoChamados.fxml");
    }

    // Método auxiliar para trocar o conteúdo do centro
    private void carregarTela(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            AlertUtils.showError("Erro de Navegação", "Não foi possível carregar a tela: " + fxmlPath + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNavGestaoChamados(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoChamados.fxml");
    }

    // Adicione aqui os métodos para os outros botões (Abrir Chamado, Meus Chamados, etc.)
    // Exemplo:
    // @FXML
    // public void handleNavAbrirChamados(ActionEvent event) {
    //     carregarTela("/xsolution/view/AbrirChamado.fxml");
    // }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Logout realizado.");
        System.exit(0); // Encerra a aplicação por enquanto
    }
}