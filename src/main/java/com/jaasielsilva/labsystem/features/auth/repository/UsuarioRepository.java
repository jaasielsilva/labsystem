package com.jaasielsilva.labsystem.features.auth.repository;

import com.jaasielsilva.labsystem.features.auth.entity.Perfil;
import com.jaasielsilva.labsystem.features.auth.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u JOIN FETCH u.empresa WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Override
    @EntityGraph(attributePaths = "empresa")
    Page<Usuario> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "empresa")
    Page<Usuario> findAllByEmpresaId(Long empresaId, Pageable pageable);

    @EntityGraph(attributePaths = "empresa")
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = "empresa")
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Usuario> searchByNome(@Param("term") String term, Pageable pageable);

    @EntityGraph(attributePaths = "empresa")
    @Query("""
            SELECT u FROM Usuario u
            WHERE u.empresa.id = :empresaId
            AND LOWER(u.nome) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<Usuario> searchByNomeAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("term") String term,
            Pageable pageable
    );

    long countByEmpresa_Id(Long empresaId);

    long countByPerfil(Perfil perfil);
}
