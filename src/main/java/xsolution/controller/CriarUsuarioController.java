package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import xsolution.exception.DbException;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;
import xsolution.utils.ViewUtils;

public class CriarUsuarioController implements Initializable {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField senhaField;
    @FXML
    private PasswordField confirmSenhaField;
    @FXML
    private Button createButton;
    @FXML
    private Hyperlink backToLoginLink;

    private UsuarioService usuarioService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.usuarioService = new UsuarioService();
        // Não precisamos carregar setores aqui, pois o serviço cuida disso
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String senha = senhaField.getText();
        String confirmSenha = confirmSenhaField.getText();

        try {
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmSenha.isEmpty()) {
                AlertUtils.showError("Erro no Formulário", "Todos os campos são obrigatórios.");
                return;
            }

            if (!senha.equals(confirmSenha)) {
                AlertUtils.showError("Erro no Formulário", "A nova senha e a confirmação não coincidem.");
                return;
            }

            // Chama o serviço sem passar setor (o serviço usa o 999)
            usuarioService.criarUsuario(nome, email, senha);

            AlertUtils.showInfo("Sucesso", "Conta criada com sucesso! Faça o login.");
            handleBack(event);

        } catch (DbException e) {
            AlertUtils.showError("Erro ao Criar Conta", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erro Inesperado", "Ocorreu um erro: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        ViewUtils.trocarCenaPrincipal(event, "/xsolution/view/Login.fxml", "X Solution - Login");
    }
}