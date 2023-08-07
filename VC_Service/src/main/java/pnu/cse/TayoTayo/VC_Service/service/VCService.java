package pnu.cse.TayoTayo.VC_Service.service;


import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.json.JSONObject;
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


    @Transactional
    public String createVC(String transcriptCredOffer, String transcriptCredRequest, String memberName, String carNumber) throws IndyException, ExecutionException, InterruptedException {

//        JSONObject credValuesJson = new JSONObject()
//                .put("first_name", new JSONObject().put("raw", "Alice").put("encoded", "1139481716457488690172217916278103335"))
//                .put("last_name", new JSONObject().put("raw", "Garcia").put("encoded", "5321642780241790123587902456789123452"))
//                .put("degree", new JSONObject().put("raw", "Bachelor of Science, Marketing").put("encoded", "010010010")) // 12434523576212321
//                .put("status", new JSONObject().put("raw", "graduated").put("encoded", "2213454313412354"))
//                .put("ssn", new JSONObject().put("raw", "123-45-6789").put("encoded", "3124141231422543541"))
//                .put("year", new JSONObject().put("raw", "2015").put("encoded", "2016"))
//                .put("average", new JSONObject().put("raw", "5").put("encoded", "5"));

        // TODO : 여기서 memberName이랑 carNumber 기준으로 DB에서 찾아야 할듯??

        JSONObject credValuesJson = new JSONObject()
                .put("owner_first_name", new JSONObject().put("raw", "Donwoo").put("encoded", "1139481716457488690172217916278103335"))
                .put("owner_last_name", new JSONObject().put("raw", "Kim").put("encoded", "5321642780241790123587902456789123452"))
                .put("car_number", new JSONObject().put("raw", carNumber ).put("encoded", "12434523576212321"))
                .put("car_model", new JSONObject().put("raw", "Mercedes-Benz G-Class").put("encoded", "2213454313412354"))
                .put("car_fuel", new JSONObject().put("raw", "Diesel").put("encoded", "616531351694"))
                .put("car_delivery_date", new JSONObject().put("raw", "20230101").put("encoded", "20230101"))
                .put("inspection_record", new JSONObject().put("raw", "20210101").put("encoded", "20210101"))
                .put("driving_record", new JSONObject().put("raw", "250").put("encoded", "250"));



        AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResult =
                Anoncreds.issuerCreateCredential(poolAndWalletManager.getIssuerWallet(),
                        transcriptCredOffer,
                        transcriptCredRequest,
                        credValuesJson.toString(),  //
                        null,0).get();

        System.out.println("생성한 유저의 VC : " + issuerCredentialResult.getCredentialJson());

        return issuerCredentialResult.getCredentialJson();

    }

//    public static String sha256(String msg)  throws NoSuchAlgorithmException {
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//        md.update(msg.getBytes());
//        return CryptoUtil.byteToHexString(md.digest());
//    }
}
