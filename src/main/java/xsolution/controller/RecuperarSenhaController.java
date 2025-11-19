package xsolution.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import xsolution.utils.AlertUtils;

public class RecuperarSenhaController {
  @FXML
  private TextField emailField;

  @FXML
  private void handleSendLink(ActionEvent event) {
    String email = emailField.getText();

    if (email == null || email.trim().isEmpty()) {
      AlertUtils.showError("Campo Obrigatório", "Por favor, digite seu e-mail para recuperar a senha.");
      return;
    }

    // Aqui daria para chamar a Service no futuro, algo como
    // [authService.enviarEmailRecuperacao(email);]

    AlertUtils.showInfo("Link Enviado",
        "Se o e-mail " + email + " estiver cadastrado, você receberá um link de recuperação em instantes.");

    emailField.clear();
  }

  @FXML
  private void handleBack(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/xsolution/view/Login.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(scene);
      stage.setTitle("X Solution - Login");

      stage.setResizable(true);
      stage.setMaximized(true);

      stage.show();

    } catch (IOException e) {
      e.printStackTrace();
      AlertUtils.showError("Erro", "Não foi possível voltar ao login.");
    }
  }
}
