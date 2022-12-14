package com.example.digitkraftbackend.service;

import com.example.digitkraftbackend.dto.AddProductDTO;
import com.example.digitkraftbackend.dto.CategoryDTO;
import com.example.digitkraftbackend.dto.ProductDTO;
import com.example.digitkraftbackend.dto.SearchBodyDTO;
import com.example.digitkraftbackend.exceptions.CategoryNotFoundException;
import com.example.digitkraftbackend.mapper.ProductMapper;
import com.example.digitkraftbackend.model.Category;
import com.example.digitkraftbackend.model.Product;
import com.example.digitkraftbackend.model.ProductImage;
import com.example.digitkraftbackend.repository.CategoryRepository;
import com.example.digitkraftbackend.repository.ProductImageRepository;
import com.example.digitkraftbackend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

    private final String root = "uploads/products/";
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    public void saveProduct(AddProductDTO addProductDTO) throws CategoryNotFoundException, IOException {

        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(addProductDTO.getName());
        productDTO.setProductImages(addProductDTO.getProductImages());
        productDTO.setPrice(addProductDTO.getPrice());
        productDTO.setCopies(addProductDTO.getCopies());
        productDTO.setDescription(addProductDTO.getDescription());
        productDTO.setCopies(addProductDTO.getCopies());
        var selectedCategory = new CategoryDTO();
        selectedCategory.setName(addProductDTO.getCategory());
        productDTO.setCategory(selectedCategory);

        Product product = productMapper.productDTOToProduct(productDTO);
        Path path = Paths.get(root + productDTO.getName() + "/");
        Category category = categoryRepository.findByNameIgnoreCase(productDTO.getCategory().getName())
                .orElseThrow(() -> new CategoryNotFoundException("Could not find category with name: " + productDTO.getCategory().getName()));
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        String[] strings = addProductDTO.getBase64Content().split(",");

        byte[] data = DatatypeConverter.parseBase64Binary(strings[1]);

        File file = new File(path.resolve(addProductDTO.getFileName()).toUri());
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setName(addProductDTO.getFileName());
        productImage.setPath(root + productDTO.getName() + "/" + addProductDTO.getFileName());
        Set<ProductImage> productImageSet = new HashSet<>();
        productImageSet.add(productImage);
        product.setProductImages(productImageSet);
        product.setCategory(category);
        productRepository.save(product);
        productImageRepository.save(productImage);
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> productList = productRepository.findAllByOrderByName();
        log.info("Successfully find all products from database");
        return productList.stream().map(productMapper::productToProductDTO).toList();
    }

    public List<ProductDTO> searchAllProducts(SearchBodyDTO searchBodyDTO) {
        var lowerCaseName = searchBodyDTO.getName() == null ? null : searchBodyDTO.getName().toLowerCase();
        List<Product> productList = productRepository.searchProducts(lowerCaseName, searchBodyDTO.getMinPrice(), searchBodyDTO.getMaxPrice(), searchBodyDTO.getCategory());
        log.info("Successfully find all products with search body: {}", searchBodyDTO);
        return productList.stream().map(productMapper::productToProductDTO).toList();
    }

    public byte[] getProductImage(String path) {
        if (!path.matches(".{1,}\\..{1,}")) {
            return null;
        }

        try (Stream<Path> stream = Files.walk(Paths.get(path))) {
            var paths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path item: paths) {
                if (path.endsWith(path)) {
                    return Files.readAllBytes(item);
                }
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }
}
