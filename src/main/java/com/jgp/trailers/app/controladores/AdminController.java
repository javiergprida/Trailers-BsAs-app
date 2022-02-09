package com.jgp.trailers.app.controladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.jgp.trailers.app.modelo.Genero;
import com.jgp.trailers.app.modelo.Pelicula;
import com.jgp.trailers.app.repositorio.IGeneroRepository;
import com.jgp.trailers.app.repositorio.IPeliculaRepository;
import com.jgp.trailers.app.servicio.AlmacenServicioImpl;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private IPeliculaRepository peliculaRepositorio;

	@Autowired
	private IGeneroRepository generoRepositorio;

	@Autowired
	private AlmacenServicioImpl almacenServicio;
	
	

	@GetMapping("")
	public ModelAndView verPaginaDeInicio(@PageableDefault(sort = "titulo", size = 5) Pageable pageable) {
		Page<Pelicula> peliculas = peliculaRepositorio.findAll(pageable);
		return new ModelAndView("admin/index").addObject("peliculas", peliculas);
	}

	@GetMapping("/peliculas/nuevo")
	public ModelAndView mostrarFormularioNuevaPelicula() {
		List<Genero> generos = generoRepositorio.findAll(Sort.by("titulo"));
		return new ModelAndView("admin/nueva-pelicula")
				.addObject("pelicula", new Pelicula())
				.addObject("generos", generos);
	}
	
	@PostMapping("/peliculas/nuevo")
	public ModelAndView registrarPelicula(@Validated Pelicula pelicula, BindingResult bindingResult) {
		if(bindingResult.hasErrors() || pelicula.getPortada().isEmpty()) {
			if(pelicula.getPortada().isEmpty()) {
				bindingResult.rejectValue("portada", "MultipartNotEmpty");
			}
			List<Genero> generos = generoRepositorio.findAll(Sort.by("titulo"));
			return new ModelAndView("admin/nueva-pelicula")
					.addObject("pelicula", pelicula)
					.addObject("generos", generos);
		}
		
		String rutaPortada = almacenServicio.almacenarArchivo(pelicula.getPortada());
		pelicula.setRutaPortada(rutaPortada);
		peliculaRepositorio.save(pelicula);
		return new ModelAndView("redirect:/admin");
	}
	
	@GetMapping("/peliculas/{id}/editar")
	public ModelAndView editarPelicula(@PathVariable Integer id) {
		Pelicula pelicula = peliculaRepositorio.getOne(id);
		List<Genero> generos = generoRepositorio.findAll(Sort.by("titulo"));
		
		return new ModelAndView("admin/editar-pelicula")
				.addObject("pelicula", pelicula)
				.addObject("generos", generos);
	}
	@PostMapping("/peliculas/{id}/editar")
	public ModelAndView editarPelicula(@PathVariable Integer id, @Validated Pelicula pelicula, BindingResult bindingResult) {
		
		if(bindingResult.hasErrors() ) {
			List<Genero> generos = generoRepositorio.findAll(Sort.by("titulo"));
			return new ModelAndView("admin/editar-pelicula")
					.addObject("pelicula", pelicula)
					.addObject("generos", generos);
		}
		
		Pelicula peliculaDB = peliculaRepositorio.getOne(id);
		peliculaDB.setTitulo(pelicula.getTitulo());
		peliculaDB.setSinopsis(pelicula.getSinopsis());
		peliculaDB.setFechaEstreno(pelicula.getFechaEstreno());
		peliculaDB.setYotubeTrailerId(pelicula.getYotubeTrailerId());
		peliculaDB.setGeneros(pelicula.getGeneros());
		if(!pelicula.getPortada().isEmpty()) {
			almacenServicio.eliminarArchivo(peliculaDB.getRutaPortada());
			String rutaPortada = almacenServicio.almacenarArchivo(pelicula.getPortada());
			peliculaDB.setRutaPortada(rutaPortada);
		}
		
		peliculaRepositorio.save(peliculaDB);
		return new ModelAndView("redirect:/admin");
	}
	
	@PostMapping("/peliculas/{id}/eliminar")
	public String eliminarPelicula(@PathVariable Integer id) {
		Pelicula pelicula = peliculaRepositorio.getOne(id);
		peliculaRepositorio.delete(pelicula);
		almacenServicio.eliminarArchivo(pelicula.getRutaPortada());
		
		return "redirect:/admin";
	}
	
}
