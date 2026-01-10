package com.naufal.buku.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.naufal.buku.model.BukuModel;
import org.springframework.stereotype.Repository;

@Repository
public interface BukuRepository extends JpaRepository<BukuModel, Long> {

}
