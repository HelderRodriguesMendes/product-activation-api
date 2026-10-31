package br.com.helder.productactivation.application.usecase;

import br.com.helder.productactivation.application.port.CreateActivationPort;
import br.com.helder.productactivation.domain.model.ActivationModel;

public class CreateActivationUseCase implements CreateActivationPort{

    @Override
        public ActivationModel createActivation(
        String externalId,
        String customerId,
        String productCode
    ) {
        ActivationModel activationRequest = ActivationModel.create(
            externalId,
            customerId,
            productCode
        );

        // agora eu valido no externo, salvo e retorno

        return null;
    }
}
