package com.rangkoto.rest_api.modules.book.controller;


import com.rangkoto.rest_api.modules.book.model.Book;
import com.rangkoto.rest_api.modules.book.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        return service.save(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book b) {
        return service.getById(id).map(book -> {
            book.setTitle(b.getTitle());
            book.setAuthor(b.getAuthor());
            book.setPages(b.getPages());
            return ResponseEntity.ok(service.save(book));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.getById(id).isPresent()) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
