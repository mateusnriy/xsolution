package xsolution.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import xsolution.db.DB;
import xsolution.exception.DbException;
import xsolution.model.entity.Setor;

public class SetorDAOImpl implements SetorDAO {

    @Override
    public List<Setor> findAll() {
        List<Setor> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement("SELECT * FROM Setor ORDER BY nome");
            rs = st.executeQuery();
            while (rs.next()) {
                list.add(new Setor(rs.getInt("idSetor"), rs.getString("nome"), rs.getString("sigla")));
            }
        } catch (SQLException e) {
            throw new DbException("Erro ao listar setores: " + e.getMessage());
        } finally {
            DB.closeResults(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
        return list;
    }

    @Override
    public Setor findById(Integer id) {
        if (id == null) return null;
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DB.getConnection();
            st = conn.prepareStatement("SELECT * FROM Setor WHERE idSetor = ?");
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                return new Setor(rs.getInt("idSetor"), rs.getString("nome"), rs.getString("sigla"));
            }
            return null;
        } catch (SQLException e) {
            throw new DbException("Erro ao buscar setor: " + e.getMessage());
        } finally {
            DB.closeResults(rs);
            DB.closeStatement(st);
            DB.closeConnection(conn);
        }
    }
}