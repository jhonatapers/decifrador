package br.com.crias.decifrador;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class MetrificadorDeCaracteres {

    public Map<Character, Metrica> metrificar(String texto) {

        ConcurrentHashMap<Character, Long> contagem = new ConcurrentHashMap<>();

        for (Character character : texto.toCharArray())
            contagem.put(character, contagem.getOrDefault(character, 0L) + 1L);

        ConcurrentHashMap<Character, Metrica> metricas = new ConcurrentHashMap<>();

        final Long quantidadeCaracteresTotal = Long.valueOf(texto.length());

        for (Entry<Character, Long> entry : contagem.entrySet())
            metricas.put(entry.getKey(),
                    new Metrica(entry.getValue(), (double) entry.getValue() / (double) quantidadeCaracteresTotal));

        return metricas;
    }

    public static record Metrica(Long aparicoes, Double porcentagemAparicoes) {
    }

}
