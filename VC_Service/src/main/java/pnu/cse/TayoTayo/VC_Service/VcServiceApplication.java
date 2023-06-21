package pnu.cse.TayoTayo.VC_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pnu.cse.TayoTayo.VC_Service.indy.indytest;

@SpringBootApplication
public class VcServiceApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(VcServiceApplication.class, args);

		System.out.println("before setup");
		indytest.setUp();
		System.out.println("after setup");



	}

}
