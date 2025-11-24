package xsolution.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import xsolution.exception.DbException;
import xsolution.model.entity.Usuario;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;
import xsolution.utils.ScreenUtils;
import xsolution.utils.Sessao;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField senhaField;
    @FXML
    private Button loginButton;

    private UsuarioService usuarioService;

    public LoginController() {
        this.usuarioService = new UsuarioService();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String senha = senhaField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            AlertUtils.showError("Erro de Login", "E-mail e senha são obrigatórios.");
            return;
        }

        try {
            Usuario usuarioAutenticado = usuarioService.autenticar(email, senha);

            if (usuarioAutenticado != null) {
                Sessao.setUsuarioLogado(usuarioAutenticado);

                ScreenUtils.changeScreen(event, "/xsolution/view/MainDashboard.fxml", "X Solution - Dashboard");
            } else {
                AlertUtils.showError("Login Falhou", "Usuário ou senha incorretos.");
            }
        } catch (DbException e) {
            AlertUtils.showError("Erro no Banco", e.getMessage());
        }
    }

    @FXML
    private void handleRecover(ActionEvent event) {
        ScreenUtils.changeScreen(event, "/xsolution/view/RecuperarSenha.fxml", "Recuperar Senha");
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        ScreenUtils.changeScreen(event, "/xsolution/view/CriarConta.fxml", "Criar Conta");
    }
}