package dao.hibernate;

import config.TransactionManager;
import dao.ClienteDAO;
import model.Cliente;
import org.hibernate.Session;

import java.util.List;

public class ClienteDAOHibernate implements ClienteDAO {

    @Override
    public void inserir(Cliente cliente) {
        TransactionManager.executar(() -> TransactionManager.getSession().persist(cliente));
    }

    @Override
    public void atualizar(Cliente cliente) {
        TransactionManager.executar(() -> TransactionManager.getSession().merge(cliente));
    }

    @Override
    public Cliente buscarPorId(int id) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.get(Cliente.class, id);
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery("from Cliente order by nome", Cliente.class).list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Cliente> listarPorStatus(boolean ativo) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery("from Cliente where ativo = :ativo order by nome", Cliente.class)
                    .setParameter("ativo", ativo)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public boolean existeCpf(String cpf) {
        Session sessao = TransactionManager.getSession();
        try {
            Long total = sessao.createQuery(
                            "select count(c) from Cliente c where c.cpf = :cpf", Long.class)
                    .setParameter("cpf", cpf)
                    .uniqueResult();
            return total != null && total > 0;
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public void deletar(int id) {
        TransactionManager.executar(() -> {
            Session sessao = TransactionManager.getSession();
            Cliente cliente = sessao.get(Cliente.class, id);
            if (cliente != null) {
                sessao.remove(cliente);
            }
        });
    }
}
