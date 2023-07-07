package pnu.cse.TayoTayo.VC_Service.service;


import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnu.cse.TayoTayo.VC_Service.util.PoolAndWalletManager;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class VCService {

    private final PoolAndWalletManager poolAndWalletManager;

    @Transactional
    public String createCredentialOffer() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n\n Issuer가 definition에 대한 credential offer를 생성한다");
        String transcriptCredOffer = Anoncreds.issuerCreateCredentialOffer(poolAndWalletManager.getIssuerWallet(),
                poolAndWalletManager.getIssuer().get("transcript_cred_def_id").toString()).get();

        //Issuer.put("transcript_cred_offer",transcriptCredOffer);

        System.out.println("생성한 Issuer의 Credential Offer: " + transcriptCredOffer);

        return transcriptCredOffer;


    }





}
