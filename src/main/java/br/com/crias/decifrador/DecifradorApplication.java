package br.com.crias.decifrador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class DecifradorApplication {

	private static Path arquivoDestino = Path.of(
			"C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\20201-teste1-decifrado.txt");

	public static void main(String[] args) {
		SpringApplication.run(DecifradorApplication.class, args);

		try (Scanner scanner = new Scanner(System.in)) {
			Vigenere vigenere = preparaSetup();

			Character[] penultimaChave = new Character[0];
			Character[] ultimaChave = vigenere.proximaPossivelChave();

			System.out.println("Ultima chave salva");
			printarChave(penultimaChave);

			boolean loop = true;
			while (loop) {

				switch (scanner.nextInt()) {
					case -1: {
						loop = false;
						break;
					}
					case 1: {

						penultimaChave = ultimaChave;
						ultimaChave = vigenere.proximaPossivelChave();

						System.out.println("Ultima chave");
						printarChave(ultimaChave);

						gravarConteudo(vigenere.decifrar(ultimaChave));
						break;
					}
				}

				ultimaChave = vigenere.proximaPossivelChave();

			}
		}

	}

	private static void printarChave(Character[] chave) {
		StringBuilder sb = new StringBuilder();
		for (Character ch : chave)
			sb.append(ch);

		System.out.println(sb.toString());
	}

	private static Vigenere preparaSetup() {

		try {

			Path path = Path.of(
					"C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\20201-teste1.txt");

			String textoCifrado = Files.readString(path, StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			String englishJson = Files.readString(
					Path.of("C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\english.json"),
					StandardCharsets.UTF_8);
			String portuguesJson = Files.readString(
					Path.of("C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\portugues.json"),
					StandardCharsets.UTF_8);

			Lingua english = mapper.readValue(englishJson, Lingua.class);

			Lingua portugues = mapper.readValue(portuguesJson, Lingua.class);

			return new Vigenere(new CalculadoraDeCoincidencia(new MetrificadorDeCaracteres()),
					new MetrificadorDeCaracteres(),
					textoCifrado,
					english,
					portugues);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static void gravarConteudo(String conteudo) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoDestino.toString()))) {
			writer.write(conteudo);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
