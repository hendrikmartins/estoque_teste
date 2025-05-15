package com.example.estoque.domain;

import java.util.List;

public class Pedido {

    List<ItemPedido> itens;

    public <T> Pedido(long l, List<T> produtoTeste) {
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
}
