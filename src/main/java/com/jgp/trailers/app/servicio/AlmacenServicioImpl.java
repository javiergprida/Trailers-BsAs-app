package com.jgp.trailers.app.servicio;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.jgp.trailers.app.excepciones.AlmacenException;
import com.jgp.trailers.app.excepciones.FileNotFoundException;

@Service
public class AlmacenServicioImpl implements IAlmacenServicio {

	@Value("${storage.location}")
	private String storageLocation;

	// esta anotacion sirve para indicar que este metodo se va a ejecutar
	// cada vesz que haya una nueva instancia de esta clase
	@PostConstruct
	@Override
	public void iniciaralmacenDeArchivos() {
		try {
			Files.createDirectories(Paths.get(storageLocation));
		} catch (IOException ex) {
			throw new AlmacenException("Error al inicializar la ubicacion en el almacen de archivos");
		}
	}

	@Override
	public String almacenarArchivo(MultipartFile archivo) {
		
		String nombreArchivo = archivo.getOriginalFilename();
		if(archivo.isEmpty()) {
			throw new AlmacenException("No se puede almacenar un archivo vacio");
		}else {
			try {
				InputStream inputStream = archivo.getInputStream();
				Files.copy(inputStream, Paths.get(storageLocation).resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);
				
			}catch(IOException ex) {
				throw new AlmacenException("Error al almacenar el archivo" + nombreArchivo, ex);
			}
			return nombreArchivo;
		}
	}

	@Override
	public Path cargarArchivo(String nombreArchivo) {
		return Paths.get(storageLocation).resolve(nombreArchivo);
	}

	@Override
	public Resource cargarComoRecurso(String nombreArchivo) {
		try {
			Path archivo = cargarArchivo(nombreArchivo);
			Resource recurso = (Resource) new UrlResource(archivo.toUri());
			
			if(recurso.exists() || recurso.isReadable()) {
				return recurso;
			}else {
				throw new FileNotFoundException("No se pudo encontrar el archico"+ nombreArchivo);
			}
				
		}catch(MalformedURLException ex) {
			throw new FileNotFoundException("No se pudo encontrar el archico"+ nombreArchivo, ex);
		}
	}

	@Override
	public void eliminarArchivo(String nombreArchivo) {
		Path archivo = cargarArchivo(nombreArchivo);
		
		try {
			FileSystemUtils.deleteRecursively(archivo);
		}catch(Exception ex) {
			System.out.print("Error: "+ ex);
		}

	}

}
