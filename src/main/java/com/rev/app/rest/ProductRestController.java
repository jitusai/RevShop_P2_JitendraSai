package com.rev.app.rest;

import com.rev.app.dto.ProductDTO;
import com.rev.app.entity.Product;
import com.rev.app.mapper.ProductMapper;
import com.rev.app.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final IProductService IProductService;

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return IProductService.findAllDTOs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return IProductService.findById(id)
                .map(product -> ResponseEntity.ok(ProductMapper.toDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        Product product = ProductMapper.toEntity(productDTO);
        Product saved = IProductService.addProduct(product);
        return ProductMapper.toDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return IProductService.findById(id)
                .map(existing -> {
                    Product product = ProductMapper.toEntity(productDTO);
                    product.setId(id);
                    Product updated = IProductService.updateProduct(product);
                    return ResponseEntity.ok(ProductMapper.toDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (IProductService.findById(id).isPresent()) {
            IProductService.deleteProduct(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(@RequestParam String keyword) {
        return IProductService.searchByName(keyword);
    }
}
