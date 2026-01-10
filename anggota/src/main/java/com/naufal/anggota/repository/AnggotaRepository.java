package com.naufal.anggota.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.naufal.anggota.model.AnggotaModel;
import org.springframework.stereotype.Repository;

@Repository
public interface AnggotaRepository extends JpaRepository<AnggotaModel, Long> {

}
