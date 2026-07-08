package dao.hibernate;

import config.TransactionManager;
import dao.EstoqueDAO;
import model.Estoque;
import org.hibernate.Session;

import java.util.List;

public class EstoqueDAOHibernate implements EstoqueDAO {

    @Override
    public void inserir(Estoque estoque) {
        TransactionManager.executar(() -> TransactionManager.getSession().persist(estoque));
    }

    @Override
    public void atualizar(Estoque estoque) {
        TransactionManager.executar(() -> TransactionManager.getSession().merge(estoque));
    }

    @Override
    public Estoque buscarPorProduto(int produtoId) {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            "select e from Estoque e join fetch e.produto where e.produto.id = :produtoId",
                            Estoque.class)
                    .setParameter("produtoId", produtoId)
                    .uniqueResult();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Estoque> listarTodos() {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            "select e from Estoque e join fetch e.produto order by e.produto.id",
                            Estoque.class)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }

    @Override
    public List<Estoque> listarAbaixoDoMinimo() {
        Session sessao = TransactionManager.getSession();
        try {
            return sessao.createQuery(
                            "select e from Estoque e join fetch e.produto "
                                    + "where e.quantidadeDisponivel < e.quantidadeMinima order by e.produto.id",
                            Estoque.class)
                    .list();
        } finally {
            TransactionManager.liberar(sessao);
        }
    }
}
