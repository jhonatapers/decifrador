package br.com.crias.decifrador;

import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.crias.decifrador.MetrificadorDeCaracteres.Metrica;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CalculadoraDeCoincidencia {

    private final MetrificadorDeCaracteres metrificadorDeCaracteres;

    public Double calcular(String texto) {

        final Long quantidadeCaracteresTotal = Long.valueOf(texto.length());

        Map<Character, Metrica> metricas = metrificadorDeCaracteres.metrificar(texto);

        return metricas.values().stream()
                .map(metrica -> (double) ((double) metrica.aparicoes() / (double) quantidadeCaracteresTotal)
                        * ((double) metrica.aparicoes() / (double) quantidadeCaracteresTotal))
                .mapToDouble(Double::doubleValue)
                .sum();
    }

}
