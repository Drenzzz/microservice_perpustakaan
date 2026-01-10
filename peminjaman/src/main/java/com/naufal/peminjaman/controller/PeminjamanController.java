package com.naufal.peminjaman.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.naufal.peminjaman.dto.CommandResult;
import com.naufal.peminjaman.model.PeminjamanModel;
import com.naufal.peminjaman.service.PeminjamanService;
import com.naufal.peminjaman.vo.ResponseTemplate;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Standard REST Controller (Refactored from CQRS)
 */
@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {

    private static final Logger log = LoggerFactory.getLogger(PeminjamanController.class);

    @Autowired
    private PeminjamanService peminjamanService;

    // ==================== READ OPERATIONS ====================

    @GetMapping
    public List<PeminjamanModel> getAllPeminjaman() {
        log.info("Request received", kv("action", "GET_ALL"));
        List<PeminjamanModel> result = peminjamanService.getAllPeminjaman();
        log.info("Request completed", kv("action", "GET_ALL"), kv("status", "SUCCESS"), kv("count", result.size()));
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeminjamanModel> getPeminjamanById(@PathVariable Long id) {
        log.info("Request received", kv("action", "GET_BY_ID"), kv("id", id));
        PeminjamanModel peminjaman = peminjamanService.getPeminjamanById(id);
        if (peminjaman != null) {
            log.info("Request completed", kv("action", "GET_BY_ID"), kv("status", "SUCCESS"), kv("id", id));
            return ResponseEntity.ok(peminjaman);
        } else {
            log.warn("Request completed", kv("action", "GET_BY_ID"), kv("status", "NOT_FOUND"), kv("id", id));
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/anggota/{id}")
    public List<ResponseTemplate> getPeminjamanWithAnggotaById(@PathVariable Long id) {
        log.info("Request received", kv("action", "GET_WITH_DETAILS"), kv("peminjamanId", id));
        List<ResponseTemplate> result = peminjamanService.getPeminjamanWithAnggotaById(id);
        log.info("Request completed", kv("action", "GET_WITH_DETAILS"), kv("status", "SUCCESS"),
                kv("count", result.size()));
        return result;
    }

    @GetMapping(path = "/by-anggota/{anggotaId}")
    public ResponseEntity<List<PeminjamanModel>> getPeminjamanByAnggotaId(@PathVariable Long anggotaId) {
        log.info("Request received", kv("action", "GET_BY_ANGGOTA_ID"), kv("anggotaId", anggotaId));
        List<PeminjamanModel> peminjamanList = peminjamanService.getPeminjamanByAnggotaId(anggotaId);
        if (peminjamanList.isEmpty()) {
            log.warn("Request completed", kv("action", "GET_BY_ANGGOTA_ID"), kv("status", "NOT_FOUND"),
                    kv("anggotaId", anggotaId));
            return ResponseEntity.notFound().build();
        }
        log.info("Request completed", kv("action", "GET_BY_ANGGOTA_ID"), kv("status", "SUCCESS"),
                kv("count", peminjamanList.size()));
        return ResponseEntity.ok(peminjamanList);
    }

    // ==================== WRITE OPERATIONS ====================

    @PostMapping
    public ResponseEntity<CommandResult> createPeminjaman(@RequestBody PeminjamanModel peminjaman) {
        log.info("Request received", kv("action", "CREATE"),
                kv("anggotaId", peminjaman.getAnggotaId()), kv("bukuId", peminjaman.getBukuId()));
        
        try {
            PeminjamanModel result = peminjamanService.createPeminjaman(peminjaman);
            CommandResult commandResult = new CommandResult(result.getId(), true, "Peminjaman created successfully");
            
            log.info("Request completed", kv("action", "CREATE"), kv("status", "SUCCESS"), kv("id", result.getId()));
            return ResponseEntity.status(201).body(commandResult);
        } catch (Exception e) {
            log.error("Request failed", kv("action", "CREATE"), kv("status", "FAILED"), kv("error", e.getMessage()));
            return ResponseEntity.badRequest().body(new CommandResult(null, false, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandResult> updatePeminjaman(@PathVariable Long id,
            @RequestBody PeminjamanModel peminjaman) {
        log.info("Request received", kv("action", "UPDATE"), kv("id", id));
        
        PeminjamanModel result = peminjamanService.updatePeminjaman(id, peminjaman);
        if (result != null) {
            log.info("Request completed", kv("action", "UPDATE"), kv("status", "SUCCESS"), kv("id", id));
            return ResponseEntity.ok(new CommandResult(id, true, "Peminjaman updated successfully"));
        }
        
        log.warn("Request completed", kv("action", "UPDATE"), kv("status", "NOT_FOUND"), kv("id", id));
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommandResult> deletePeminjaman(@PathVariable Long id) {
        log.info("Request received", kv("action", "DELETE"), kv("id", id));
        peminjamanService.deletePeminjaman(id);
        
        // Assuming delete is always successful if no exception
        log.info("Request completed", kv("action", "DELETE"), kv("status", "SUCCESS"), kv("id", id));
        return ResponseEntity.ok(new CommandResult(id, true, "Peminjaman deleted successfully"));
    }
}
