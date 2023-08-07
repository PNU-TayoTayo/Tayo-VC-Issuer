package pnu.cse.TayoTayo.VC_Service.controller;


import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.web.bind.annotation.*;
import pnu.cse.TayoTayo.VC_Service.dto.request.VCRequest;
import pnu.cse.TayoTayo.VC_Service.service.VCService;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/vc_service")
@RequiredArgsConstructor
public class VCController {

    private final VCService vcService;

    @GetMapping("/offer")
    public String offer() throws IndyException, ExecutionException, InterruptedException {


        String credentialOffer = vcService.createCredentialOffer();

        return credentialOffer;
    }

    @PostMapping("/getVC")
    public String getVC(@RequestBody VCRequest request) throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\nBE에서 요청온 멤버이름 : "+request.getMemberName());
        System.out.println("BE에서 요청온 차 번호 : "+request.getCarNumber());
        System.out.println("BE에서 요청온 offer : " + request.getCredentialOffer());
        System.out.println("BE에서 요청온 Json : "+request.getCredentialRequestJson());


        return vcService.createVC(request.getCredentialOffer(), request.getCredentialRequestJson(),request.getMemberName(),request.getCarNumber());

    }







}
