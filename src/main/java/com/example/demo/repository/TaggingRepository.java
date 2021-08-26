package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Tagging;

public interface TaggingRepository extends JpaRepository<Tagging,Long>{
}
