package br.com.crias.decifrador;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.crias.decifrador.MetrificadorDeCaracteres.Metrica;

public class Vigenere {

    private int tamanhoChave = 0;

    private final String textoCifrado;

    private final Lingua[] linguas;

    private final Double margem = 0.002;

    private final MetrificadorDeCaracteres metrificadorDeCaracteres;

    private final CalculadoraDeCoincidencia calculadoraDeCoincidencia;

    public Vigenere(CalculadoraDeCoincidencia calculadoraDeCoincidencia,
            MetrificadorDeCaracteres metrificadorDeCaracteres,
            String textoCifrado,
            Lingua... linguas) {
        this.calculadoraDeCoincidencia = calculadoraDeCoincidencia;
        this.metrificadorDeCaracteres = metrificadorDeCaracteres;
        this.textoCifrado = textoCifrado;
        this.linguas = linguas;
    }

    public Character[] proximaPossivelChave() {
        tamanhoChave++;

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

        return revelarChave(tamanhoChave, subTextos, linguaAlvo);
    }

    public String decifrar(Character[] chave) {
        StringBuilder builder = new StringBuilder();
        int tamanhoChave = chave.length;
        int i = 0;
        for (Character character : textoCifrado.toCharArray()) {

            int charDecifrado = (character - chave[i] + 26) % 26 + 'a';

            builder.append((char) charDecifrado);
            i = i == tamanhoChave - 1 ? 0 : i + 1;
        }

        return builder.toString();

    }

    public Character[] decifrarChave(Character[] chave, String[] subTextos) {
        int i = 0;

        while (subTextos.length > i && chave.length > i) {

            int charChaveDecifrado = (subTextos[i].charAt(0) - chave[i] + 26) % 26 + 'a';

            chave[i] = (char) charChaveDecifrado;
            i++;
        }

        return chave;
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

            Entry<Character, Double> caracaterQueMaisApareceNaLingua = lingua.getFrequencias()
                    .entrySet()
                    .stream()
                    .max(Map.Entry.<Character, Double>comparingByValue())
                    .get();

            int diferenca = calcularDiferencaAscii(caracaterQueMaisApareceNaLingua.getKey(),
                    caracterTextoQueMaisAparece.getKey());

            chave[i] = encontrarCaractereCorrespondente(subTextos[i].charAt(0), diferenca);

        }

        return decifrarChave(chave, subTextos);
    }

    private <T> Set<T> ordenar(Set<T> set, Comparator<T> comparator) {
        return set.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int calcularDiferencaAscii(char char1, char value1) {
        return (char1 - value1 + 26) % 26;
    }

    private char encontrarCaractereCorrespondente(char char1, int diff) {
        int result = ((char1 - 'a' + diff) % 26 + 26) % 26 + 'a';
        return (char) result;
    }

}
