package pnu.cse.TayoTayo.VC_Service.config;

import lombok.RequiredArgsConstructor;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pnu.cse.TayoTayo.VC_Service.indy.indytest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component
public class PoolAndWalletManager {

    static Logger log = LoggerFactory.getLogger(indytest.class);
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

//    @Autowired
//    public PoolAndWalletManager() throws IndyException, ExecutionException, InterruptedException {
//        // Initialize and create the pool and wallet
//        pool = createPool();
//        wallet = createWallet();
//    }

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


    @PostConstruct
    public void init() throws IndyException, ExecutionException, InterruptedException {

        System.out.println("\n steward의 DID : "+Did.getListMyDidsWithMeta(stewardWallet).get());
        System.out.println("\n government의 DID : "+Did.getListMyDidsWithMeta(governmentWallet).get());
        System.out.println("\n Issuer의 DID : "+Did.getListMyDidsWithMeta(IssuerWallet).get());

        //TODO : 앞에서 지갑 생성하고 Schema 등록이나 Credential Definition 등록 같은건 여기서 하기
        //      그리고 VC 발급해줄 수 있는 API 하나 만들기 !!! (private API 사용)
        //      흠.. offer는 어떻게 하지??



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

}
