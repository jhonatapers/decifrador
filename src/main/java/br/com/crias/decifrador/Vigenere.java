package br.com.crias.decifrador;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class Vigenere implements Decifrador {

    private final Double indiceCoincidenciaAlvo = 0.066;

    private final Double margem = 0.002;

    private final CalculadoraDeCoincidencia calculadoraDeCoincidencia;

    @Override
    public String decifrar(String textoCifrado) {

        int tamanhoChave = 1;
        String[] subTextos = capturarSubTextosPorTamanhoDaChave(textoCifrado, tamanhoChave);

        while (!dentroDaMargem(calcularIndiceCoincidencia(subTextos))) {
            if (tamanhoChave == textoCifrado.length())
                break;

            tamanhoChave++;
            subTextos = capturarSubTextosPorTamanhoDaChave(textoCifrado, tamanhoChave);
        }

        return "";
    }

    private double calcularIndiceCoincidencia(String[] subTextos) {
        Collection<Double> indicesCoincidencias = new ArrayList<>();
        for (int i = 0; i < subTextos.length; i++)
            indicesCoincidencias.add(calculadoraDeCoincidencia.calcular(subTextos[i]));

        return indicesCoincidencias.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    private String[] capturarSubTextosPorTamanhoDaChave(String texto, int tamanhoChave) {

        StringBuilder[] builders = new StringBuilder[tamanhoChave];
        for (int h = 0; h < builders.length; h++)
            builders[h] = new StringBuilder();

        int i = 0;
        for (Character character : texto.toCharArray()) {
            builders[i].append(character);
            i = i == tamanhoChave - 1 ? 0 : i + 1;
        }

        String[] subTextos = new String[tamanhoChave];
        for (int j = 0; j < subTextos.length; j++)
            subTextos[j] = builders[j].toString();

        return subTextos;

    }

    private boolean dentroDaMargem(Double indiceCoincidenciaEncontrado) {
        return margem >= Math.max(indiceCoincidenciaEncontrado, indiceCoincidenciaAlvo)
                - Math.min(indiceCoincidenciaEncontrado, indiceCoincidenciaAlvo);
    }

}
