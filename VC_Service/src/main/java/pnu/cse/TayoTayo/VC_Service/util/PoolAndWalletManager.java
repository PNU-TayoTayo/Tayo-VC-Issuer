package pnu.cse.TayoTayo.VC_Service.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component
@Getter
public class PoolAndWalletManager {

    static Logger log = LoggerFactory.getLogger(PoolAndWalletManager.class);
    private final Pool pool;
    private final Wallet stewardWallet;
    private final Wallet governmentWallet;
    private final Wallet IssuerWallet;

    private Map<String, Object> steward = new HashMap<>();
    private Map<String, Object> government = new HashMap<>();
    private Map<String, Object> Issuer = new HashMap<>();

    public PoolAndWalletManager() throws IndyException, ExecutionException, InterruptedException {
        this.pool = createPool();
        this.stewardWallet = createStewardWallet();
        this.governmentWallet = createGovermentWallet();
        this.IssuerWallet = createIssuerWallet();
    }



    private Pool createPool() throws IndyException, ExecutionException, InterruptedException {
        String poolName = "TayoTayoPool";
        String poolConfig = "{\"genesis_txn\": \"src/main/java/pnu/cse/TayoTayo/VC_Service/indy/pool1.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool pool = null;
        try {
            pool = Pool.openPoolLedger(poolName, "{}").get();
        }catch(Exception e){
            Pool.createPoolLedgerConfig(poolName, poolConfig).get();
            pool = Pool.openPoolLedger(poolName, "{}").get();
        }
        System.out.println("Pool created and opened successfully.");

        return pool;
    }

    private Wallet createStewardWallet() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n===Steward의 지갑 생성 시작===");

