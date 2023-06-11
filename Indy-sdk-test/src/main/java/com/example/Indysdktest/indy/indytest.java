package com.example.Indysdktest.indy;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hyperledger.indy.sdk.IndyConstants.ROLE_TRUSTEE;

public class indytest {
    public static void setUp() throws Exception {
        System.out.println("\n\n\n");
        System.out.println("STEP 1 - Connect to Pool");
        Pool pool = createAndOpenPoolLedger();

        ////////////////////////////////////////
        System.out.println("\n\n\nSTEP 2 - Configuring steward");
        // Steward 정보 설정
        Map<String, Object> steward = new HashMap<>();
        steward.put("name", "Sovrin Steward");
        steward.put("wallet_config", new JSONObject().put("id", "Trustee_wallet").toString());
        steward.put("wallet_credentials", new JSONObject().put("key", "Trustee_wallet_key").toString());
        steward.put("seed", "000000000000000000000000Trustee1");

        Wallet.createWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();
        Wallet stewardWallet = Wallet.openWallet(steward.get("wallet_config").toString(), steward.get("wallet_credentials").toString()).get();

        if(stewardWallet != null){
            try {
                // Create Trustee DID
                String trusteeSeed = new JSONObject().put("seed", "000000000000000000000000Trustee1").toString();
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(stewardWallet, trusteeSeed).get();

                steward.put("did",didResult.getDid());
                steward.put("key",didResult.getVerkey());

//                String stewardDid = Did.getDidWithMeta(stewardWallet, "V4SGRU86Z58d6TV7PBUe6f").get();
//                JSONObject stewardDidJson = new JSONObject(stewardDid);
//
//                steward.put("did",stewardDidJson.getString("did"));
//                steward.put("key",stewardDidJson.getString("verkey"));

                // 결과 출력
                System.out.println("steward DID: " + steward.get("did"));
                System.out.println("steward Key: " + steward.get("key"));


                // Store Trustee DID in the Ledger
                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), steward.get("did").toString(),
                        steward.get("key").toString(), null, ROLE_TRUSTEE).get();
                String did = Ledger.signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest).get();

