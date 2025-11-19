package xsolution.service;

import org.mindrot.jbcrypt.BCrypt;
import xsolution.dao.UsuarioDAO;
import xsolution.dao.UsuarioDAOImpl;
import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Servidor;
import xsolution.model.entity.Usuario;
import xsolution.model.enums.StatusUsuario;

public class UsuarioService {

  private UsuarioDAO usuarioDAO;

  public UsuarioService() {
    this.usuarioDAO = new UsuarioDAOImpl(DB.getConnection());
  }

  public void criarUsuario(String nome, String email, String senhaPura) throws DbException {
    if (senhaPura == null || senhaPura.length() < 8) {
      throw new DbException("A senha deve ter no mínimo 8 caracteres.");
    }

    String senhaHash = BCrypt.hashpw(senhaPura, BCrypt.gensalt());

    // DAO gera o próximo ID (checar depois a questão do prefixo)
    String novoId = usuarioDAO.gerarProximoIdServidor();

    Servidor novoServidor = new Servidor();
    novoServidor.setId(novoId);
    novoServidor.setNome(nome);
    novoServidor.setEmail(email);
    novoServidor.setSenhaHash(senhaHash);
    novoServidor.setStatus(StatusUsuario.ATIVO);

    try {
      usuarioDAO.inserir(novoServidor);
    } catch (Exception e) {
      if (e.getMessage().contains("usuario_email_key")) {
        throw new DbException("O e-mail informado já está em uso.");
      }
      throw new DbException("Erro inesperado ao criar usuário: " + e.getMessage());
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
}