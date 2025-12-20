package p3project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import p3project.classes.Contract;
import p3project.functions.ContractFunctions;
import p3project.functions.UserFunctions;

@Controller
public class ContractController{

    @Autowired
    private UserFunctions uF;

    @Autowired
    private ContractFunctions cF;

    // Deletes a contract by ID
    @PostMapping("/sponsors/deleteContract")
    public String deleteContractMapping(@RequestParam Long contractId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return cF.deleteContract(contractId, request, redirectAttributes);
    }

    // Handles creating a new contract for a sponsor
    @PostMapping("/sponsors/addContract")
    public String addContractForSponsorWeb(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return cF.addContractForSponsor(contract, pdffile, request, redirectAttributes);
    }

    @PostMapping("/update/contract")
    public String updateContractFieldsWeb(@ModelAttribute Contract contract, @RequestParam MultipartFile pdffile, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if(!uF.userHasValidToken(request)) return "redirect:/login";
        if(!uF.userIsAdmin(request))       return "redirect:/homepage";

        return cF.updateContractFields(contract, pdffile, request, redirectAttributes);
    }

    @GetMapping("/getFile/{contractId}")
    public ResponseEntity<byte[]> getFileMapping(@PathVariable long contractId) {
        return cF.getFile(contractId);
    }
}