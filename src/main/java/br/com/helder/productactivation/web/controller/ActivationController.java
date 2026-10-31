package br.com.helder.productactivation.web.controller;

import br.com.helder.productactivation.application.port.CreateActivationPort;
import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.web.controller.contract.ActivationControllerContract;
import br.com.helder.productactivation.web.dto.request.CreateActivationRequestDTO;
import br.com.helder.productactivation.web.dto.response.CreateActivationResponseDTO;
import br.com.helder.productactivation.web.mapper.ActivationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ActivationController implements ActivationControllerContract {

    private final CreateActivationPort createActivationPort;

    @Override
    public ResponseEntity<CreateActivationResponseDTO> createActivation(CreateActivationRequestDTO createActivationRequestDTO) {

        ActivationModel activationModel = createActivationPort.createActivation(
            createActivationRequestDTO.externalId(),
            createActivationRequestDTO.customerId(),
            createActivationRequestDTO.productCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ActivationMapper.toResponse(activationModel));
    }
}
