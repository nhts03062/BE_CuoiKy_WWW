package fit.iuh.se.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fit.iuh.se.dtos.CategoryDTO;

public interface CategoryService {
    Page<CategoryDTO> findAll(Pageable pageable);

    CategoryDTO findById(int id);

    CategoryDTO save(CategoryDTO categoryDTO);

    CategoryDTO update(int id, CategoryDTO categoryDTO);

    void delete(int id);

    Page<CategoryDTO> searchByName(String name, Pageable pageable);

    boolean existsByName(String name);

    boolean canDelete(int id);
}
