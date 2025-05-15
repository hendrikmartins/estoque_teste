package com.example.estoque.service;

import com.example.estoque.domain.Pedido;
import com.example.estoque.domain.Produto;
import com.example.estoque.entity.ProdutoEntity;
import com.example.estoque.exception.ForaDeEstoqueException;
import com.example.estoque.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceUnitTest {

    @InjectMocks
    private ProdutoService produtoService;

    @Mock
    private ProdutoRepository produtoRepository;

    private Produto produtoNovo;
    private Produto produtoExistenteComId;
    private ProdutoEntity produtoEntityNovo;
    private ProdutoEntity produtoEntityExistente;
    private Pedido pedidoComEstoque;
    private Pedido pedidoSemEstoque;

    @BeforeEach
    void setUp() {
        produtoNovo = new Produto("Produto A", "Descrição A", 10.0, 50);
        produtoExistenteComId = new Produto("Produto B", "Descrição B", 20.0, 100); // Instância para comparação em alguns cenários
        produtoEntityNovo = new ProdutoEntity(null, "Produto A", "Descrição A", 10.0, 50);
        produtoEntityExistente = new ProdutoEntity(1L, "Produto B", "Descrição B", 20.0, 100);
        pedidoComEstoque = new Pedido(1L, Collections.singletonList(new Produto("Produto B", "Descrição B", 20.0, 5)));
        pedidoSemEstoque = new Pedido(2L, Collections.singletonList(new Produto("Produto B", "Descrição B", 20.0, 150)));
    }

    @Test
    void dadoUmProdutoNovo_quandoCadastrarProduto_entaoOSalvaNoRepositorio() {
        when(produtoRepository.findByNome(produtoNovo.getNome())).thenReturn(null);
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoEntityNovo);

        produtoService.cadastrarProduto(produtoNovo);

        ProdutoEntity produtoEntityParaSalvar = new ProdutoEntity(produtoNovo);
        verify(produtoRepository, times(1)).save(produtoEntityParaSalvar);
    }

    @Test
    void dadoUmProdutoExistente_quandoCadastrarProduto_entaoAtualizaAQuantidadeNoRepositorio() {
        when(produtoRepository.findByNome(produtoExistenteComId.getNome())).thenReturn(produtoEntityExistente);
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoEntityExistente);

        produtoService.cadastrarProduto(produtoExistenteComId);

        assertEquals(produtoExistenteComId.getQtd(), produtoEntityExistente.getQtd());
        verify(produtoRepository, times(1)).save(produtoEntityExistente);
    }

    @Test
    void dadoQueExistemProdutosNoRepositorio_quandoEncontrarTodos_entaoRetornaUmaListaDeProdutos() {

        List<ProdutoEntity> produtoEntities = Arrays.asList(
                new ProdutoEntity(1L, "Produto X", "Desc X", 5.0, 20),
                new ProdutoEntity(2L, "Produto Y", "Desc Y", 15.0, 30)
        );
        when(produtoRepository.findAll()).thenReturn(produtoEntities);

        List<Produto> produtosRetornados = produtoService.encontrarTodos();

        assertEquals(produtoEntities.size(), produtosRetornados.size());
        assertEquals(produtoEntities.get(0).getNome(), produtosRetornados.get(0).getNome());
        assertEquals(produtoEntities.get(0).getDescricao(), produtosRetornados.get(0).getDescricao());
        assertEquals(produtoEntities.get(1).getNome(), produtosRetornados.get(1).getNome());
        assertEquals(produtoEntities.get(1).getDescricao(), produtosRetornados.get(1).getDescricao());
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    void dadoQueNaoExistemProdutosNoRepositorio_quandoEncontrarTodos_entaoRetornaUmaListaVazia() {
        when(produtoRepository.findAll()).thenReturn(Collections.emptyList());

        List<Produto> produtosRetornados = produtoService.encontrarTodos();

        assertTrue(produtosRetornados.isEmpty());
        verify(produtoRepository, times(1)).findAll();
    }

    @Test
    void dadoUmPedidoComEstoqueSuficiente_quandoAtualizarEstoque_entaoAtualizaOEstoqueNoRepositorio() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntityExistente));
        when(produtoRepository.save(any(ProdutoEntity.class))).thenReturn(produtoEntityExistente);

        assertDoesNotThrow(() -> produtoService.atualizarEstoque(pedidoComEstoque));

        int novaQuantidade = produtoEntityExistente.getQtd() - pedidoComEstoque.getItens().get(0).getQtd();
        assertEquals(novaQuantidade, produtoEntityExistente.getQtd());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(produtoEntityExistente);
    }

    @Test
    void dadoUmPedidoComEstoqueInsuficiente_quandoAtualizarEstoque_entaoLancaForaDeEstoqueException() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoEntityExistente));

        assertThrows(ForaDeEstoqueException.class, () -> produtoService.atualizarEstoque(pedidoSemEstoque));
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, never()).save(any(ProdutoEntity.class));
    }

    @Test
    void dadoUmNomeDeProdutoExistenteNoRepositorio_quandoEncontrarPorNome_entaoRetornaOProdutoCorrespondente() {
        when(produtoRepository.findByNome(produtoExistenteComId.getNome())).thenReturn(produtoEntityExistente);

        Produto produtoRetornado = produtoService.encontrarPorNome(produtoExistenteComId.getNome());

        assertEquals(produtoEntityExistente.getNome(), produtoRetornado.getNome());
        assertEquals(produtoEntityExistente.getDescricao(), produtoRetornado.getDescricao());
        assertEquals(produtoEntityExistente.getPreco(), produtoRetornado.getPreco());
        assertEquals(produtoEntityExistente.getQtd(), produtoRetornado.getQtd());
        verify(produtoRepository, times(1)).findByNome(produtoExistenteComId.getNome());
    }

    @Test
    void dadoUmNomeDeProdutoInexistenteNoRepositorio_quandoEncontrarPorNome_entaoRetornaUmProdutoComValoresNulosOuPadrao() {
        when(produtoRepository.findByNome("Produto Inexistente")).thenReturn(null);

        Produto produtoRetornado = produtoService.encontrarPorNome("Produto Inexistente");

        assertNull(produtoRetornado.getNome());
        assertNull(produtoRetornado.getDescricao());
        assertNull(produtoRetornado.getPreco());
        assertNull(produtoRetornado.getQtd());
        verify(produtoRepository, times(1)).findByNome("Produto Inexistente");
    }
}