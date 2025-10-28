package fit.iuh.se.servicesImpl;

import fit.iuh.se.dtos.CategoryDTO;
import fit.iuh.se.entities.Category;
import fit.iuh.se.repositories.CategoryRepository;
import fit.iuh.se.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepo;

    @Override
    public Page<CategoryDTO> findAll(Pageable pageable) {
        return categoryRepo.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public CategoryDTO findById(int id) {
        Optional<Category> opt = categoryRepo.findById(id);
        return opt.map(this::convertToDTO).orElse(null);
    }

    @Override
    public CategoryDTO save(CategoryDTO dto) {
        if (categoryRepo.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Tên danh mục đã tồn tại!");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category saved = categoryRepo.save(category);

        return convertToDTO(saved);
    }

    @Override
    public CategoryDTO update(int id, CategoryDTO dto) {
        Optional<Category> opt = categoryRepo.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy danh mục cần cập nhật!");
        }

        if (categoryRepo.existsByNameAndIdNot(dto.getName(), id)) {
            throw new RuntimeException("Tên danh mục đã tồn tại ở danh mục khác!");
        }

        Category category = opt.get();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category updated = categoryRepo.save(category);

        return convertToDTO(updated);
    }

    @Override
    public void delete(int id) {
        if (!canDelete(id)) {
            throw new RuntimeException("Không thể xóa danh mục vì vẫn còn sản phẩm!");
        }
        categoryRepo.deleteById(id);
    }

    @Override
    public Page<CategoryDTO> searchByName(String name, Pageable pageable) {
        return categoryRepo.findByNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepo.findByName(name).isPresent();
    }

    @Override
    public boolean canDelete(int id) {
        Optional<Category> opt = categoryRepo.findById(id);
        return opt.isPresent() && (opt.get().getProducts() == null || opt.get().getProducts().isEmpty());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
