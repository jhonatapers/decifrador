package br.com.crias.decifrador;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Lingua {

    private String nome;

    private double indiceCoincidencia;

    private Map<Character, Double> frequencias;

}
