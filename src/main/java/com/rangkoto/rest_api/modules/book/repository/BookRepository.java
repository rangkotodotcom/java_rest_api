package com.rangkoto.rest_api.modules.book.repository;

import com.rangkoto.rest_api.modules.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
