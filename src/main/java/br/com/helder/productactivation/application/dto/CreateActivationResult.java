package br.com.helder.productactivation.application.dto;

import br.com.helder.productactivation.domain.model.ActivationModel;

public record CreateActivationResult(
    ActivationModel activation,
    boolean created
) {
}