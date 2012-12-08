package br.com.caelum.stella.boleto.bancos;

import java.net.URL;

import br.com.caelum.stella.boleto.Banco;
import br.com.caelum.stella.boleto.Boleto;
import br.com.caelum.stella.boleto.Emissor;
import br.com.caelum.stella.boleto.exception.CriacaoBoletoException;

/**
 * Esta classe possui a lógica necessária para gerar boletos da
 * Caixa Econômica Federal usando o modelo SIGCB.
 * <p>
 * Alguns dados são obrigatórios para gerar esse tipo de boleto:
 * <dl>
 * 	<dt><b>Carteira</b></dt>
 * 	<dd>1 - para título da modalidade REGISTRADA</dd>
 * 	<dd>2 - para título da modalidade SEM REGISTRO</dd>
 * </dl>
 * <dl>
 * 	<dt><b>Nosso Número</b></dt>
 * 	<dd>Número para seu controle sobre a emissão do boleto.
 * 		Este parâmetro pode conter até 15 caracteres</dd>
 * </dl>
 * <dl>
 * 	<dt><b>Código do Cedente (Código Fornecido pela Agência)</b></dt>
 * 	<dd>Este é um código fornecido pela sua agência que identifica
 * 		a conta que receberá o pagamento, entre em contato com o seu
 * 		gerente para maiores informações. Este campo pode conter até
 * 		6 caracteres.</dd>
 * </dl>
 * O código a seguir contém um exemplo de como gerar um boleto para
 * esse modelo de cobrança:
 * <pre>
 * Emissor emissor = Emissor.newEmissor()
 * 			.withCedente("Fulano da Silva")
 * 			.withAgencia(1234)
 * 			.withDigitoAgencia('1')
 * 			.withContaCorrente(4321)
 * 			.withDigitoContaCorrente('3')
 * 			.withCarteira(2)
 * 			.withNossoNumero(123)
 * 			.withCodigoFornecidoPelaAgencia(5507);
 *
 * Sacado sacado = Sacado.newSacado()
 * 			.withNome("Fulano de Tal")
 *
 * Datas datas = Datas.newDatas()
 * 			.withVencimento(10, 12, 2012);
 *
 * Banco banco = new CaixaSIGCB();
 *
 * Boleto boleto = Boleto.newBoleto()
 * 			.withBanco(banco)
 * 			.withDatas(datas)
 * 			.withEmissor(emissor)
 * 			.withSacado(sacado)
 * 			.withValorBoleto(100.00);
 * </pre>
 */
public class CaixaSIGCB implements Banco {
	/**
	 * Código de emissão do boleto feita pelo cedente
	 */
	private static final String CODIGO_EMISSAO_CEDENTE = "4";

	private static final String NUMERO_CAIXA = "104";

	/**
	 * Modalidade de Cobrança Registrada
	 */
	private static final String REGISTRADA = "RG";

	/**
	 * Modalidade de Cobrança Sem Registro
	 */
	private static final String SEM_REGISTRO = "SR";

	private final GeradorDeDigitoDeBoleto dvGenerator = new GeradorDeDigitoDeBoleto();

	@Override
	public String geraCodigoDeBarrasPara(Boleto boleto) {
		StringBuilder buffer = new StringBuilder();

		buffer.append(getNumeroFormatado());
		buffer.append(String.valueOf(boleto.getCodigoEspecieMoeda()));
		// Digito Verificador sera inserido aqui.

		buffer.append(boleto.getFatorVencimento());
		buffer.append(boleto.getValorFormatado());

		Emissor emissor = boleto.getEmissor();

		String codigoFornecidoPelaAgencia = getCodigoFornecidoPelaAgenciaDoEmissorFormatado(emissor);

		buffer.append(codigoFornecidoPelaAgencia);
		buffer.append(dvGenerator.geraDigitoMod11(codigoFornecidoPelaAgencia));

		String nossoNumero = getNossoNumeroDoEmissorFormatado(emissor);

		buffer.append(nossoNumero.substring(2, 5));
		buffer.append(nossoNumero.substring(0, 1));
		buffer.append(nossoNumero.substring(5, 8));
		buffer.append(nossoNumero.substring(1, 2));
		buffer.append(nossoNumero.substring(8));

		String campoLivre = buffer.substring(18);

		buffer.append(dvGenerator.geraDigitoMod11(campoLivre));

		buffer.insert(4, dvGenerator.geraDigitoMod11(buffer.toString()));

		String codigoDeBarras = buffer.toString();

		if (codigoDeBarras.length() != 44) {
			throw new CriacaoBoletoException(
					"Erro na geração do código de barras. Número de digitos diferente de 44. Verifique todos os dados.");
		}

		return codigoDeBarras;
	}

	@Override
	public String getCarteiraDoEmissorFormatado(Emissor emissor) {
		return emissor.getCarteira() == 1 ? REGISTRADA : SEM_REGISTRO;
	}

	@Override
	public String getContaCorrenteDoEmissorFormatado(Emissor emissor) {
		return String.format("%05d", emissor.getContaCorrente());
	}

	@Override
	public URL getImage() {
		return getClass().getResource(
				String.format("/br/com/caelum/stella/boleto/img/%s.png",
						getNumeroFormatado()));
	}

	@Override
	public String getNossoNumeroDoEmissorFormatado(Emissor emissor) {
		return emissor.getCarteira() + CODIGO_EMISSAO_CEDENTE
				+ String.format("%015d", emissor.getNossoNumero());
	}

	private String getCodigoFornecidoPelaAgenciaDoEmissorFormatado(
			Emissor emissor) {
		return String.format("%06d", emissor.getCodigoFornecidoPelaAgencia());
	}

	@Override
	public String getNumeroFormatado() {
		return NUMERO_CAIXA;
	}
}