                System.out.println("결과 : " + did);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(stewardWallet).get());


        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for government");

        // Government 정보 설정
        Map<String, Object> government = new HashMap<>();
        government.put("name", "theGovernment");
        government.put("wallet_config", new JSONObject().put("id", "Government").toString());
        government.put("wallet_credentials", new JSONObject().put("key", "the_Government_wallet_key"));
        government.put("pool", pool);
        government.put("role", "ENDORSER");

        Wallet.createWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();
        Wallet governmentWallet = Wallet.openWallet(government.get("wallet_config").toString(), government.get("wallet_credentials").toString()).get();

        if(governmentWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(governmentWallet, new JSONObject().put("seed", "000000000000000000000Government1").toString()).get();
                government.put("did",didResult.getDid());
                government.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("government DID: " + government.get("did"));
                System.out.println("government Key: " + government.get("key"));

                // steward의 did를 사용하여 government의 did를 등록하고, 해당 did를 TRUST_ANCHOR 역할로 설정하는 작업을 수행
                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), government.get("did").toString(), government.get("key").toString(),
                        null, government.get("role").toString()).get();
                String res = Ledger.signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest).get();
                System.out.println("결과 : " + res);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(governmentWallet).get());


        System.out.println("\n\n\n");
        System.out.println("STEP 3 - register DID for University");

        // University 정보 설정
        Map<String, Object> university = new HashMap<>();
        university.put("name", "University");
        university.put("wallet_config", new JSONObject().put("id", "University").toString());
        university.put("wallet_credentials", new JSONObject().put("key", "the_University_wallet_key").toString());
        university.put("pool", pool);
        university.put("role", "ENDORSER");


        Wallet.createWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();
        Wallet universityWallet = Wallet.openWallet(university.get("wallet_config").toString(), university.get("wallet_credentials").toString()).get();

        if(universityWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(universityWallet, new JSONObject().put("seed", "000000000000000000000University1").toString()).get();
                university.put("did",didResult.getDid());
                university.put("key",didResult.getVerkey());

                // 결과 출력
                System.out.println("university DID: " + university.get("did"));
                System.out.println("university Key: " + university.get("key"));


                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), university.get("did").toString(), university.get("key").toString(),
                        null, university.get("role").toString()).get();
                String res = Ledger.signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest).get();
                System.out.println("결과 : " + res);

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
        company.put("wallet_config", new JSONObject().put("id", "Company").toString());
        company.put("wallet_credentials", new JSONObject().put("key", "the_Company_wallet_key").toString());
        company.put("pool", pool);
        company.put("role", "ENDORSER");

        Wallet.createWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();
        Wallet companyWallet = Wallet.openWallet(company.get("wallet_config").toString(), company.get("wallet_credentials").toString()).get();

        if(companyWallet != null){
            try {
                DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(companyWallet, new JSONObject().put("seed", "000000000000000000000000Company1").toString()).get();
                company.put("did",didResult.getDid());
                company.put("key",didResult.getVerkey());

                System.out.println("company DID: " + company.get("did"));
                System.out.println("company Key: " + company.get("key"));

                String nymRequest = Ledger.buildNymRequest(steward.get("did").toString(), company.get("did").toString(), company.get("key").toString(),
                        null, company.get("role").toString()).get();
                String res = Ledger.signAndSubmitRequest(pool, stewardWallet, steward.get("did").toString(), nymRequest).get();
                System.out.println("결과 : " + res);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(companyWallet).get());


        /*
           STEP 4 : 해당 코드는 정부가 credential schema를 생성하고, 해당 schema를 ledger에 전송하는 과정

           스키마의 이름은 Transcript, 버전은 1.2 , 포함될 속성은 first_name, last_name 등등등... (나중에 우리가 정의해야할 부분)
         */

        System.out.println("\n\n\n");
        System.out.println("STEP 4 - Government creates credential schema");

        System.out.println("\"Government\" -> Create Transcript Schema");

        Map<String, Object> transcript = new HashMap<>();
        transcript.put("name", "Transcript");
        transcript.put("version", "1.2");
        transcript.put("attributes", new JSONArray(
                Arrays.asList("first_name", "last_name",
                        "degree", "status", "year", "average", "ssn")).toString());

        // issuerCreateSchema 함수를 사용하여 스키마를 생성
        AnoncredsResults.IssuerCreateSchemaResult schemaResult = Anoncreds.issuerCreateSchema(
                government.get("did").toString(),
                transcript.get("name").toString(),
                transcript.get("version").toString(),
                transcript.get("attributes").toString()
        ).get();

        // 생성된 스키마 ID랑 Json을 government Map에 저장
        government.put("transcript_schema_id", schemaResult.getSchemaId());
        government.put("transcript_schema", schemaResult.getSchemaJson());

        String transcript_schema_id =government.get("transcript_schema_id").toString();

        System.out.println("\"Government\" -> Send \"Transcript\" Schema to Ledger");
        System.out.println("정부가 정의해둔 Schema : " + government.get("transcript_schema").toString());


        //Ledger.buildSchemaRequest 함수를 사용하여 위에서 만든 스키마를 ledger에 전송하기 위한 요청을 생성
        String schemaRequest = Ledger.buildSchemaRequest(government.get("did").toString(), government.get("transcript_schema").toString()).get();
        System.out.println(schemaRequest);

        // Ledger.signAndSubmitRequest 함수를 사용하여 스키마 등록요청을 서명하고 ledger에 제출
        String res = Ledger.signAndSubmitRequest((Pool) government.get("pool"), governmentWallet, government.get("did").toString(),
                schemaRequest).get();
        System.out.println(res);


        // STEP - 5 : University will create a credential definition
        System.out.println("\n\n\n");
        System.out.println("STEP5 - University creates Transcript Credential Definition");

        System.out.println("\"theUniversity\" -> Get \"Transcript\" Schema from Ledger");

        // GET SCHEMA FROM LEDGER (앞에서 정부가 정의해둔 Schema를 레저로 부터 가져옴)
        String getSchemaRequest = Ledger.buildGetSchemaRequest(university.get("did").toString(), transcript_schema_id).get();
        System.out.println(getSchemaRequest);

        // TODO : 이 부분이 다르긴 함
        String getSchemaResponse = ensurePreviousRequestApplied((Pool) university.get("pool"), getSchemaRequest, response -> {
            JSONObject getSchemaResponseObject = new JSONObject(response);
            return !getSchemaResponseObject.getJSONObject("result").isNull("seqNo");
        });

        System.out.println(getSchemaResponse);

        LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();


        university.put("transcript_schema_id", parseSchemaResult.getId());
        university.put("transcript_schema",parseSchemaResult.getObjectJson());

        System.out.println(parseSchemaResult.getObjectJson());


        // TRANSCRIPT CREDENTIAL DEFINITION
        // 여기서 정의한건 university가 지갑에 가지고 있는건가?? 어디에 들어가는거지
        System.out.println("\"theUniversity\" -> Create and store in Wallet \"theUniversity Transcript\" Credential Definition");

        AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult = Anoncreds.issuerCreateAndStoreCredentialDef(
                universityWallet, university.get("did").toString(), university.get("transcript_schema").toString(), "TAG1", null, new JSONObject().put("support_revocation", false).toString()).get();

        university.put("transcript_cred_def_id",createCredDefResult.getCredDefId());
        university.put("transcript_cred_def",createCredDefResult.getCredDefJson());

        System.out.println("\"theUniversity\" -> Send  \"theUniversity Transcript\" Credential Definition to Ledger");

        String credDefRequest = Ledger.buildCredDefRequest(university.get("did").toString(),university.get("transcript_cred_def").toString()).get();
        Ledger.signAndSubmitRequest(pool, universityWallet, university.get("did").toString(), credDefRequest);

        System.out.println(">>>>>>>>>>"+university.get("transcript_cred_def_id"));
        System.out.println(university.get("transcript_cred_def"));


        // STEP6 - University Issues Transcript Credential to Alice - 엘리스에게 VC 발급
        System.out.println("\n\n\n");
        System.out.println("STEP6 - University Issues Transcript Credential to Alice");
        System.out.println("===================================================");
        System.out.println("Getting Transcript with theUniversity ");
        System.out.println("===================================================");

        /*
            우선 엘리스 setup(지갑생성등) 부터 하자
            우린 setup과정이 회원가입일 것임

         */

        // Alice 정보 설정
        Map<String, Object> Alice = new HashMap<>();
        Alice.put("name", "Alice");
        Alice.put("wallet_config", new JSONObject().put("id", "alice_wallet").toString());
        Alice.put("wallet_credentials", new JSONObject().put("key", "alice_wallet_key").toString());
        Alice.put("pool", pool);

        Wallet.createWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();
        Wallet AliceWallet = Wallet.openWallet(Alice.get("wallet_config").toString(), Alice.get("wallet_credentials").toString()).get();

        if(AliceWallet != null){
            try {

              DidResults.CreateAndStoreMyDidResult didResult = Did.createAndStoreMyDid(AliceWallet, new JSONObject().put("seed", "00000000000000000000000000Alice1").toString()).get();
                Alice.put("did",didResult.getDid());
                Alice.put("key",didResult.getVerkey());

                System.out.println("Alice DID: " + Alice.get("did"));
                System.out.println("Alice Key: " + Alice.get("key"));

                String nymRequest = Ledger.buildNymRequest(government.get("did").toString(), Alice.get("did").toString(), Alice.get("key").toString(),
                        null, null).get();
                String resd = Ledger.signAndSubmitRequest(pool, governmentWallet, government.get("did").toString(), nymRequest).get();
                System.out.println("결과 : " + resd);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println(Did.getListMyDidsWithMeta(AliceWallet).get());


        // University creates transcript credential offer
        System.out.println("University creates and sends transcript credential offer to Alice");
        System.out.println(" 대학교가 alice에게 transcript credential(이게 VC 인가..?)를 보냄");

        String transcriptCredOffer = Anoncreds.issuerCreateCredentialOffer(
                universityWallet, university.get("transcript_cred_def_id").toString()).get();

        university.put("transcript_cred_offer",transcriptCredOffer);

        System.out.println("\n\"theUniversity\" -> Send \"Transcript\" Credential Offer to Alice");
        Alice.put("transcript_cred_offer",transcriptCredOffer);

        //
        System.out.println("Alice prepares a transcript credential request");

        String transcriptCredDefId = new JSONObject(transcriptCredOffer).getString("cred_def_id");
        String transcriptSchemaId = new JSONObject(transcriptCredOffer).getString("schema_id");

        Alice.put("transcript_schema_id",transcriptSchemaId);
        Alice.put("transcript_cred_def_id",transcriptCredDefId);


        System.out.println("\n\"Alice\" -> Create and store \"Alice\" Master Secret in Wallet");
        String AliceMasterSecretId = Anoncreds.proverCreateMasterSecret(AliceWallet, null).get();
        Alice.put("master_secret_id",AliceMasterSecretId);


        System.out.println("\n\"Alice\" -> Get \"theUniversity Transcript\" Credential Definition from Ledger");
        LedgerResults.ParseResponseResult parsedCredDefResponse = getCredDef(pool, Alice.get("did").toString(), Alice.get("transcript_cred_def_id").toString());

        Alice.put("theUniversity_transcript_cred_def_id",parsedCredDefResponse.getId());
        Alice.put("theUniversity_transcript_cred_def",parsedCredDefResponse.getObjectJson());


        System.out.println("\n\"Alice\" -> Create \"Transcript\" Credential Request for theUniversity");
        AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult =
                Anoncreds.proverCreateCredentialReq(AliceWallet, Alice.get("did").toString(), Alice.get("transcript_cred_offer").toString(),
                Alice.get("theUniversity_transcript_cred_def").toString(), Alice.get("master_secret_id").toString()).get();
        Alice.put("transcript_cred_request",credentialRequestResult.getCredentialRequestJson());
        Alice.put("transcript_cred_request_metadata",credentialRequestResult.getCredentialRequestMetadataJson());


        System.out.println("\n\"Alice\" -> Send \"Transcript\" Credential Request to theUniversity");
        university.put("transcript_cred_request",Alice.get("transcript_cred_request"));

        // 대학교에서 엘리스에게 credential을 발급해준다 (대학교가 알고 있는 엘리스의 개인정보들) - 즉, 이게 VC 인거지
        // TODO : 여기서 encoded는 뭘까?
        System.out.println("\nUniversity issues credential to alice");
        System.out.println("\"theUniversity\" -> Create \"Transcript\" Credential for Alice");
        JSONObject credValuesJson = new JSONObject()
                .put("first_name", new JSONObject().put("raw", "Alice").put("encoded", "1139481716457488690172217916278103335"))
                .put("last_name", new JSONObject().put("raw", "Garcia").put("encoded", "5321642780241790123587902456789123452"))
                .put("degree", new JSONObject().put("raw", "Bachelor of Science, Marketing").put("encoded", "12434523576212321"))
                .put("status", new JSONObject().put("raw", "graduated").put("encoded", "2213454313412354"))
                .put("ssn", new JSONObject().put("raw", "123-45-6789").put("encoded", "3124141231422543541"))
                .put("year", new JSONObject().put("raw", "2015").put("encoded", "2015"))
                .put("average", new JSONObject().put("raw", "5").put("encoded", "5"));

        university.put("alice_transcript_cred_values",credValuesJson);

        AnoncredsResults.IssuerCreateCredentialResult issuerCredentialResult =
                Anoncreds.issuerCreateCredential(universityWallet, university.get("transcript_cred_offer").toString(),university.get("transcript_cred_request").toString(),
                university.get("alice_transcript_cred_values").toString(),null,0).get();

        String transcriptCredJson = issuerCredentialResult.getCredentialJson();
        university.put("transcript_cred",transcriptCredJson);


        System.out.println("\"theUniversity\" -> Send \"Transcript\" Credential to Alice");
        System.out.println(transcriptCredJson);

        Alice.put("transcript_cred",university.get("transcript_cred"));
        System.out.println("\"Alice\" -> Store \"Transcript\" Credential from theUniversity");

        // TODO : ??
        String alice_trans_cred_def = getCredDef(pool, Alice.get("did").toString(), Alice.get("transcript_cred_def_id").toString()).getObjectJson();
        Alice.put("transcript_cred_def",alice_trans_cred_def);

        String transcriptCredentialId = Anoncreds.proverStoreCredential(AliceWallet, null, Alice.get("transcript_cred_request_metadata").toString()
                , Alice.get("transcript_cred").toString(), Alice.get("transcript_cred_def").toString(), null).get();

        System.out.println("\\n\\n>>>>>>>>>>>>>>>>>>>>>>.\\n\\n" + Alice.get("transcript_cred_def"));


        // VP(Verifiable Presentation) 생성
        // STEP7 - Alice makes verifiable presentation(VP) to Company
        // 우리는 차량 등록시에 VP를 제출하고 서비스에서 검증한 후 검증한 데이터를 아마도..? (애매하네)
        System.out.println("\n\n\nSTEP7 - Alice makes verifiable presentation(VP) to Company\n");

        // 우린 서비스에서 차량 전용 Proof Request를 만들어야 함 (이게 회사에서 필요한 양식)
        // 그러니깐 우리회사에 지원할 꺼면 first_name, last_name,degree,status,ssn,phone_number를 내라! 이거네
        System.out.println("\"theCompany\" -> Create \"Job-Application\" Proof Request");

        String nonce = Anoncreds.generateNonce().get();
        JSONArray transcriptRestrictions = new JSONArray().put(new JSONObject().put("cred_def_id", university.get("transcript_cred_def_id").toString()));

        String proofRequestJson = new JSONObject()
                .put("nonce", nonce)
                .put("name", "Job-Application")
                .put("version", "0.1")
                .put("requested_attributes", new JSONObject()
                        .put("attr1_referent", new JSONObject().put("name", "first_name"))
                        .put("attr2_referent", new JSONObject().put("name", "last_name"))
                        .put("attr3_referent", new JSONObject().put("name", "degree").put("restrictions", transcriptRestrictions))
                        .put("attr4_referent", new JSONObject().put("name", "status").put("restrictions", transcriptRestrictions))
                        .put("attr5_referent", new JSONObject().put("name", "ssn").put("restrictions", transcriptRestrictions))
                        .put("attr6_referent", new JSONObject().put("name", "year").put("restrictions", transcriptRestrictions)))
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("name", "average")
                                .put("p_type", ">=")
                                .put("p_value", 4)
                                .put("restrictions", transcriptRestrictions)))
                .toString();

        company.put("job_application_proof_request", proofRequestJson);


        System.out.println("Job-Application Proof Request: " + proofRequestJson.toString());

        // 회사가 위에서 만든 Proof Request (지원 양식)을 Alice에게 전해줌
        // ALice는 이전에 발급받은 VC로 양식을 채워서 다시 제출해야할듯?
        System.out.println("\n\"theCompany\" -> Send \"Job-Application\" Proof Request to Alice");
        Alice.put("job_application_proof_request", company.get("job_application_proof_request"));

        // 즉, 받은 양식을 VC로 채워서 보내는게 VP
        // Alice는 양식 받아서 Presentation(VP)을 준비함
        System.out.println("\"Alice\" -> Get credentials for \"Job-Application\" Proof Request");

        // TODO : 엘리스 지갑에서 꺼낸듯?
        CredentialsSearchForProofReq search_for_job_application_proof_request = CredentialsSearchForProofReq.open(
                AliceWallet, Alice.get("job_application_proof_request").toString(), null).get();

        System.out.println("====================================================================");
        System.out.println(search_for_job_application_proof_request);
        System.out.println("====================================================================");


        // TODO : 이 부분 이해가 잘안되네
        JSONArray credentialsForAttribute3 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr3_referent", 100).get());
        String credentialIdForAttribute3 = credentialsForAttribute3.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute4 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr4_referent", 100).get());
        String credentialIdForAttribute4 = credentialsForAttribute4.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute5 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr5_referent", 100).get());
        String credentialIdForAttribute5 = credentialsForAttribute5.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForAttribute6 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("attr6_referent", 100).get());
        String credentialIdForAttribute6 = credentialsForAttribute6.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        JSONArray credentialsForPredicate1 = new JSONArray(search_for_job_application_proof_request.fetchNextCredentials("predicate1_referent", 100).get());
        String credentialIdForPredicate1 = credentialsForPredicate1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

        search_for_job_application_proof_request.close();

        // Alice -> Create Job-Application Proof
        // 우리회사에 지원할 꺼면 first_name, last_name,    (degree,status,ssn),    phone_number
        String credentialsJson = new JSONObject()
                .put("self_attested_attributes", new JSONObject()
                        .put("attr1_referent", "Alice")
                        .put("attr2_referent", "Garcia"))
                .put("requested_attributes", new JSONObject()
                        .put("attr3_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute3)
                                .put("revealed", true))
                        .put("attr4_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute4)
                                .put("revealed", true))
                        .put("attr5_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute5)
                                .put("revealed", true))
                        .put("attr6_referent", new JSONObject()
                                .put("cred_id", credentialIdForAttribute6)
                                .put("revealed", true)))
                .put("requested_predicates", new JSONObject()
                        .put("predicate1_referent", new JSONObject()
                                .put("cred_id",credentialIdForPredicate1)))
                .toString();

        JSONObject schemasMap = new JSONObject();
        JSONObject credDefsMap = new JSONObject();

        // schemasMap, credDefsMap 이 부분 하나씩만!!!!!!!!!!!!!!! 해보자
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute3);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute4);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute5);
        populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute6);

        String schemas = schemasMap.toString();
        String credDefs = credDefsMap.toString();
        String revocState = new JSONObject().toString();


        Alice.put("job_application_requested_creds",credentialsJson);

        //Alice creates the Proof for Acme Job-Application Proof Request
        // proof가 최종 제출할 VP
        String proofJson = Anoncreds.proverCreateProof(AliceWallet, Alice.get("job_application_proof_request").toString()
                            ,Alice.get("job_application_requested_creds").toString()
                            ,Alice.get("master_secret_id").toString()
                            ,schemas, credDefs, revocState).get();

        JSONObject proof = new JSONObject(proofJson);

        System.out.println("\n\n 최종 VP \n");
        System.out.println(proof);
        System.out.println("==============");
        Alice.put("job_application_proof", proof);

        // 이렇게 만든 VP를 Company에 제출!
        System.out.println( "\n\"Alice\" -> Send \"Job-Application\" Proof to theCompany" );
        company.put("job_application_proof", Alice.get("job_application_proof"));

        // 마지막 STEP 8 !!! 회사가 받은 엘리스의 VP를 검증하는 과정!!!!!!!!!
        // company.get("job_application_proof") 이게 받은거
        System.out.println("\n\n\n STEP8 - Company validates Alice's claims");

        //company.get("job_application_proof");
        JSONObject selfAttestedAttrs = new JSONObject(company.get("job_application_proof").toString()).getJSONObject("requested_proof").getJSONObject("self_attested_attrs");
        JSONObject revealedAttrs = new JSONObject(company.get("job_application_proof").toString()).getJSONObject("requested_proof").getJSONObject("revealed_attrs");
        System.out.println("SelfAttestedAttrs: " + selfAttestedAttrs);
        System.out.println("RevealedAttrs: " + revealedAttrs);

        System.out.println("Alice ==" + selfAttestedAttrs.getString("attr1_referent"));
        System.out.println("Garcia ==" + selfAttestedAttrs.getString("attr2_referent"));
        System.out.println("Bachelor of Science, Marketing==" + revealedAttrs.getJSONObject("attr3_referent").getString("raw"));
        System.out.println("graduated ==" + revealedAttrs.getJSONObject("attr4_referent").getString("raw"));
        System.out.println("123-45-6789 ==" + revealedAttrs.getJSONObject("attr5_referent").getString("raw"));
        System.out.println("2015 ==" + revealedAttrs.getJSONObject("attr6_referent").getString("raw"));

        String revocRegDefs = new JSONObject().toString();
        String revocRegs = new JSONObject().toString();

        Boolean same = Anoncreds.verifierVerifyProof(
                company.get("job_application_proof_request").toString(),
                company.get("job_application_proof").toString(), // 제출한 VP
                schemas, credDefs,
                revocRegDefs, revocRegs).get();

        System.out.println("일치 : " + same +"\n\n");

        // 마무리
        closeAndDeletePoolLedger(stewardWallet,governmentWallet,universityWallet,companyWallet,AliceWallet
                ,steward,government,university,company,Alice,pool);

    }


    // TODO : 이미 존재하면 안만들도록 예외처리
    public static void createWallet(Map<String, Object> identity) throws IndyException {
        System.out.println("\"" + identity.get("name") + "\" -> Create 타요타요's Wallet");

        Wallet.createWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString());
    }

    public static void deleteWallet(Map<String, Object> identity) throws IndyException {
        System.out.println("\"" + identity.get("name") + "\" -> Delete 타요타요's Wallet");

        Wallet.deleteWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString());
    }

    public static Wallet openWallet(Map<String, Object> identity) throws IndyException, ExecutionException, InterruptedException {
        System.out.println("\"" + identity.get("name") + "\" -> Open 타요타요's Wallet");

        Wallet wallet = Wallet.openWallet(identity.get("wallet_config").toString(), identity.get("wallet_credentials").toString()).get();

        return wallet;
    }

    public static DidResults.CreateAndStoreMyDidResult createAndStoreUserDid(Wallet wallet, Map<String, Object> identity)
            throws IndyException, ExecutionException, InterruptedException {

        String DidInfo;
        if(identity.containsKey("seed")){
            DidInfo = "{\"seed\": \"" + identity.get("seed").toString() + "\"}";
        }else{
            DidInfo = "{}";
        }

        System.out.println("DidInfo : " + DidInfo);

        DidResults.CreateAndStoreMyDidResult DidResult = Did.createAndStoreMyDid(wallet, DidInfo).get();
        System.out.println("create and store my did 관련 : " + DidResult);

        return DidResult;
    }

    /*
        sendNym((Pool) steward.get("pool") , stewardWallet,(String)steward.get("did"),
                        (String)company.get("did"),(String)company.get("key"),(String) company.get("role"));
     */
    public static void sendNym(Pool poolHandle, Wallet wallet, String did, String newDid, String newKey, String role) throws Exception {
        // 새로운 Nym 등록을 위해 요청 생성

        String nymRequest = Ledger.buildNymRequest(did, newDid, newKey, null, role).get();
        System.out.println("buildNumRequest 관련 : "+nymRequest);

        // 생성된 요청에 서명하고 Indy 풀에 제출하여 ledger에 기록
        String res = Ledger.signAndSubmitRequest(poolHandle, wallet, did, nymRequest).get();
        System.out.println("signAndSubmitRequest 관련 : "+res);
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

    //populateCredentialInfo(pool, Alice.get("did").toString(), schemasMap, credDefsMap, credentialsForAttribute1);
    public static void populateCredentialInfo(Pool pool, String did, JSONObject schemas, JSONObject credDefs, JSONArray credentials) throws Exception {
        for (JSONObject o : array2List(credentials)) {
            JSONObject credInfo = o.getJSONObject("cred_info");
            String schemaId = credInfo.getString("schema_id");
            String credDefId = credInfo.getString("cred_def_id");
//            System.out.println("\n-->" + o);
//            System.out.println(credInfo);
//            System.out.println(schemaId);
//            System.out.println(credDefId+"\n");

            if (schemas.isNull(schemaId)) {
                String getSchemaRequest = Ledger.buildGetSchemaRequest(did, schemaId).get();
                String getSchemaResponse = Ledger.submitRequest(pool, getSchemaRequest).get();
                LedgerResults.ParseResponseResult parseSchemaResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
                String schemaJson = parseSchemaResult.getObjectJson();
                schemas.put(schemaId, new JSONObject(schemaJson));
            }
            if (credDefs.isNull(credDefId)) {
                String getCredDefRequest = Ledger.buildGetCredDefRequest(did, credDefId).get();
                String getCredDefResponse = Ledger.submitRequest(pool, getCredDefRequest).get();
                LedgerResults.ParseResponseResult parseCredDefResponse = Ledger.parseGetCredDefResponse(getCredDefResponse).get();

                String credDefJson = parseCredDefResponse.getObjectJson();
                credDefs.put(credDefId, new JSONObject(credDefJson));
            }
        }
    }

    public static List<JSONObject> array2List(JSONArray credentials) {
        List<JSONObject> result = new ArrayList<>();
        credentials.forEach(o -> result.add((JSONObject) o));
        return result;
    }

    static Pool createAndOpenPoolLedger() throws Exception {

        String poolName = "pool1";
        String poolConfig = "{\"genesis_txn\": \"Indy-sdk-test/src/main/java/com/example/Indysdktest/indy/pool1.txn\"} ";

        Pool.setProtocolVersion(2);
        Pool.createPoolLedgerConfig(poolName,poolConfig);

        Pool pool = Pool.openPoolLedger(poolName, "{}").get();
        System.out.println("openPoolLedger 관련 : " + pool.toString());

        return pool;
    }

    public static void closeAndDeletePoolLedger(Wallet w1, Wallet w2, Wallet w3, Wallet w4, Wallet w5,
                                                Map<String, Object> m1, Map<String, Object> m2, Map<String, Object> m3, Map<String, Object> m4 , Map<String, Object> m5,Pool pool) throws Exception {

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


}


