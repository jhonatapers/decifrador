package br.com.crias.decifrador;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

import br.com.crias.decifrador.MetrificadorDeCaracteres.Metrica;

@Component
public class Vigenere implements Decifrador {

    Semaphore barreira;

    private final Lingua[] linguas;

    private final Double margem = 0.002;

    // private final Double indiceCoincidenciaAlvo = 0.066;

    private final MetrificadorDeCaracteres metrificadorDeCaracteres;

    private final CalculadoraDeCoincidencia calculadoraDeCoincidencia;

    public Vigenere(CalculadoraDeCoincidencia calculadoraDeCoincidencia,
            MetrificadorDeCaracteres metrificadorDeCaracteres,
            Lingua... linguas) {
        this.calculadoraDeCoincidencia = calculadoraDeCoincidencia;
        this.metrificadorDeCaracteres = metrificadorDeCaracteres;
        this.linguas = linguas;
        this.barreira = new Semaphore(linguas.length);
    }

    @Override
    public String decifrar(String textoCifrado) {

        int tamanhoChave = 1;
        String[] subTextos = capturarSubTextosPorTamanhoDaChave(textoCifrado, tamanhoChave);
        double indiceCoincidenciaEncontrado = calcularIndiceCoincidencia(subTextos);

        while (!dentroDaMargem(indiceCoincidenciaEncontrado)) {
            if (tamanhoChave == textoCifrado.length())
                break;

            tamanhoChave++;
            subTextos = capturarSubTextosPorTamanhoDaChave(textoCifrado, tamanhoChave);
            indiceCoincidenciaEncontrado = calcularIndiceCoincidencia(subTextos);
        }

        Lingua linguaAlvo = linguaOndeIndiceCoincidenciaMelhorSeAdequa(indiceCoincidenciaEncontrado);

        Character[] chave = revelarChave(tamanhoChave, subTextos, linguaAlvo);

        return decifrar(textoCifrado, chave);
    }

    private Lingua linguaOndeIndiceCoincidenciaMelhorSeAdequa(final double melhorIndiceCoincidencia) {
        return List.of(linguas)
                .stream()
                .filter(lingua -> margem >= Math.max(melhorIndiceCoincidencia, lingua.getIndiceCoincidencia())
                        - Math.min(melhorIndiceCoincidencia, lingua.getIndiceCoincidencia()))
                .findFirst()
                .orElseThrow(null);
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
        return List.of(linguas)
                .stream()
                .anyMatch(lingua -> margem >= Math.max(indiceCoincidenciaEncontrado, lingua.getIndiceCoincidencia())
                        - Math.min(indiceCoincidenciaEncontrado, lingua.getIndiceCoincidencia()));
    }

    private Character[] revelarChave(int tamanhoChave, String[] subTextos, Lingua lingua) {

        Character[] chave = new Character[tamanhoChave];

        for (int i = 0; i < tamanhoChave; i++) {

            Map<Character, Metrica> metricas = metrificadorDeCaracteres.metrificar(subTextos[i]);

            Set<Map.Entry<Character, Metrica>> caracteresTextoOrdenadoPorAparicoes = ordenar(metricas.entrySet(),
                    Map.Entry.<Character, Metrica>comparingByValue(
                            Comparator.comparingDouble(Metrica::porcentagemAparicoes).reversed()));

            Entry<Character, Metrica> caracterTextoQueMaisAparece = caracteresTextoOrdenadoPorAparicoes.stream()
                    .max(Map.Entry.<Character, Metrica>comparingByValue(
                            Comparator.comparingDouble(Metrica::porcentagemAparicoes)))
                    .get();

            Entry<Character, Double> aham = acharValorMaisProximo(lingua.getFrequencias().entrySet(),
                    caracterTextoQueMaisAparece.getValue().aparicoes());

            int diferenca = calcularDiferencaAscii(aham.getKey(), caracterTextoQueMaisAparece.getKey());

            chave[i] = encontrarCaractereCorrespondente(subTextos[i].charAt(0), diferenca);
        }

        return chave;
    }

    private <T> Set<T> ordenar(Set<T> set, Comparator<T> comparator) {
        return set.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map.Entry<Character, Double> acharValorMaisProximo(Set<Map.Entry<Character, Double>> entrySet,
            double targetValue) {
        return entrySet.stream()
                .min((entry1, entry2) -> Double.compare(Math.abs(entry1.getValue() - targetValue),
                        Math.abs(entry2.getValue() - targetValue)))
                .orElse(null);
    }

    private int calcularDiferencaAscii(char char1, char value1) {
        return (char1 - value1 + 26) % 26;
    }

    private char encontrarCaractereCorrespondente(char char1, int diff) {
        int result = ((char1 - 'a' + diff) % 26 + 26) % 26 + 'a';
        return (char) result;
    }

    private String decifrar(String textoCifrado, Character[] chave) {
        StringBuilder builder = new StringBuilder();
        int tamanhoChave = chave.length;
        int i = 0;
        for (Character character : textoCifrado.toCharArray()) {
            int diff = calcularDiferencaAscii(character, chave[i]);
            Character charDecifrado = encontrarCaractereCorrespondente(character, diff);
            builder.append(charDecifrado);
            i = i == tamanhoChave - 1 ? 0 : i + 1;
        }

        return builder.toString();

    }

}
