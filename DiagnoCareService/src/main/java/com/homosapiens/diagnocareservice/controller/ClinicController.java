//package com.homosapiens.diagnocareservice.controller;
//
//import com.homosapiens.diagnocareservice.model.entity.Clinic;
//import com.homosapiens.diagnocareservice.service.ClinicService;
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.function.EntityResponse;
//
//import java.util.List;
//
//@RestController
//@AllArgsConstructor
//@RequestMapping("/clinics")
//public class ClinicController {
//    ClinicService clinicService;
//
//    @GetMapping
//    public ResponseEntity<List<Clinic>> findAll() {
//        List<Clinic> clinics = clinicService.findAllClinic();
//        return ResponseEntity.ok(clinics);
//    }
//
//
//}
