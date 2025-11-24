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

    public List<Usuario> listarTecnico() {
        List<Usuario> tecnicos = usuarioDAO.listarTecnicos();
        tecnicos.forEach(tec -> {
            System.out.println("ID: " + tec.getId() + ", Nome: " + tec.getNome() + ", Email: " + tec.getEmail());
        });
        return tecnicos;
    }
}