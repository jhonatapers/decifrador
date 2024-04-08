package br.com.crias.decifrador;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Vigenere {

    private int tamanhoChave = 0;

    private final String textoCifrado;

    private final Lingua[] linguas;

    private final Double margem = 0.003;

    private final CalculadoraDeCoincidencia calculadoraDeCoincidencia;

    public Vigenere(CalculadoraDeCoincidencia calculadoraDeCoincidencia,
            MetrificadorDeCaracteres metrificadorDeCaracteres,
            String textoCifrado,
            Lingua... linguas) {
        this.calculadoraDeCoincidencia = calculadoraDeCoincidencia;
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
            chave[i] = analiseFrequenciaBhattacharyya(subTextos[i], lingua.getFrequencias().values().stream().mapToDouble(Double::doubleValue).toArray());
        }

        return chave;
    }

    private char analiseFrequencia(String sequencia, double[] frequenciasIngles) {
        Map<Character, Double> frequencias = new HashMap<>();
        for (char c : sequencia.toCharArray()) {
            frequencias.put(c, frequencias.getOrDefault(c, 0.0) + 1);
        }

        List<Entry<Character, Double>> frequenciasOrdenadas = frequencias.entrySet()
                .stream()
                .sorted(Comparator.comparing(Entry::getValue))
                .collect(Collectors.toList());

        char deslocamento = (char) (frequenciasOrdenadas.get(frequenciasOrdenadas.size() - 1).getKey() - 'e' + 'a');

        return deslocamento;
    }

    private char analiseFrequenciaQuiQuadrado(String sequencia, double[] frequenciasLingua) {
        double menorQuiQuadrado = Double.MAX_VALUE;
        char melhorCaractere = 'a';
    
        for (int deslocamento = 0; deslocamento < 26; deslocamento++) {
            double somaQuiQuadrados = 0.0;
    
            // Calcula os qui-quadrados
            for (int i = 0; i < 26; i++) {
                double frequenciaAtual = frequenciasLingua[i];
                double frequenciaObservada = frequenciaObservada(sequencia, deslocamento, i);
                double quiQuadrado = Math.pow(frequenciaObservada - frequenciaAtual, 2) / frequenciaAtual;
                somaQuiQuadrados += quiQuadrado;
            }
    
            if (somaQuiQuadrados < menorQuiQuadrado) {
                menorQuiQuadrado = somaQuiQuadrados;
                melhorCaractere = (char) ('a' + deslocamento);
            }
        }
    
        return melhorCaractere;
    }

    private char analiseFrequenciaBhattacharyya(String subtexto, double[] frequenciasLingua) {
        double menorDistancia = Double.MAX_VALUE;
        char melhorCaractere = 'a';
    
        for (int deslocamento = 0; deslocamento < 26; deslocamento++) {
            double distancia = 0.0;
    
            // Calcula a distância de Bhattacharyya
            double somaRaizProdutos = 0.0;
            for (int i = 0; i < 26; i++) {
                double frequenciaAtual = frequenciasLingua[i];
                double frequenciaObservada = frequenciaObservada(subtexto, deslocamento, i);
                somaRaizProdutos += Math.sqrt(frequenciaAtual * frequenciaObservada);
            }
            distancia = -Math.log(somaRaizProdutos);
    
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhorCaractere = (char) ('a' + deslocamento);
            }
        }
    
        return melhorCaractere;
    }
    
    private Double frequenciaObservada(String texto, Integer deslocamento, Integer index) {
        int[] contagemCaracteres = new int[26];
        int totalCaracteres = 0;
    
        for (int i = 0; i < texto.length(); i++) {
            char caractere = texto.charAt(i);
            if (Character.isLetter(caractere)) {
                char caractereDeslocado = (char) ('a' + (caractere - 'a' - deslocamento + 26) % 26);
                int indice = caractereDeslocado - 'a';
                contagemCaracteres[indice]++;
                totalCaracteres++;
            }
        }

        return (double) contagemCaracteres[index] / totalCaracteres;

    }

}
