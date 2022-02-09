package com.jgp.trailers.app.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jgp.trailers.app.modelo.Pelicula;

public interface IPeliculaRepository extends JpaRepository<Pelicula, Integer>{

}
