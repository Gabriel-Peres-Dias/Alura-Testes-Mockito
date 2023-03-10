import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.GeradorDePagamento;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;


import static org.mockito.Mockito.verify;

public class GeradorDePagamentoTest {

    @Mock
    private PagamentoDao pagamentoDao;

    private GeradorDePagamento service;

    @Captor
    private ArgumentCaptor<Pagamento> captor;

    @BeforeEach
    public void inicializar() {
        MockitoAnnotations.initMocks(this);
        this.service = new GeradorDePagamento(pagamentoDao);
    }

    @Test
    public void deveGerarPagamentoParaVencedorDoLeilao() {
        Leilao leilao = leilao();
        Lance lanceVencedor = leilao.getLanceVencedor();
        service.gerarPagamento(lanceVencedor);

        verify(pagamentoDao).salvar(captor.capture());
        Pagamento pagamento = captor.getValue();

        Assertions.assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        Assertions.assertEquals(LocalDate.now().plusDays(1), pagamento.getVencimento());
        Assertions.assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        Assertions.assertFalse(pagamento.getPago());
        Assertions.assertEquals(leilao, pagamento.getLeilao());
    }


    private Leilao leilao() {
        Leilao leilao = new Leilao("Celular",
                new BigDecimal("500"),
                new Usuario("Fulano"));

        Lance lance = new Lance(new Usuario("Ciclano"),
                new BigDecimal("900"));

        leilao.setLanceVencedor(lance);
        leilao.propoe(lance);

        return leilao;

    }
}
