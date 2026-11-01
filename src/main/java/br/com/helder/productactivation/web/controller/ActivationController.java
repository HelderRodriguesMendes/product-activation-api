package br.com.helder.productactivation.web.controller;

import br.com.helder.productactivation.application.dto.CreateActivationResult;
import br.com.helder.productactivation.application.port.CreateActivationPort;
import br.com.helder.productactivation.application.port.GetActivationPort;
import br.com.helder.productactivation.domain.model.ActivationModel;
import br.com.helder.productactivation.web.controller.contract.ActivationControllerContract;
import br.com.helder.productactivation.web.dto.request.CreateActivationRequestDTO;
import br.com.helder.productactivation.web.dto.response.CreateActivationResponseDTO;
import br.com.helder.productactivation.web.mapper.ActivationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivationController implements ActivationControllerContract {

    private final CreateActivationPort createActivationPort;
    private final GetActivationPort getActivationPort;

    @Override
    public ResponseEntity<CreateActivationResponseDTO> createActivation(
        CreateActivationRequestDTO request
    ) {
        CreateActivationResult result = createActivationPort.createActivation(
            request.externalId(),
            request.customerId(),
            request.productCode()
        );

        CreateActivationResponseDTO response =
            ActivationMapper.toResponse(result.activation());

        if (!result.created()) {
            return ResponseEntity.ok(response);
        }

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.activation().getId())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<CreateActivationResponseDTO> getActivation(
        UUID id
    ) {
        ActivationModel activation = getActivationPort.getActivation(id);

        return ResponseEntity.ok(
            ActivationMapper.toResponse(activation)
        );
    }
}
