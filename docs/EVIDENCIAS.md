# Evidências de Execução — Versão Hibernate

Execução real da classe `Main` contra o MySQL (container Docker), seguindo o
[Roteiro de Demonstração](ROTEIRO_DEMONSTRACAO.md). Saída completa (incluindo o SQL
gerado pelo Hibernate) em [`evidencia-execucao.txt`](evidencia-execucao.txt).

## Resumo dos fluxos verificados

| Fluxo | Resultado |
|-------|-----------|
| Cadastro de cliente | `Cliente cadastrado com ID 4.` |
| Consulta de clientes | lista os 3 do seed + o novo (João Teste) |
| Cadastro de produto + estoque | `Produto cadastrado com ID 4.` (Cadeira Gamer) |
| Entrada de estoque (produto 1) | Teclado `50 → 60` |
| Venda com itens (3× Teclado + 2× Mouse) | Total bruto R$ 1110,00 |
| Desconto de R$ 50 | Total líquido **R$ 1060,00** |
| Confirmação da venda | `Venda confirmada com ID 1` |
| Baixa de estoque ao confirmar | Teclado `60 → 57`, Mouse `30 → 28` |
| Consulta de venda por ID e por cliente | Venda #1 com itens e totais |
| Cancelamento da venda | `Venda cancelada e estoque estornado.` |
| Estorno de estoque ao cancelar | Teclado `57 → 60`, Mouse `28 → 30` |
| Consulta por status (CANCELADA) | Venda #1 com status CANCELADA |
| Regra: estoque insuficiente | `Erro: Estoque insuficiente para o produto Teclado Mecanico` |

Os resultados são **idênticos** aos da versão JDBC — mesma regra de negócio, mesma base.

## SQL gerado pelo ORM (com `show_sql`)

Na confirmação da venda, o Hibernate gera automaticamente os comandos a partir do
mapeamento das entidades (trecho real da execução):

```sql
insert into venda (cliente_id, data_venda, desconto, status, total_bruto, total_liquido) values (?, ?, ?, ?, ?, ?)
insert into item_venda (preco_unitario, produto_id, quantidade, subtotal, venda_id) values (?, ?, ?, ?, ?)
select e1_0.id, ..., p1_0.nome, ... from estoque e1_0 join produto p1_0 on p1_0.id=e1_0.produto_id where p1_0.id=?
update estoque set localizacao=?, produto_id=?, quantidade_disponivel=?, quantidade_minima=?, ultima_atualizacao=? where id=?
```

## Controle transacional

Confirmação e cancelamento rodam em uma transação Hibernate única
(`TransactionManager.executar` → `session.beginTransaction()` + `commit`/`rollback`).
Teste de rollback (vender além do estoque): a operação falha e o estoque permanece
inalterado.

## Consulta por produto e regras de inativação

Execução real adicional (base recriada com o seed) cobrindo a consulta de vendas por
produto e as regras de cliente/produto inativo. Saída completa (com o SQL gerado) em
[`evidencia-consulta-produto.txt`](evidencia-consulta-produto.txt).

| Fluxo | Resultado |
|-------|-----------|
| Duas vendas confirmadas | #1 (Ana: Teclado×2 + Mouse×1 = R$ 680) e #2 (Bruno: Teclado×1 = R$ 250) |
| **Consulta por produto** (Teclado, ID 1) | retorna **as vendas #1 e #2** (ambas contêm o produto) |
| **Consulta por produto** (Monitor, ID 3) | `Nenhuma venda encontrada.` (não vendido) |
| Regra: cliente inativo | após inativar o cliente, nova venda → `Erro: Cliente inativo não pode realizar compras.` |
| Regra: produto inativo | após inativar o produto, adicioná-lo → `Erro: Produto inativo não pode ser vendido.` |

A consulta usa HQL com subconsulta `exists`, preservando o `join fetch` completo dos itens
(a venda é filtrada, mas todos os seus itens continuam carregados). SQL gerado pelo ORM:

```sql
select distinct v1_0.id, ..., i1_0.*, p1_0.*, c1_0.*
from venda v1_0
left join item_venda i1_0 on v1_0.id=i1_0.venda_id
left join produto p1_0 on p1_0.id=i1_0.produto_id
left join cliente c1_0 on c1_0.id=v1_0.cliente_id
where exists (select 1 from item_venda i2_0 where i2_0.produto_id=? and v1_0.id=i2_0.venda_id)
order by v1_0.data_venda desc
```
