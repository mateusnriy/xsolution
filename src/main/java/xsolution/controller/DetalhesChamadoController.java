package xsolution.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import xsolution.model.entity.Chamado;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.PerfilUsuario;
import xsolution.model.enums.StatusChamado;
import xsolution.service.ChamadoService;
import xsolution.service.UsuarioService;
import xsolution.utils.AlertUtils;
import xsolution.utils.Sessao;

public class DetalhesChamadoController {

    @FXML
    private TextField txtProtocolo;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextArea txtDescricao;
    @FXML
    private ComboBox<StatusChamado> cbStatus;
    @FXML
    private ComboBox<Usuario> cbTecnico;
    @FXML
    private Button btnAssumir;

    private Chamado chamadoAtual;
    private ChamadoService chamadoService;
    private UsuarioService usuarioService;
    private boolean salvou = false;

    @FXML
    public void initialize() {
        this.chamadoService = new ChamadoService();
        this.usuarioService = new UsuarioService();

        cbStatus.setItems(FXCollections.observableArrayList(StatusChamado.values()));

        try {
            cbTecnico.setItems(FXCollections.observableArrayList(usuarioService.listarTecnico()));
        } catch (Exception e) {
            AlertUtils.showError("Erro", "Não foi possível carregar a lista de técnicos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setChamado(Chamado chamado) {
        this.chamadoAtual = chamado;

        txtProtocolo.setText(chamado.getProtocolo());
        txtTitulo.setText(chamado.getTitulo());
        txtDescricao.setText(chamado.getDescricao());
        cbStatus.setValue(chamado.getStatus());
        cbTecnico.setValue(chamado.getTecnicoResponsavel());

        configurarPermissoes();
    }

    private void configurarPermissoes() {
        Usuario usuarioLogado = Sessao.getUsuarioLogado();
        boolean isAdmin = usuarioLogado.getPerfil() == PerfilUsuario.ADMINISTRADOR;
        boolean isTecnico = usuarioLogado.getPerfil() == PerfilUsuario.TECNICO;

        cbTecnico.setDisable(!isAdmin);

        if (isTecnico) {
            Usuario responsavelAtual = chamadoAtual.getTecnicoResponsavel();

            boolean souEu = responsavelAtual != null && responsavelAtual.getId().equals(usuarioLogado.getId());

            if (!souEu) {
                btnAssumir.setVisible(true);
                btnAssumir.setManaged(true);
            } else {
                btnAssumir.setVisible(false);
                btnAssumir.setManaged(false);
            }
        }
    }

    @FXML
    public void handleAssumirChamado(ActionEvent event) {
        cbTecnico.setValue(Sessao.getUsuarioLogado());

        btnAssumir.setVisible(false);
        btnAssumir.setManaged(false);

        AlertUtils.showInfo("Atenção",
                "Você se definiu como responsável.\nClique em 'Salvar Alterações' para confirmar.");
    }

    @FXML
    public void handleSalvar() {
        try {
            boolean mudouStatus = cbStatus.getValue() != chamadoAtual.getStatus();
            if (mudouStatus) {
                chamadoService.atualizarStatus(chamadoAtual, cbStatus.getValue());
            }

            Usuario novoTecnico = cbTecnico.getValue();
            Usuario tecnicoAntigo = chamadoAtual.getTecnicoResponsavel();

            boolean mudouTecnico = false;
            if (tecnicoAntigo == null && novoTecnico != null)
                mudouTecnico = true;
            else if (tecnicoAntigo != null && novoTecnico != null && !tecnicoAntigo.getId().equals(novoTecnico.getId()))
                mudouTecnico = true;

            if (mudouTecnico) {
                Usuario logado = Sessao.getUsuarioLogado();
                boolean isAdmin = logado.getPerfil() == PerfilUsuario.ADMINISTRADOR;
                boolean isSelfAssign = logado.getId().equals(novoTecnico.getId());

                if (!isAdmin && !isSelfAssign) {
                    throw new RuntimeException("Você não tem permissão para atribuir este chamado a outra pessoa.");
                }

                chamadoService.designarTecnico(chamadoAtual, novoTecnico);
            }

            AlertUtils.showInfo("Sucesso", "Chamado atualizado com sucesso.");
            this.salvou = true;
            closeWindow();

        } catch (Exception e) {
            AlertUtils.showError("Erro ao salvar", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancelar() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtProtocolo.getScene().getWindow();
        stage.close();
    }

    public boolean isSalvou() {
        return salvou;
    }
}