package pnu.cse.TayoTayo.VC_Service.indy;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.crypto.Crypto;
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

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hyperledger.indy.sdk.pool.Pool.openPoolLedger;

public class VCVerifierTest {

    static Logger log = LoggerFactory.getLogger(indytest.class);

    public static void setUp() throws Exception {

        System.out.println("\n\n\nSTEP 1 - Connect to Pool");
        Pool pool = createAndOpenPoolLedger();

        System.out.println("\n\n\nSTEP 2 - Configuring steward");
        Map<String, Object> steward = new HashMap<>();
        steward.put("name", "Sovrin Steward");
        steward.put("wallet_config", new JSONObject().put("id", "souvrin_steward_wallet").toString());
        steward.put("wallet_credentials", new JSONObject().put("key", "steward_wallet_key").toString());
        steward.put("seed", "000000000000000000000000Steward1");

        Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        Wallet stewardWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();

        if(stewardWallet != null){
            try {
                // Create Trustee DID
                String trusteeSeed = new JSONObject().put("seed", "000000000000000000000000Steward1").toString();

                DidResults.CreateAndStoreMyDidResult stewardDid = Did.createAndStoreMyDid(stewardWallet, trusteeSeed).get();
                steward.put("did",stewardDid.getDid());
                steward.put("key",stewardDid.getVerkey());

                // 결과 출력
                System.out.println("steward DID: " + steward.get("did"));
                System.out.println("steward Key: " + steward.get("key"));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }



        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for government");

        // Government 정보 설정
        Map<String, Object> government = new HashMap<>();
        government.put("name", "theGovernment");
        government.put("wallet_config", new JSONObject().put("id", "government_wallet").toString());
        government.put("wallet_credentials", new JSONObject().put("key", "government_wallet_key"));

        Wallet.createWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
        Wallet governmentWallet = Wallet.openWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();

        if(governmentWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(governmentWallet, "{}").get();
                government.put("did",didResult.getDid());
                government.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("government DID: " + government.get("did"));
                System.out.println("government Key: " + government.get("key"));

                // steward의 did를 사용하여 government의 did를 등록하고, 해당 did를 TRUST_ANCHOR 역할로 설정하는 작업을 수행
                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), government.get("did").toString(), government.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(governmentWallet).get());


        System.out.println("\n\n\nSTEP 3 - register DID for University");

        // University 정보 설정
        Map<String, Object> university = new HashMap<>();
        university.put("name", "University");
        university.put("wallet_config", new JSONObject().put("id", "theUniversity_wallet").toString());
        university.put("wallet_credentials", new JSONObject().put("key", "theUniversity_wallet_key").toString());

        Wallet.createWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();
        Wallet universityWallet = Wallet.openWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();

        if(universityWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(universityWallet, "{}").get();
                university.put("did",didResult.getDid());
                university.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("university DID: " + university.get("did"));
                System.out.println("university Key: " + university.get("key"));

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), university.get("did").toString(), university.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(universityWallet).get());


        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for Company");

        // Government 정보 설정
        Map<String, Object> company = new HashMap<>();
        company.put("name", "company");
        company.put("wallet_config", new JSONObject().put("id", "theCompany_wallet").toString());
        company.put("wallet_credentials", new JSONObject().put("key", "theCompany_wallet_key").toString());

        Wallet.createWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();
        Wallet companyWallet = Wallet.openWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();

