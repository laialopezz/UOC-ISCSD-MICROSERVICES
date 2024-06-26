package edu.uoc.epcsd.productcatalog.controllers;


import edu.uoc.epcsd.productcatalog.controllers.dtos.CreateProductRequest;
import edu.uoc.epcsd.productcatalog.controllers.dtos.GetProductResponse;
import edu.uoc.epcsd.productcatalog.entities.Category;
import edu.uoc.epcsd.productcatalog.entities.Item;
import edu.uoc.epcsd.productcatalog.entities.Product;
import edu.uoc.epcsd.productcatalog.services.CategoryService;
import edu.uoc.epcsd.productcatalog.services.ItemService;
import edu.uoc.epcsd.productcatalog.services.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ItemService itemService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getAllProducts() {
        log.trace("getAllProducts");

        return productService.findAll();
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable @NotNull Long productId) {
        log.trace("getProductById");

        return productService.findById(productId).map(product -> ResponseEntity.ok().body(GetProductResponse.fromDomain(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Long> createProduct(@RequestBody CreateProductRequest createProductRequest) {
        log.trace("createProduct");

        log.trace("Creating product " + createProductRequest);
        Long productId = productService.createProduct(
                createProductRequest.getCategoryId(),
                createProductRequest.getName(),
                createProductRequest.getDescription(),
                createProductRequest.getDailyPrice(),
                createProductRequest.getBrand(),
                createProductRequest.getModel()).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productId)
                .toUri();

        return ResponseEntity.created(uri).body(productId);
    }

    @DeleteMapping("/{id}")
    public List<Item> setOperational(@PathVariable @NotNull Long id){
        log.trace("setOperational");
        List<Item> updatedItems = List.of();
        Optional<Product> product = productService.findById(id);
        if(product.isPresent()){
            product.get().getItemList().forEach(item -> {
                Item updatedItem = itemService.setOperational(item.getSerialNumber(), false);
                updatedItems.add(updatedItem);
            });
        }
        return updatedItems;
    }


    @GetMapping("/getByCategoryId/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProductsByCategoryId(@PathVariable @NotNull Long id) {
        log.trace("getProductsByCategoryId");
        List<Product> categories = List.of();
        productService.findAll().forEach(product -> {
            if (product.getCategory().getId().equals((id))) {
                categories.add(product);
            } else {
                product.getCategory().getChildren().forEach(subcategory -> {
                    if (subcategory.getId().equals((id))) categories.add(product);
                });
            }
        });

        return categories;
    }

    @GetMapping("/getByName/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProductsByName(@PathVariable @NotNull String name) {
        log.trace("getProductsByName");
        Stream<Product> categories = productService.findAll().stream().filter((product -> product.getName().equals(name)));
        return categories.collect(Collectors.toList());
    }
}
