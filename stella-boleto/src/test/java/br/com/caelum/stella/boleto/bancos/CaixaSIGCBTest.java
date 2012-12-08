package br.com.caelum.stella.boleto.bancos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.StringEndsWith.endsWith;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import br.com.caelum.stella.boleto.Banco;
import br.com.caelum.stella.boleto.Boleto;
import br.com.caelum.stella.boleto.Datas;
import br.com.caelum.stella.boleto.Emissor;
import br.com.caelum.stella.boleto.exception.CriacaoBoletoException;

public class CaixaSIGCBTest {
	private Banco banco;
	private Emissor emissor;
	private Boleto boleto;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void codigoCarteiraRegistradaQuandoCarteiraIgualUm()
			throws Exception {
		emissor.withCarteira(1);

		String resultado = banco.getCarteiraDoEmissorFormatado(emissor);

		assertThat(resultado, is("RG"));
	}

	@Test
	public void codigoCarteiraSemRegistroQuandoCarteiraDiferenteDeUm()
			throws Exception {
		for (int i = 2; i < 10; i++) {
			emissor.withCarteira(i);

			String resultado = banco.getCarteiraDoEmissorFormatado(emissor);

			assertThat(resultado, is("SR"));
		}
	}

	@Test
	public void lancaExcecaoQuandoTamanhoDoCodigoDeBarrasInvalido()
			throws Exception {
		emissor.withCarteira(12);

		thrown.expect(CriacaoBoletoException.class);
		thrown.expectMessage(is("Erro na geração do código de barras. Número de digitos diferente de 44. Verifique todos os dados."));

		banco.geraCodigoDeBarrasPara(boleto);
	}

	@Test
	public void getImageForCaixa() {
		URL imageUrl = banco.getImage();

		assertThat(imageUrl, notNullValue());
		assertThat(imageUrl.toString(),
				endsWith("/br/com/caelum/stella/boleto/img/104.png"));
	}

	@Test
	public void geraCodigoDeBarraParaBoletoCaixaSIGCB() throws Exception {
		String codigoDeBarras = banco.geraCodigoDeBarrasPara(boleto);

		assertThat(codigoDeBarras,
				is("10491324200000321120055077000100040000000190"));
	}

	@Test
	public void getNossoNumeroDoEmissorFormatado() throws Exception {
		String nossoNumero = banco.getNossoNumeroDoEmissorFormatado(emissor);

		assertThat(nossoNumero, is("14000000000000019"));
	}

	@Test
	public void getNumeroFormatado() throws Exception {
		assertThat(banco.getNumeroFormatado(), is("104"));
	}

	@Test
	public void getContaCorrenteDoEmissorFormatada() throws Exception {
		String contaCorrente = banco
				.getContaCorrenteDoEmissorFormatado(emissor);

		assertThat(contaCorrente, is("04321"));
	}

	@Before
	public void setUp() {
		banco = new CaixaSIGCB();

		emissor = Emissor.newEmissor()
				.withCedente("Fulano da Silva")
				.withAgencia(1234)
				.withDigitoAgencia('1')
				.withContaCorrente(4321)
				.withDigitoContaCorrente('3')
				.withCarteira(1)
				.withNossoNumero(19)
				.withCodigoFornecidoPelaAgencia(5507);

		Datas datas = Datas.newDatas().withVencimento(23, 8, 2006);

		boleto = Boleto.newBoleto()
				.withDatas(datas)
				.withEmissor(emissor)
				.withValorBoleto(321.12);
	}
}
