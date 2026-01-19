package com.naufal.anggota.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naufal.anggota.model.AnggotaModel;
import com.naufal.anggota.repository.AnggotaRepository;

@Service
public class AnggotaService {
    @Autowired

    private AnggotaRepository anggotaRepository;

    public List<AnggotaModel> getAllAnggota() {
        return anggotaRepository.findAll();
    }

    public AnggotaModel getAnggotaById(Long id) {

        return anggotaRepository.findById(id).orElse(null);
    }

    public AnggotaModel createAnggota(AnggotaModel anggota) {
        System.out.println("berhasil entri anggota");
        return anggotaRepository.save(anggota);
    }

    public void deleteAnggota(Long id) {
        anggotaRepository.deleteById(id);
    }

    public AnggotaModel updateAnggota(Long id, AnggotaModel anggotaUpdate) {
        return anggotaRepository.findById(id)
                .map(existingAnggota -> {
                    if (anggotaUpdate.getNim() != null) {
                        existingAnggota.setNim(anggotaUpdate.getNim());
                    }
                    if (anggotaUpdate.getNama() != null) {
                        existingAnggota.setNama(anggotaUpdate.getNama());
                    }
                    if (anggotaUpdate.getAlamat() != null) {
                        existingAnggota.setAlamat(anggotaUpdate.getAlamat());
                    }
                    if (anggotaUpdate.getEmail() != null) {
                        existingAnggota.setEmail(anggotaUpdate.getEmail());
                    }
                    if (anggotaUpdate.getJenis_kelamin() != null) {
                        existingAnggota.setJenis_kelamin(anggotaUpdate.getJenis_kelamin());
                    }
                    return anggotaRepository.save(existingAnggota);
                })
                .orElse(null);
    }
}
