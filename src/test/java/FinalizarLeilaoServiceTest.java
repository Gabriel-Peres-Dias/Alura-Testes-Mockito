import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import br.com.alura.leilao.service.EnviadorDeEmails;
import br.com.alura.leilao.service.FinalizarLeilaoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService service;

    @Mock
    private LeilaoDao leilaoDao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.service = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
    }

    @Test
    void deveriaFinalizarUmLeiao() {
        //inicializo com os valores que vou precisar
        List<Leilao> leiloes = leiloes();

        //quando a essa ação foi feita
        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        // resultado esperado
        Assertions.assertTrue(leilao.isFechado());
        Assertions.assertEquals(new BigDecimal(900), leilao.getLanceVencedor().getValor());

        //verificar se o dao feito, no caso o que ele salvou foi esse primeiro leilao
        verify(leilaoDao).salvar(leilao);
    }

    @Test
    void deveriaDeveriaEnviarEmailParaVencedorDoLeilao() {
        List<Leilao> leiloes = leiloes();

        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
        service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Lance lanceVencedor = leilao.getLanceVencedor();

        verify(leilaoDao).salvar(leilao);
        verify(enviadorDeEmails).enviarEmailVencedorLeilao(leilao.getLanceVencedor());
    }

    @Test
    void naoDeveriaDeveriaEnviarEmailParaVencedorDoLeilaoEmCasoDeErroAoEncerrarLeilao() {
        List<Leilao> leiloes = leiloes();

        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
        //metódo thenThow usado para forçar o resultado do método jogar uma exception
        when(leilaoDao.salvar(any())).thenThrow(RuntimeException.class);

        try {
            service.finalizarLeiloesExpirados();
            verifyNoInteractions(enviadorDeEmails);
        } catch (Exception e) {}

    }


    private List<Leilao> leiloes() {
        List<Leilao> lista = new ArrayList<>();

        Leilao leilao = new Leilao("Celular",
                new BigDecimal("500"),
                new Usuario("Fulano"));

        Lance primeiro = new Lance(new Usuario("Beltrano"),
                new BigDecimal("600"));
        Lance segundo = new Lance(new Usuario("Ciclano"),
                new BigDecimal("900"));

        leilao.propoe(primeiro);
        leilao.propoe(segundo);

        lista.add(leilao);

        return lista;

    }
}
