package pnu.cse.TayoTayo.VC_Service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VCRequest {

    String credentialRequestJson;
    String credentialOffer;
    String memberName;
    String carNumber;

}
