package xsolution.dao;

import java.util.List;

import xsolution.model.entity.Servidor;
import xsolution.model.entity.Usuario;

public interface UsuarioDAO {
  void inserir(Servidor servidor);

  Usuario buscarPorEmail(String email);

  String gerarProximoIdServidor();

  List<Usuario> findAllTecnicos();
}
