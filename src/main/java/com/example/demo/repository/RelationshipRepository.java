package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Relationship;

public interface RelationshipRepository extends JpaRepository<Relationship, Long>{

}
