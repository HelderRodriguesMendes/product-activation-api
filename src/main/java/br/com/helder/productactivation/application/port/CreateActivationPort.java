package br.com.helder.productactivation.application.port;

import br.com.helder.productactivation.domain.model.ActivationModel;

public interface CreateActivationPort {
    ActivationModel createActivation(
        String externalId,
        String customerId,
        String productCode
    );
}