        // steward 관련 속성
        steward.put("name", "Sovrin Steward");
        steward.put("wallet_config",
                new JSONObject().put("id", "souvrin_steward_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/VC_Service/wallet")).toString());
        steward.put("wallet_credentials", new JSONObject().put("key", "steward_wallet_key").toString());
        steward.put("seed","000000000000000000000000Steward1");

        // 여기서 지갑 있는지 없는 지 체크 해야겠는데??
        Wallet stWallet = null;

        try {
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        }catch(Exception e){
            Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
            stWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        }


        DidResults.CreateAndStoreMyDidResult stewardDid = Did.createAndStoreMyDid(stWallet, new JSONObject().put("seed", steward.get("seed")).toString()).get();
        steward.put("did",stewardDid.getDid());
        steward.put("key",stewardDid.getVerkey());

        System.out.println("\n\n===Steward의 지갑 생성 완료===");
        return stWallet;
    }

    private Wallet createGovermentWallet() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n===Goverment의 지갑 생성 시작===");

        // Government 정보 설정
        government.put("wallet_config",
                new JSONObject().put("id", "government_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/VC_Service/wallet")).toString());
        government.put("wallet_credentials", new JSONObject().put("key", "government_wallet_key"));

        Wallet govWallet = null;

        try {
            govWallet = Wallet.openWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
        }catch(Exception e){
            Wallet.createWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
            govWallet = Wallet.openWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
        }

        if(govWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(govWallet, "{}").get();
                government.put("did",didResult.getDid());
                government.put("key",didResult.getVerkey());

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), government.get("did").toString(), government.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet,steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("\n\n===Goverment의 지갑 생성 완료===");

        return govWallet;
    }

    private Wallet createIssuerWallet() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n===Issuer의 지갑 생성 시작===");

        // TODO : 여기에 Issuer 이름 정해서 넣기
        Issuer.put("wallet_config",
                new JSONObject().put("id", "Issuer_wallet")
                        .put("storage_config", new JSONObject()
                                .put("path", "src/main/java/pnu/cse/TayoTayo/VC_Service/wallet")).toString());
        Issuer.put("wallet_credentials", new JSONObject().put("key", "Issuer_wallet_key").toString());

        Wallet IssWallet = null;

        try {
            IssWallet = Wallet.openWallet(Issuer.get("wallet_config").toString(), Issuer.get("wallet_credentials").toString()).get();
        }catch(Exception e){
            Wallet.createWallet(Issuer.get("wallet_config").toString(), Issuer.get("wallet_credentials").toString()).get();
            IssWallet = Wallet.openWallet(Issuer.get("wallet_config").toString(), Issuer.get("wallet_credentials").toString()).get();
        }

        if(IssWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(IssWallet, "{}").get();
                Issuer.put("did",didResult.getDid());
                Issuer.put("key",didResult.getVerkey());

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), Issuer.get("did").toString(), Issuer.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet,(String)steward.get("did"), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("\n\n===Issuer의 지갑 생성 완료===");

        return IssWallet;
    }

    /**
     * 정부의 지갑으로 Schema 등록을 하는데 해당 정부의 지갑이
     * VC_Service에 있어야할까 ? 아니면 Tayo-BE에 있어야 할까? 흠...
     * 이건 1회만 하면 됨!! (버전이 변경 되지 않는다면..?!)
     *
     * Schema 정의
     *      차모델, 출고 일자, 가장 최근 검사 결과(자동차 종합/정기 검사), 주행 거리
     *      차주의 이름, 차량 번호, 차 모델, 출고 일자, 검사 이력, 주행 거리 일단 여기까지..
     *
     *      정부의 DID를 사용해서 Schema 생성! (Schema에도 DID가 있음!!)
     *
     */

    private void createCredentialSchema() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n\n <Government가 Transcript를 위한 Schema를 등록하는 과정>");
        //1. Government가 Transcript Schema를 생성

        Map<String, Object> tayotayoSchema = new HashMap<>();
        tayotayoSchema.put("name", "TayoTayo_Service_Car_Transcript");
        tayotayoSchema.put("version", "1.2");
        tayotayoSchema.put("attributes",
                new JSONArray(Arrays.asList("owner_first_name","owner_last_name", "car_number"
                        ,"car_model","car_delivery_date", "inspection_record", "driving_record")).toString());
                // 차주의 이름, 차량 번호, 차 모델, 출고 일자, 검사 이력, 주행 거리 일단 여기까지..

        // 정부의 did로 issuerCreateSchema 메소드로 스키마 생성!
        AnoncredsResults.IssuerCreateSchemaResult schemaResult = Anoncreds.issuerCreateSchema(
                government.get("did").toString(),
                tayotayoSchema.get("name").toString(),
                tayotayoSchema.get("version").toString(),
                tayotayoSchema.get("attributes").toString()
        ).get();

        government.put("transcript_schema_id", schemaResult.getSchemaId());
        government.put("transcript_schema", schemaResult.getSchemaJson());
        System.out.println("Issuer가 사용할 Schema의 ID : " + schemaResult.getSchemaId());

        // 2. Government가 생성한 Transcript Schema를 레저에 등록
        System.out.println("정부가 정의해둔 Schema : " + government.get("transcript_schema").toString());

        //Ledger.buildSchemaRequest 함수를 사용하여 위에서 생성한 스키마를 ledger에 등록하기 위한 Schema Request 생성
        // 여기서 왜 did를 등록ㅎ는거지 ? Schema의 ID는 필요 없는건가..?
        String schemaRequest = Ledger.buildSchemaRequest(government.get("did").toString(), government.get("transcript_schema").toString()).get();
        System.out.println(schemaRequest);

        // Ledger.signAndSubmitRequest 함수를 사용하여 스키마 등록요청을 서명하고 ledger에 제출
        String res = Ledger.signAndSubmitRequest(pool, governmentWallet, government.get("did").toString(),schemaRequest).get();
        System.out.println(res);
    }

    /**
     *  VC 발급 서비스가 Government가 정의 해둔 스키마로 Credential Definition을 만듬!
     */
    private void createCredentialDefinition() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n\n\n VC-Servic(Issuer) creates Transcript Credential Definition");

        // Issuer가 레저에 등록되어 있는 Schema를 가지고 옴 (아마도 이게 SchemaID를 가지고 오는것!)
        String getSchemaRequest = Ledger.buildGetSchemaRequest(Issuer.get("did").toString(), government.get("transcript_schema_id").toString()).get();
        System.out.println("\n\n레저로 부터 져온 SchemaRequest : " + getSchemaRequest);

        // TODO : 얘의 정확한 용도가 뭘까?
        String getSchemaResponse = ensurePreviousRequestApplied(pool, getSchemaRequest, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        System.out.println("\n\nSchemaResponse : "+getSchemaResponse);

        //  Indy 분산원장으로부터 받은 스키마 응답(스키마 조회 요청에 대한 응답)을 파싱
        LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
        Issuer.put("transcript_schema_id", parseSchemaResult.getId());
        Issuer.put("transcript_schema",parseSchemaResult.getObjectJson());

        System.out.println("\n\n[레저로부터 가져온 Schema 확인]");
        System.out.println("SchemaId : " + parseSchemaResult.getId());
        System.out.println("SchemaJson : "+parseSchemaResult.getObjectJson());

        // 레저로부터 가져온 Schema를 기반으로 Credential Definition 생성 요청
        System.out.println("\n\nIssuer가 Schema에 대한 Credential Definition를 만든다");
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult =
                Anoncreds.issuerCreateAndStoreCredentialDef(IssuerWallet, Issuer.get("did").toString(), Issuer.get("transcript_schema").toString(),
                        "TAG1", null, new JSONObject().put("support_revocation", false).toString()).get();
        Issuer.put("transcript_cred_def_id",createCredDefResult.getCredDefId());
        Issuer.put("transcript_cred_def",createCredDefResult.getCredDefJson());

        // 생성한 Crednetial Definition을 Request로 만들어서 서명하고 제출해서 레저에 등록!!
        System.out.println("\n\nIssuer가 생성한 Credential Definition을 레저에 등록 ");
        String credDefRequest = Ledger.buildCredDefRequest(Issuer.get("did").toString(),Issuer.get("transcript_cred_def").toString()).get();
        Ledger.signAndSubmitRequest(pool, IssuerWallet, Issuer.get("did").toString(), credDefRequest);

        System.out.println("\n\n[Issuer가 생성하고 레저에 등록한 Credential Definition 확인]");
        System.out.println("Id : " + createCredDefResult.getCredDefId());
        System.out.println("스키마 형태 : "+createCredDefResult.getCredDefJson());

    }

    /**
     * Issuer가 여기서 Credential Offer를 생성 (??)
     * 이걸 따로 레저에 등록하는 과정은 없는듯??
     *
     *
     */

    private void createCredentialOffer() throws IndyException, ExecutionException, InterruptedException {


        /**
         * 타요타요 서버가 Offer를 받아서 해당 Offer의 Schema_id랑 cred_def_id를 따로 가지고 있어야하는거아닌가?
         *
         *  타요타요 서버가 이걸 알고 있음
         *         타요타요 서버에서 유저가 Offer를 요청하면 Controller를 통해서 Offer를 생성해서 가져옴!
         *
         *         유저는 본인의 MasterSecret을 만든다! DB에 저장?? 흠..
         *
         *         유저는 서버에 있는 Schema_id랑 cre_def_id를 레저로부터 가져온다!
         *
         *         유저는 credentialRequestResult를 만든다
         *
         *         그리고 VC 발급해달라는 API를 호출한다!!!
         *
         *
         */

//        System.out.println("\n theUniversity가 생성한 Credential Offer를 서버에게 보낸다");
//        Alice.put("transcript_cred_offer",transcriptCredOffer);
//
//        System.out.println("\n 엘리스는 받은 Credential offer로 부터 schemaID랑 credential definition ID를 획득");
//        Alice.put("transcript_schema_id",new JSONObject(transcriptCredOffer).getString("schema_id"));
//        Alice.put("transcript_cred_def_id",new JSONObject(transcriptCredOffer).getString("cred_def_id"));

//        System.out.println("\n엘리스는 VC 생성에 필요한 Master Secret을 생성하고 엘리스 지갑에 저장한다");
//        String AliceMasterSecretId = Anoncreds.proverCreateMasterSecret(AliceWallet, null).get();
//        Alice.put("master_secret_id",AliceMasterSecretId);

//        System.out.println("\n 엘리스는 알고 있는 credential definition ID로 레저로부터 Credential Definition를 가져온다");
//        LedgerResults.ParseResponseResult parsedCredDefResponse = getCredDef(pool, Alice.get("did").toString(), Alice.get("transcript_cred_def_id").toString());
//        Alice.put("theUniversity_transcript_cred_def",parsedCredDefResponse.getObjectJson());


//        // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
//        System.out.println("\n엘리스는 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
//        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
//                Anoncreds.proverCreateCredentialReq(AliceWallet, Alice.get("did").toString(), Alice.get("transcript_cred_offer").toString(),
//                        Alice.get("theUniversity_transcript_cred_def").toString(), Alice.get("master_secret_id").toString()).get();
//
//        Alice.put("transcript_cred_request",credentialRequestResult.getCredentialRequestJson());
//        Alice.put("transcript_cred_request_metadata",credentialRequestResult.getCredentialRequestMetadataJson());
//
//        System.out.println("\n엘리스가 Issuer에게 Credential Request를 제출");
//        university.put("transcript_cred_request",Alice.get("transcript_cred_request"));
//
//        // 대학교에서 엘리스에게 credential을 발급해준다 (대학교가 알고 있는 엘리스의 개인정보들) - 즉, 이게 VC 인거지
//        System.out.println("\n이제 대학교가 엘리스를 위한 Credential을 생성한다"); // 이건 아마도 Issuer의 DB를 하나 파야할듯?

    }





        @PostConstruct
    public void init() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n steward의 DID : "+Did.getListMyDidsWithMeta(stewardWallet).get());
        System.out.println("\n government의 DID : "+Did.getListMyDidsWithMeta(governmentWallet).get());
        System.out.println("\n Issuer의 DID : "+Did.getListMyDidsWithMeta(IssuerWallet).get());

        //TODO : 앞에서 지갑 생성하고 Schema 등록이나 Credential Definition 등록 같은건 여기서 하기
        //      그리고 VC 발급해줄 수 있는 API 하나 만들기 !!! (private API 사용)
        //      흠.. offer는 어떻게 하지?? API??

        // government의 schema 등록!
        createCredentialSchema();
        // Issuer의 Credential Definition 등록!
        createCredentialDefinition();


    }

    @PreDestroy
    public void cleanup() throws Exception {
        // 여기서 Pool 삭제하고 Government랑 Issuer 지갑 삭제
        closeAndDeleteWallet(IssuerWallet,Issuer.get("wallet_config").toString(),Issuer.get("wallet_credentials").toString());
        closeAndDeleteWallet(governmentWallet,government.get("wallet_config").toString(),government.get("wallet_credentials").toString());
        closeAndDeleteWallet(stewardWallet,steward.get("wallet_config").toString(),steward.get("wallet_credentials").toString());

        pool.closePoolLedger().get();
        Pool.deletePoolLedgerConfig("TayoTayoPool").get();

    }

    public static void closeAndDeleteWallet(Wallet wallet, String config, String key) throws Exception {
        if (wallet != null) {
            wallet.closeWallet().get();
            Wallet.deleteWallet(config, key).get();
        }
    }
    private static String signAndSubmitRequest(Pool pool, Wallet endorserWallet, String endorserDid, String request) throws Exception {
        return submitRequest(pool, Ledger.signRequest(endorserWallet, endorserDid, request).get());
    }

    private static String submitRequest(Pool pool, String req) throws Exception {
        String res = Ledger.submitRequest(pool, req).get();
        if ("REPLY".equals(new JSONObject(res).get("op"))) {
            log.info("SubmitRequest: " + req);
            log.info("SubmitResponse: " + res);
        } else {
            log.warn("SubmitRequest: " + req);
            log.warn("SubmitResponse: " + res);
        }
        return res;
    }

    public interface PoolResponseChecker {
        boolean check(String response);
    }
    public static String ensurePreviousRequestApplied(Pool pool, String checkerRequest, PoolResponseChecker checker)
            throws IndyException, ExecutionException, InterruptedException {

        for (int i = 0; i < 3; i++) {
            String response = Ledger.submitRequest(pool, checkerRequest).get();
            try {
                if (checker.check(response)) {
                    return response;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.err.println(e.toString());
                System.err.println(response);
            }
            Thread.sleep(10000);
        }

        throw new IllegalStateException();
    }

}
