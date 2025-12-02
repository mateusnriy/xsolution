package xsolution.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;
import xsolution.utils.ViewUtils;

public class MainDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button navMeusChamadosButton;
    @FXML
    private Button navGestaoChamadosButton;
    @FXML
    private Button navGestaoEquipamentosButton;
    @FXML
    private Button navGestaoUsuariosButton;
    @FXML
    private Button navLogoutButton;
    @FXML
    private Button navMeuPerfilButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Usuario usuarioLogado = Sessao.getUsuarioLogado();

        if (usuarioLogado == null) {
            System.out.println("ALERTA: Nenhum usuário na sessão.");
            return;
        }

        aplicarPermissoes(usuarioLogado);
    }

    private void aplicarPermissoes(Usuario usuario) {
        PerfilUsuario perfil = usuario.getPerfil();

        boolean isAdmin = perfil == PerfilUsuario.ADMINISTRADOR;
        boolean isTecnico = perfil == PerfilUsuario.TECNICO;

        configurarBotao(navGestaoUsuariosButton, isAdmin);

        configurarBotao(navGestaoEquipamentosButton, isAdmin || isTecnico);

        configurarBotao(navGestaoChamadosButton, isAdmin || isTecnico);
    }

    private void configurarBotao(Button button, boolean visivel) {
        if (button != null) {
            button.setVisible(visivel);
            button.setManaged(visivel);
        }
    }

    private void carregarTela(String fxmlPath) {
        if (contentArea == null) {
            AlertUtils.showError("Erro Crítico",
                    "O container 'contentArea' não foi injetado. Verifique o fx:id no MainDashboard.fxml.");
            return;
        }

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
    public void handleNavGestaoEquipamentos(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoEquipamentos.fxml");
    }

    @FXML
    public void handleNavGestaoUsuarios(ActionEvent event) {
        carregarTela("/xsolution/view/GestaoUsuarios.fxml");
    }

    @FXML
    public void handleNavMeusChamados(ActionEvent event) {
        carregarTela("/xsolution/view/MeusChamados.fxml");
    }

    @FXML
    public void handleNavMeuPerfil(ActionEvent event) {
        carregarTela("/xsolution/view/MeuPerfil.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        Sessao.logout();
        ViewUtils.trocarCenaPrincipal(event, "/xsolution/view/Login.fxml", "X Solution - Login");
    }
}