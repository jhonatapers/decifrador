package br.com.crias.decifrador;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class DecifradorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DecifradorApplication.class, args);

		Path path = Path.of(
				"C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\20201-teste1.txt");
		String content = "";
		try {

			content = Files.readString(path, StandardCharsets.UTF_8);

			ObjectMapper mapper = new ObjectMapper();

			String englishJson = Files.readString(
					Path.of("C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\english.json"),
					StandardCharsets.UTF_8);
			String portuguesJson = Files.readString(
					Path.of("C:\\Users\\jhona\\Desktop\\Workspace\\pucrs\\seguranca de sistemas\\T1\\decifrador\\src\\main\\resources\\portugues.json"),
					StandardCharsets.UTF_8);

			Lingua english = mapper.readValue(englishJson, Lingua.class);

			Lingua portugues = mapper.readValue(portuguesJson, Lingua.class);

			Vigenere vigenere = new Vigenere(new CalculadoraDeCoincidencia(new MetrificadorDeCaracteres()),
					new MetrificadorDeCaracteres(),
					english,
					portugues);

			vigenere.decifrar(content);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// public static Lingua carregarFrequenciaDoYAML(String arquivoYAML) {
	// Yaml yaml = new Yaml();
	// InputStream inputStream =
	// Main.class.getClassLoader().getResourceAsStream(arquivoYAML);

	// if (inputStream == null) {
	// throw new IllegalArgumentException("Arquivo YAML não encontrado: " +
	// arquivoYAML);
	// }

	// Map<String, Object> data = yaml.load(inputStream);
	// Lingua frequencia = new Lingua();

	// frequencia.setIndiceCoincidencia((Double) data.get("indice-coincidencia"));
	// frequencia.setFrequencia((Map<String, Double>) data.get("frequencia"));

	// return frequencia;
	// }

}
