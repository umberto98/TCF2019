package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Tools {

	/**
	 * funzione che ritorna una lista di path ai file contenuti nella directory
	 * @param 		path
	 * @return 		List<Path>
	 */
	public static List<Path> getFilesInDir(Path path) {

		if (Files.exists(path)){

			try (Stream<Path> walk = Files.walk(path)) {

				List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
				/* filtro i risultati per avere solo i file e li aggiungo alla lista result */

				return result;

			} catch (IOException e) {
				e.printStackTrace();
				/* stampo la stacktrace in caso di errore */

				return Collections.emptyList();
			}
		}
		else {
			System.out.println("Il path specificato non esiste");
			return Collections.emptyList();
		}
	}

	/* uso try with per assicurarmi che lo stream sia chiuso */



	public static List<Path> getDirsInDir(Path path) {
		if (Files.exists(path)){
			try (Stream<Path> walk = Files.walk(path)) {

				List<Path> result = walk.filter(Files::isDirectory).collect(Collectors.toList());

				return result;

			} catch (IOException e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}
		else {
			System.out.println("Il path specificato non esiste");
			return Collections.emptyList();
		}

	}
}
