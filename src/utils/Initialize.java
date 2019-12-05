package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import controllers.FileController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import models.Track;
import models.TrackList;
import userinterface.MainApp;

public class Initialize {

	public static final void setMainDir() {
		try (BufferedReader br= Files.newBufferedReader(Tools.DIRFILEPATH)){
			br.lines().forEach(line->{
				if(!line.isEmpty() && Files.isDirectory(Paths.get(line))) {
					MainApp.mainDirList.add(line);
				}

			});
		} catch (IOException e) { 
			System.out.println("mainDir non selezionata");
		}

		while(MainApp.mainDirList.size() == 0) {
			if(!addDirectory()) {
				Platform.exit();
				break;
			}
		}
	}

	public static boolean addDirectory() {
		boolean setAttempt = false;

		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Select Directory");
		dialog.setHeaderText("Inserire il path alla directory selezionata");
		dialog.setContentText("L'operazione porebbe richiedere del tempo");

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return dialog.getEditor().getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent() && result.get() != null){
			setAttempt = true;
			if(Files.isDirectory(Paths.get(result.get())) && !result.get().isEmpty()){
				boolean isContained = false;
				boolean contains = false;
				String savedDir = "";

				for(String s:MainApp.mainDirList){
					isContained = Tools.getDirsInDir(Paths.get(s)).contains(Paths.get(result.get()));
					contains = Tools.getDirsInDir(Paths.get(result.get())).contains(Paths.get(s));
					if(isContained || contains) {
						savedDir = s;
						System.out.println("contains " + contains + " is contained "+ isContained);
						break;
					}
				};


				if(!isContained && !contains) {
					MainApp.mainDirList.add(result.get());
					setDirSongs(result.get());
					try (BufferedWriter bw= Files.newBufferedWriter(Tools.DIRFILEPATH)){
						MainApp.mainDirList.forEach(dir->{
							try {
								bw.write(dir);
								bw.newLine();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if(isContained){
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Errore");
					alert.setHeaderText("La directory indicata � gia inclusa in una delle directory salvate: " + savedDir);
					alert.showAndWait();
					return addDirectory();
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Errore");
					alert.setHeaderText("La directory indicata include una directory gi� salvata: " + savedDir);
					alert.showAndWait();
					return addDirectory();
				}
			}
			else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Errore");
				alert.setHeaderText("Il path indicato non � una directory");
				alert.showAndWait();
				return addDirectory();
			}
		}
		return setAttempt;



	}


	public static TrackList getAllSongs() {
		List<String> toBeRemoved = new ArrayList<String>();
		TrackList tracklist = new TrackList();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Tools.ALLSONGSFILEPATH.toString()), "utf-8"))){

			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split("&tcf&");
				if (arr.length == 7) {
					Path path = Paths.get(arr[6]);
					if(Files.isRegularFile(path)){
						tracklist.addTrack(new Track(arr));;
					}
					else {
						System.out.println(path.toString() + "\tNon � un file corretto");
						toBeRemoved.add(line);
					}				
				}
			}
		} catch (IOException e) {
			//TODO errore
			System.out.println("Non � stato possibile caricare le canzoni");
			e.printStackTrace();
		}
		
		if(toBeRemoved.size() > 0) removeTrackFromDirFile(toBeRemoved);
		return tracklist;
	}


	public static void setDirSongs(String dir) {
		Path dirPath = Paths.get(dir);

		TrackList tl = FileController.getFilesFromDir(dirPath);
		tl.setMetadata();

		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Tools.ALLSONGSFILEPATH.toString(), true), "UTF-8"))){
			bw.newLine();
			bw.newLine();
			bw.write(dir);
			tl.forEach(t->{
				try {
					bw.newLine();
					bw.write(t.getString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Completato");
		alert.setHeaderText("Canzoni impostate correttamente");
		alert.showAndWait();
	}

	public static void removeDir(String dir) {
		File f = new File(Tools.ALLSONGSFILEPATH.toString());
		File temp = new File("temp.txt");
		try (BufferedWriter bw = Files.newBufferedWriter(temp.toPath());
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Tools.ALLSONGSFILEPATH.toString()), "UTF-8"));){
			String currentLine;
			boolean remove = false;
			while((currentLine = br.readLine()) != null){
				if(currentLine == dir) remove = true;
				else if(MainApp.mainDirList.contains(currentLine)) remove = false;
				if(remove) {
					System.out.println(currentLine);
					currentLine = "";
				}
				bw.write(currentLine + System.getProperty("line.separator"));
			}
			f.delete();
			temp.renameTo(f);
		} catch (Exception e) {
			Tools.stackTrace(e);
		}
		MainApp.mainDirList.remove(dir);
		
		try(BufferedWriter bw = Files.newBufferedWriter(Tools.DIRFILEPATH)){
			MainApp.mainDirList.forEach(s->{
				try {
					bw.write(s+System.getProperty("line.separator"));
				} catch (IOException e) {
					Tools.stackTrace(e);
				}
			});
		} catch (Exception e) {
			Tools.stackTrace(e);
		}
	}

	public static void removeTrackFromDirFile(List<String> trackString) {
		File f = new File(Tools.ALLSONGSFILEPATH.toString());
		File temp = new File(Paths.get("files", "temp.txt").toString());
		try (BufferedWriter bw = Files.newBufferedWriter(temp.toPath());
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Tools.ALLSONGSFILEPATH.toString()), "UTF-8"));){
			String currentLine;
			while((currentLine = br.readLine()) != null){
				if(trackString.contains(currentLine)){
					System.out.println("Song removed " + currentLine);
					currentLine = "";
				}
				bw.write(currentLine + System.getProperty("line.separator"));

			}
			f.delete();
		} catch (Exception e) {
			Tools.stackTrace(e);
		}
		try {
			Files.copy(Paths.get("files", "temp.txt"), Tools.ALLSONGSFILEPATH, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Tools.stackTrace(e);
		}
	}
	
	public static void removeTrackFromDirFile(Track track) {
		List<String> l = new ArrayList<String>();
		l.add(track.getString());
		removeTrackFromDirFile(l);
	}
}