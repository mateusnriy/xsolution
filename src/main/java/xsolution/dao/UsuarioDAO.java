package xsolution.dao;

import java.util.List;

import xsolution.model.entity.Servidor;
import xsolution.model.entity.Usuario;

public interface UsuarioDAO {
    void inserir(Servidor servidor);

    void atualizar(Usuario usuario);

    void atualizarSenha(String idUsuario, String novaSenhaHash);

    List<Usuario> listarTodos();

    Usuario buscarPorEmail(String email);

    String gerarProximoIdServidor();

    List<Usuario> listarTecnicos();
}
