package dao.hibernate;

import config.TransactionManager;
import dao.ProdutoDAO;
import model.Produto;
import org.hibernate.Session;

import java.util.List;

public class ProdutoDAOHibernate implements ProdutoDAO {

    @Override
    public void inserir(Produto produto) {
        TransactionManager.executar(() -> TransactionManager.getSession().persist(produto));
    }

    @Override
    public void atualizar(Produto produto) {
        TransactionManager.executar(() -> TransactionManager.getSession().merge(produto));
    }

    @Override
    public Produto buscarPorId(int id) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.get(Produto.class, id);
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Produto> listarTodos() {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery("from Produto order by nome", Produto.class).list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Produto> listarPorStatus(boolean ativo) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery("from Produto where ativo = :ativo order by nome", Produto.class)
                    .setParameter("ativo", ativo)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Produto> listarPorCategoria(String categoria) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery("from Produto where categoria = :categoria order by nome", Produto.class)
                    .setParameter("categoria", categoria)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public void deletar(int id) {
        TransactionManager.executar(() -> {
            Session sessao = TransactionManager.getSession();
            Produto produto = sessao.get(Produto.class, id);
            if (produto != null) {
                sessao.remove(produto);
            }
        });
    }
}
