package com.rangkoto.rest_api.modules.book.service;


import com.rangkoto.rest_api.modules.book.model.Book;
import com.rangkoto.rest_api.modules.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final BookRepository repo;

    public BookService(BookRepository repo) {
        this.repo = repo;
    }

    public List<Book> getAll() {
        return repo.findAll();
    }

    public Optional<Book> getById(Long id) {
        return repo.findById(id);
    }

    public Book save(Book book) {
        return repo.save(book);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
