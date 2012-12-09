package br.com.caelum.stella.boleto.bancos;

public class GeradorDeDigitoDeBoleto {

    public int geraDigitoMod11(String codigoDeBarras) {
        int soma = 0;
        for (int i = codigoDeBarras.length() - 1, multiplicador = 2; i >= 0; i--, multiplicador++) {
            if (multiplicador == 10) {
                multiplicador = 2;
            }
            soma += Integer.parseInt(String.valueOf(codigoDeBarras.charAt(i)))
                    * multiplicador;
        }

        int resto = soma % 11;

        int digito = 11 - resto;

        return digito > 9 ? 0 : digito;
    }

    public int geraDigitoMod10(String campo) {
        int soma = 0;
        for (int i = campo.length() - 1; i >= 0; i--) {
            int multiplicador = (campo.length() - i) % 2 + 1;
            int algarismoMultiplicado = Integer.parseInt(String.valueOf(campo
                    .charAt(i)))
                    * multiplicador;
            soma += (algarismoMultiplicado / 10) + (algarismoMultiplicado % 10);
        }

        int resto = soma % 10;
        int resultado = (10 - resto) % 10;
		return resultado;
    }

}
