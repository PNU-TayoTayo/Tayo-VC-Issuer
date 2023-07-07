package pnu.cse.TayoTayo.VC_Service.controller;


import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.VC_Service.service.VCService;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/vc_service")
@RequiredArgsConstructor
public class VCController {

    private final VCService vcService;

    @GetMapping("/offer")
    public String offer() throws IndyException, ExecutionException, InterruptedException {

        // TODO : Private API룰 구축하자!!

        String credentialOffer = vcService.createCredentialOffer();

        return credentialOffer;
    }






}
