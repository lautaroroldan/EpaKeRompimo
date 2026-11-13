package com.lautaroroldan.epakerompimo.controller;

import com.lautaroroldan.epakerompimo.dto.enterprise.EnterpriseResponse;
import com.lautaroroldan.epakerompimo.service.EnterpriseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Ruta pública (ver SecurityConfig): el buscador se puede usar sin cuenta.
@RestController
@RequestMapping("/api/enterprises")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    // Buscador: desarrolladoras por nombre
    @GetMapping("/search")
    public List<EnterpriseResponse> search(@RequestParam String name) {
        return enterpriseService.search(name);
    }
}
