package edu.uoc.epcsd.productcatalog.controllers;

import edu.uoc.epcsd.productcatalog.controllers.dtos.CreateCategoryRequest;
import edu.uoc.epcsd.productcatalog.entities.Category;
import edu.uoc.epcsd.productcatalog.services.CategoryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getAllCategories() {
        log.trace("getAllCategories");

        return categoryService.findAll();
    }

    @PostMapping
    public ResponseEntity<Long> createCategory(@RequestBody CreateCategoryRequest createCategoryRequest) {
        log.trace("createCategory");

        log.trace("Creating category " + createCategoryRequest);
        Long categoryId = categoryService.createCategory(
                createCategoryRequest.getParentId(),
                createCategoryRequest.getName(),
                createCategoryRequest.getDescription()).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryId)
                .toUri();

        return ResponseEntity.created(uri).body(categoryId);
    }

    @GetMapping("/getByName/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getCategoriesByName(@PathVariable @NotNull String name) {
        log.trace("getCategoriesByName");
        Stream<Category> categories = categoryService.findAll().stream().filter((category -> category.getName().equals(name)));
        return categories.collect(Collectors.toList());
    }

    @GetMapping("/getByDescription/{description}")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getCategoriesByDescription(@PathVariable @NotNull String description) {
        log.trace("getCategoriesByDescription");
        Stream<Category> categories = categoryService.findAll().stream().filter((category -> category.getDescription().equals(description)));
        return categories.collect(Collectors.toList());
    }

    @GetMapping("/getByParentCategory/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getCategoriesByParentCategory(@PathVariable @NotNull Long id) {
        log.trace("getCategoriesByParentCategory");
        Stream<Category> categories = categoryService.findAll().stream().filter((category -> category.getParent().getId().equals(id)));
        return categories.collect(Collectors.toList());
    }
}
