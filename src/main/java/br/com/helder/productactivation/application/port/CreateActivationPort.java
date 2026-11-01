package br.com.helder.productactivation.application.port;

import br.com.helder.productactivation.application.dto.CreateActivationResult;

public interface CreateActivationPort {

    CreateActivationResult createActivation(
        String externalId,
        String customerId,
        String productCode
    );
}
