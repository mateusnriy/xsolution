package xsolution.controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import xsolution.utils.AlertUtils;

public class MainDashboardController {

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

  // @FXML
  // public void initialize() {
  // System.out.println("Dashboard inicializado.");
  // }
  @FXML
  private void handleLogout(ActionEvent event) {
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