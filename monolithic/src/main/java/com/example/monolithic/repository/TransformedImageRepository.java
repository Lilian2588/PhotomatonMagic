package com.example.monolithic.repository;

import com.example.monolithic.model.TransformedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransformedImageRepository extends JpaRepository<TransformedImage, String> {
}