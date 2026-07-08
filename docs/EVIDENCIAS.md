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