        if(companyWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(companyWallet, "{}").get();
                company.put("did",didResult.getDid());
                company.put("key",didResult.getVerkey());

                System.out.println("company DID: " + company.get("did"));
                System.out.println("company Key: " + company.get("key"));

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), company.get("did").toString(), company.get("key").toString(),
                        null, "TRUST_ANCHOR").get();
                String res = signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest);
                System.out.println(res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(companyWallet).get());


        /*
           STEP 4 : 해당 코드는 정부가 credential schema(VC의 양식??)를 생성하고, 해당 schema를 ledger에 전송해서 등록!!
           스키마의 이름은 Transcript, 버전은 1.2 , 포함될 속성은 first_name, last_name 등등등... (나중에 우리가 정의해야할 부분)
         */

        System.out.println("\n\n\nSTEP 4 - Government creates credential schema");

        System.out.println("\n1. Government가 Transcript Schema를 생성");

        // 차대번호나 차량번호를 받아서
        // 차종류, 차량 번호, 출고일자, 운행 기록, 정기검사 유무, 검사이력? 정비이력?, 주행거리, 의무 보험 가입정보?? 사고정보?
        Map<String, Object> transcript = new HashMap<>();
        transcript.put("name", "TayoTayo Service Car Transcript");
        transcript.put("version", "1.2");
        transcript.put("attributes", new JSONArray(
                Arrays.asList("first_name","last_name","degree", "status", "year", "average", "ssn")).toString());

        // government의 did로 issuerCreateSchema 메소드로 스키마 생성!
        AnoncredsResults.IssuerCreateSchemaResult schemaResult = Anoncreds.issuerCreateSchema(
                government.get("did").toString(),
                transcript.get("name").toString(),
                transcript.get("version").toString(),
                transcript.get("attributes").toString()
        ).get();

        government.put("transcript_schema_id", schemaResult.getSchemaId());
        government.put("transcript_schema", schemaResult.getSchemaJson());
        String transcript_schema_id =government.get("transcript_schema_id").toString();

        System.out.println("\n2. Government가 Transcript Schema를 레저에 등록 ");
        System.out.println("정부가 정의해둔 Schema : " + government.get("transcript_schema").toString());

        //Ledger.buildSchemaRequest 함수를 사용하여 위에서 생성한 스키마를 ledger에 등록하기 위한 Schema Request 생성
        String schemaRequest = Ledger.buildSchemaRequest(government.get("did").toString(), government.get("transcript_schema").toString()).get();
        System.out.println(schemaRequest);

        // Ledger.signAndSubmitRequest 함수를 사용하여 스키마 등록요청을 서명하고 ledger에 제출
        //String res = signAndSubmitRequest(pool,governmentWallet,government.get("did").toString(),schemaRequest);
        String res = Ledger.signAndSubmitRequest(pool, governmentWallet, government.get("did").toString(),schemaRequest).get();
        System.out.println(res);


        // STEP - 5 : University will create a credential definition
        System.out.println("\n\n\nSTEP5 - University creates Transcript Credential Definition");

        System.out.println("\ntheUniversity(Issuer)가 레저에 등록되어 있는 Schema를 가지고 옴 (schemaId로) ");

        // 아마도 이게 SchemaID를 가지고 오는 사이클 인듯?
        String getSchemaRequest = Ledger.buildGetSchemaRequest(university.get("did").toString(), transcript_schema_id).get();
        String getSchemaResponse = ensurePreviousRequestApplied(pool, getSchemaRequest, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        System.out.println(getSchemaResponse);
        LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
        university.put("transcript_schema_id", parseSchemaResult.getId());
        university.put("transcript_schema",parseSchemaResult.getObjectJson());

        System.out.println("\n이게 Schema랑 똑같나??? "+parseSchemaResult.getObjectJson());

        // Credential Definition 생성 요청
        System.out.println("\ntheUniversity가 지갑안에 Schema에 대한 Credential Definition를 만든다");
        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(
                universityWallet, university.get("did").toString(), university.get("transcript_schema").toString(), "TAG1", null, new JSONObject().put("support_revocation", false).toString()).get();
        university.put("transcript_cred_def_id",createCredDefResult.getCredDefId());
        university.put("transcript_cred_def",createCredDefResult.getCredDefJson());

        // Request만들어서 서명하고 제출해서 등록!!
        System.out.println("\ntheUniversity가 생성한 Credential Definition을 레저에 등록한다 ");
        String credDefRequest = Ledger.buildCredDefRequest(university.get("did").toString(),university.get("transcript_cred_def").toString()).get();
        Ledger.signAndSubmitRequest(pool, universityWallet, university.get("did").toString(), credDefRequest);

        System.out.println("등록된 Credential Definition ID : "+university.get("transcript_cred_def_id"));
        System.out.println("등록된 Credential Definition Json : "+university.get("transcript_cred_def"));


        // STEP6 - University Issues Transcript Credential to Alice - 엘리스에게 VC 발급
        System.out.println("\n\n\nSTEP6 - University Issues Transcript Credential to Alice");

        /*
            우선 엘리스 setup(지갑생성등) 부터 하자
            우린 setup과정이 회원가입일 것임
         */

        // Alice 정보 설정
        Map<String, Object> Alice = new HashMap<>();
        Alice.put("name", "Alice");
        Alice.put("wallet_config", new JSONObject().put("id", "alice_wallet").toString());
        Alice.put("wallet_credentials", new JSONObject().put("key", "alice_wallet_key").toString());

        Wallet.createWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();
        Wallet AliceWallet = Wallet.openWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();

        if(AliceWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(AliceWallet, "{}").get();
                Alice.put("did",didResult.getDid());
                Alice.put("key",didResult.getVerkey());

                System.out.println("Alice DID: " + Alice.get("did"));
                System.out.println("Alice Key: " + Alice.get("key"));

                // TODO : 근데 왜 정부의 did로 하는거지?
//                String nymRequest = Ledger.buildNymRequest(government.get("did").toString(), Alice.get("did").toString(), Alice.get("key").toString(),
//                        null, "ENDORSER").get();
//                String resd = Ledger.submitRequest(pool, nymRequest).get();
//                System.out.println(resd);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(AliceWallet).get());


        System.out.println("\n Issuer가 definition에 대한 credential offer를 생성한다");
        String transcriptCredOffer = Anoncreds.issuerCreateCredentialOffer(universityWallet, university.get("transcript_cred_def_id").toString()).get();
        university.put("transcript_cred_offer",transcriptCredOffer);

        // offer와 관련된 내용은 이미 웹에서 제공하고 있을 듯..?
        System.out.println("\n theUniversity가 생성한 Credential Offer를 엘리스에게 보낸다");
        Alice.put("transcript_cred_offer",transcriptCredOffer);

        System.out.println("\n 엘리스는 받은 Credential offer로 부터 schemaID랑 credential definition ID를 획득");
        Alice.put("transcript_schema_id",new JSONObject(transcriptCredOffer).getString("schema_id"));
        Alice.put("transcript_cred_def_id",new JSONObject(transcriptCredOffer).getString("cred_def_id"));

        System.out.println("\n엘리스는 VC 생성에 필요한 Master Secret을 생성하고 엘리스 지갑에 저장한다");
        String AliceMasterSecretId = Anoncreds.proverCreateMasterSecret(AliceWallet, null).get();
        Alice.put("master_secret_id",AliceMasterSecretId);

        System.out.println("\n 엘리스는 알고 있는 credential definition ID로 레저로부터 Credential Definition를 가져온다");
        LedgerResults.ParseResponseResult parsedCredDefResponse = getCredDef(pool, Alice.get("did").toString(), Alice.get("transcript_cred_def_id").toString());
        Alice.put("theUniversity_transcript_cred_def",parsedCredDefResponse.getObjectJson());


        // 여기서 Credential request 매개변수로 AliceWallet, AliceDID, Credential Offer, Credential Definition, Master Secret ID
        System.out.println("\n엘리스는 VC생성을 위해 Issuer에게 보낼 transcript credential request를 준비한다");
        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
                Anoncreds.proverCreateCredentialReq(AliceWallet, Alice.get("did").toString(), Alice.get("transcript_cred_offer").toString(),
                        Alice.get("theUniversity_transcript_cred_def").toString(), Alice.get("master_secret_id").toString()).get();
        Alice.put("transcript_cred_request",credentialRequestResult.getCredentialRequestJson());
        Alice.put("transcript_cred_request_metadata",credentialRequestResult.getCredentialRequestMetadataJson());

        System.out.println("\n엘리스가 Issuer에게 Credential Request를 제출");
        university.put("transcript_cred_request",Alice.get("transcript_cred_request"));

        // 대학교에서 엘리스에게 credential을 발급해준다 (대학교가 알고 있는 엘리스의 개인정보들) - 즉, 이게 VC 인거지
        System.out.println("\n이제 대학교가 엘리스를 위한 Credential을 생성한다"); // 이건 아마도 Issuer의 DB를 하나 파야할듯?

        // TODO : 여기서 encoded는 무슨 값이 들어가야하지..?
        //      암호화 한 값인가..?
        JSONObject credValuesJson = new JSONObject()
                .put("first_name", new JSONObject().put("raw", "Alice").put("encoded", "1139481716457488690172217916278103335"))
                .put("last_name", new JSONObject().put("raw", "Garcia").put("encoded", "5321642780241790123587902456789123452"))
                .put("degree", new JSONObject().put("raw", "Bachelor of Science, Marketing").put("encoded", "12434523576212321"))
                .put("status", new JSONObject().put("raw", "graduated").put("encoded", "2213454313412354"))
                .put("ssn", new JSONObject().put("raw", "123-45-6789").put("encoded", "3124141231422543541"))
                .put("year", new JSONObject().put("raw", "2015").put("encoded", "2016"))
                .put("average", new JSONObject().put("raw", "5").put("encoded", "5"));

        university.put("alice_transcript_cred_values",credValuesJson);

        // 대학교가 CredentialResult를 생성한다 (IssuerWallet, cred_offer, cred_request, credValuesJson, ...)
        AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResult =
                Anoncreds.issuerCreateCredential(universityWallet, university.get("transcript_cred_offer").toString(),university.get("transcript_cred_request").toString(),
                        university.get("alice_transcript_cred_values").toString(),null,0).get();



        /*
            이게 생성한 VC임

            VC의 구조 설명
            1. signature 필드
                - m_2 : VC의 기본 클레임 값, 즉, 발행한 Holder의 정보들 포함
                - a : 발급자의 nonce 값에 해당
                - e : VC의 무결성과 신뢰성을 보장하는데 도움 됨
                - v : 발급자와 관련된 확인 키를 나타냄

                즉, 전체 p_credential 개체를 확인해야함

            2. signature_correctness_proof 필드
                - se :
                - c :


         */

        university.put("transcript_cred",issuerCredentialResult.getCredentialJson());
        System.out.println("\n 생성한 VC : " + university.get("transcript_cred"));

        System.out.println("\ntheUniversity가 엘리스에게 생성한 VC를 보낸다");
        Alice.put("transcript_cred",university.get("transcript_cred"));







        // TODO : 여기서 VC의 서명을 확인하는 부분이 필요할듯?
        //      레저로부터 VC 발급자의 DID document에서 Key를 가지고 와야함
        //      그리고 서명 검증하기! 여기서 중간에 데이터가 수정안됐음을 증명가능할까??


        // 레저에 등록되어 있는 Issuer의 공개키를 가지고 옴
        String IssuerKey = Did.keyForDid(pool, AliceWallet, university.get("did").toString()).get();

        // 해당 공개키를 사용해서 VC 검증! (VC의 p_credential 개체 확인)
        //Alice.get("transcript_cred")에서 p_credential 분리
        JSONObject AliceVC = new JSONObject(Alice.get("transcript_cred").toString());
        String signature = AliceVC.getJSONObject("signature").toString();

        try{
            System.out.println("Issuer Key : " + new JSONObject().put("verKey",IssuerKey).toString());
            System.out.println("\n\n message : "+Alice.get("transcript_cred").toString());
            System.out.println("\n\nvc 서명 : " +signature + " \n\n");

            boolean vcCheck = Crypto.cryptoVerify(IssuerKey, Alice.get("transcript_cred").toString().getBytes(), signature.getBytes()).get();
            System.out.println("Verify VC: " + vcCheck);

        }catch (Exception e){
            log.info(e.toString());
            System.out.println("error");

        }finally {
            closeAndDeletePoolLedger(stewardWallet,governmentWallet,universityWallet,companyWallet,AliceWallet,
                    steward,government,university,company,Alice,pool);
            System.exit(0);
        }






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

    public static LedgerResults.ParseResponseResult getCredDef(Pool pool, String did, String cred_def_id) throws IndyException, ExecutionException, InterruptedException {
        String get_cred_def_request = Ledger.buildGetCredDefRequest(did, cred_def_id).get();

        String get_cred_def_response = ensurePreviousRequestApplied(pool, get_cred_def_request, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });
        return Ledger.parseGetCredDefResponse(get_cred_def_response).get();
    }



    public static List<JSONObject> array2List(JSONArray credentials) {
        List<JSONObject> result = new ArrayList<>();
        credentials.forEach(o -> result.add((JSONObject) o));
        return result;
    }

    static Pool createAndOpenPoolLedger() throws Exception {

        String poolName = "pool1";
        String poolConfig = "{\"genesis_txn\": \"src/main/java/com/example/Indysdktest/indy/pool1.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool.createPoolLedgerConfig(poolName,poolConfig);

        Pool pool = openPoolLedger(poolName, "{}").get();
        System.out.println("openPoolLedger 관련 : " + pool.toString());

        return pool;
    }

    public static void closeAndDeletePoolLedger(Wallet w1, Wallet w2, Wallet w3, Wallet w4, Wallet w5,
                                                Map<String, Object> m1, Map<String, Object> m2, Map<String, Object> m3, Map<String, Object> m4 ,
                                                Map<String, Object> m5,Pool pool) throws Exception {

        // 엘리스 지갑, 대학교 지갑, 회사 지갑, 정부 지갑, seward 지갑 삭제
        closeAndDeleteWallet(w1, m1.get("wallet_config").toString(), m1.get("wallet_credentials").toString());
        closeAndDeleteWallet(w2, m2.get("wallet_config").toString(), m2.get("wallet_credentials").toString());
        closeAndDeleteWallet(w3, m3.get("wallet_config").toString(), m3.get("wallet_credentials").toString());
        closeAndDeleteWallet(w4, m4.get("wallet_config").toString(), m4.get("wallet_credentials").toString());
        closeAndDeleteWallet(w5, m5.get("wallet_config").toString(), m5.get("wallet_credentials").toString());

        pool.closePoolLedger().get();
        Pool.deletePoolLedgerConfig("pool1").get();
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
        return res.toString();
    }


}


