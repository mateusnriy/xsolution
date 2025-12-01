package xsolution.service;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import xsolution.dao.SetorDAO;
import xsolution.dao.SetorDAOImpl;
import xsolution.dao.UsuarioDAO;
import xsolution.dao.UsuarioDAOImpl;
import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Servidor;
import xsolution.model.entity.Setor;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.StatusUsuario;
import xsolution.utils.Sessao;

public class UsuarioService {

    private UsuarioDAO usuarioDAO;
    private SetorDAO setorDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl(DB.getConnection());
        this.setorDAO = new SetorDAOImpl();
    }

    public void criarUsuario(String nome, String email, String senhaPura) throws DbException {
        if (senhaPura == null || senhaPura.length() < 8) {
            throw new DbException("A senha deve ter no mínimo 8 caracteres.");
        }

        Setor setorPadrao = setorDAO.findById(999);
        if (setorPadrao == null) {
            setorPadrao = new Setor();
            setorPadrao.setId(999);
            setorPadrao.setNome("Setor Padrão (Sistema)");
            setorPadrao.setSigla("PADRAO");
        }

        String senhaHash = BCrypt.hashpw(senhaPura, BCrypt.gensalt());
        String novoId = usuarioDAO.gerarProximoIdServidor();

        Servidor novoServidor = new Servidor();
        novoServidor.setId(novoId);
        novoServidor.setNome(nome);
        novoServidor.setEmail(email);
        novoServidor.setSenhaHash(senhaHash);
        novoServidor.setStatus(StatusUsuario.ATIVO);
        novoServidor.setSetor(setorPadrao);

        try {
            usuarioDAO.inserir(novoServidor);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("usuario_email_key")) {
                throw new DbException("O e-mail informado já está em uso.");
            }
            throw new DbException("Erro ao criar usuário: " + e.getMessage());
        }
    }

    public Usuario autenticar(String email, String senhaPura) throws DbException {
        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario == null) {
            return null;
        }

        if (BCrypt.checkpw(senhaPura, usuario.getSenhaHash())) {
            if (usuario.getStatus() != StatusUsuario.ATIVO) {
                throw new DbException("Seu acesso está bloqueado. Contate o Administrador.");
            }
            return usuario;
        }

        return null;
    }

    public void atualizarPerfil(Usuario usuario, String senhaAtual, String novaSenha, String confirmacaoSenha) {
        atualizarUsuario(usuario);

        if (novaSenha != null && !novaSenha.isBlank()) {

            if (senhaAtual == null || senhaAtual.isBlank()) {
                throw new DbException("Para alterar a senha, é necessário informar a senha atual.");
            }

            Usuario usuarioBanco = usuarioDAO.buscarPorEmail(usuario.getEmail());
            if (!BCrypt.checkpw(senhaAtual, usuarioBanco.getSenhaHash())) {
                throw new DbException("A senha atual está incorreta.");
            }

            if (novaSenha.length() < 8) {
                throw new DbException("A nova senha deve ter no mínimo 8 caracteres.");
            }

            if (!novaSenha.equals(confirmacaoSenha)) {
                throw new DbException("A nova senha e a confirmação não coincidem.");
            }

            String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
            usuarioDAO.atualizarSenha(usuario.getId(), novoHash);

            // usuario.setSenhaHash(novoHash);
        }
    }

    public List<Usuario> listarTecnico() {
        List<Usuario> tecnicos = usuarioDAO.listarTecnicos();
        tecnicos.forEach(tec -> {
            System.out.println("ID: " + tec.getId() + ", Nome: " + tec.getNome() + ", Email: " + tec.getEmail());
        });
        return tecnicos;
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public void atualizarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new DbException("Usuário inválido.");
        }
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new DbException("O nome é obrigatório.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new DbException("O e-mail é obrigatório.");
        }

        // Aqui a gente garante o usuário logado não desativar a si mesmo (user é burro)
        if (usuario.getId().equals(Sessao.getUsuarioLogado().getId()) && usuario.getStatus() == StatusUsuario.INATIVO) {
            throw new DbException("Você não pode desativar seu próprio usuário.");
        }

        usuarioDAO.atualizar(usuario);
    }
}