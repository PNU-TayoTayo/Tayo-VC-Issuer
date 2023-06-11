package com.example.Indysdktest;

import com.example.Indysdktest.indy.indytest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class IndySdkTestApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(IndySdkTestApplication.class, args);

		System.out.println("before setup");
		indytest.setUp();
		System.out.println("after setup");

		System.exit(0);
	}

}
