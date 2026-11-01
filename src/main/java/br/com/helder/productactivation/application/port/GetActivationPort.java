package br.com.helder.productactivation.application.port;

import br.com.helder.productactivation.domain.model.ActivationModel;

import java.util.UUID;

public interface GetActivationPort {

    ActivationModel getActivation(UUID id);
}