package com.rangkoto.rest_api.modules.book.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "books")
public class Book {
    //    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    private String title;
    private String author;
    private int pages;
}
