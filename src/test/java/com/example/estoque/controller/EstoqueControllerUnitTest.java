package com.example.estoque.controller;

import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueControllerUnitTest {

    @InjectMocks
    private EstoqueController controller;

    @Mock
    private ProdutoService service;

    private Produto produtoValido;
    private Pedido pedidoComEstoque;
    private Pedido pedidoSemEstoque;
    private final String NOME_PRODUTO = "Produto Teste";

    @BeforeEach
    void setUp() {
        produtoValido = new Produto("Produto Teste", 10.0, 100);
        pedidoComEstoque = new Pedido(1L, Collections.singletonList(new Produto("Produto Teste", 10.0, 5)));
        pedidoSemEstoque = new Pedido(2L, Collections.singletonList(new Produto("Produto Teste", 10.0, 150)));
    }

    @Test
    void dadoUmProdutoValido_quandoCadastraProduto_entaoRetornaOkComMensagemDeSucesso() {
        ResponseEntity<String> response = controller.cadastraProduto(produtoValido);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cadastrado com Sucesso", response.getBody());
        verify(service, times(1)).cadastrarProduto(produtoValido);
    }

    @Test
    void dadoQueExistemProdutosNoEstoque_quandoListarProdutos_entaoRetornaOkComAListaDeProdutos() {
        List<Produto> produtos = Arrays.asList(new Produto("Produto A", 5.0, 20), new Produto("Produto B", 15.0, 30));
        when(service.encontrarTodos()).thenReturn(produtos);

        ResponseEntity<List<Produto>> response = controller.listarProdutos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(produtos, response.getBody());
        verify(service, times(1)).encontrarTodos();
    }

    @Test
    void dadoQueNaoExistemProdutosNoEstoque_quandoListarProdutos_entaoRetornaOkComListaVazia() {
        when(service.encontrarTodos()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Produto>> response = controller.listarProdutos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.emptyList(), response.getBody());
        verify(service, times(1)).encontrarTodos();
    }

    @Test
    void dadoUmNomeDeProdutoExistente_quandoBuscaProduto_entaoRetornaOkComOProdutoEncontrado() {
        when(service.encontrarPorNome(NOME_PRODUTO)).thenReturn(produtoValido);

        ResponseEntity<Produto> response = controller.buscaProduto(NOME_PRODUTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(produtoValido, response.getBody());
        verify(service, times(1)).encontrarPorNome(NOME_PRODUTO);
    }

    @Test
    void dadoUmNomeDeProdutoInexistente_quandoBuscaProduto_entaoRetornaOkComNull() {
        when(service.encontrarPorNome(anyString())).thenReturn(null);

        ResponseEntity<Produto> response = controller.buscaProduto("Produto Inexistente");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(service, times(1)).encontrarPorNome("Produto Inexistente");
    }

    @Test
    void dadoUmPedidoComEstoqueSuficiente_quandoAtualizarEstoque_entaoRetornaOkComMensagemDeSucesso() throws ForaDeEstoqueException {
        doNothing().when(service).atualizarEstoque(pedidoComEstoque);

        ResponseEntity<String> response = controller.atualizarEstoque(pedidoComEstoque);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Estoque Atualizado", response.getBody());
        verify(service, times(1)).atualizarEstoque(pedidoComEstoque);
    }

    @Test
    void dadoUmPedidoComEstoqueInsuficiente_quandoAtualizarEstoque_entaoRetornaBadRequestComMensagemDeErro() throws ForaDeEstoqueException {
        doThrow(new ForaDeEstoqueException("Estoque insuficiente para o produto Produto Teste."))
                .when(service).atualizarEstoque(pedidoSemEstoque);

        ResponseEntity<String> response = controller.atualizarEstoque(pedidoSemEstoque);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Estoque insuficiente para o produto Produto Teste.", response.getBody());
        verify(service, times(1)).atualizarEstoque(pedidoSemEstoque);
    }
}