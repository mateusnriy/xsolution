package xsolution.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import xsolution.utils.AlertUtils;

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
  private Button navLogoutButton;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Inicialização opcional: carregar uma tela padrão
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

  @FXML
  public void handleNavAbrirChamados(ActionEvent event) {
    // carregarTela("/xsolution/view/AbrirChamado.fxml");
  }

  @FXML
  public void handleNavMeusChamados(ActionEvent event) {
    // carregarTela("/xsolution/view/MeusChamados.fxml");
  }

  @FXML
  public void handleLogout(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/Login.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(scene);
      stage.setTitle("X Solution - Login");
      stage.centerOnScreen();
      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
      AlertUtils.showError("Erro de Logout", "Não foi possível voltar ao login.");
    }
  }
}