package xsolution.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import xsolution.utils.AlertUtils;
import xsolution.utils.ScreenUtils;

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
    ScreenUtils.changeScreen(event, "/xsolution/view/Login.fxml", "X Solution - Login");
  }
}
