package br.com.helder.productactivation.web.controller.contract;

import br.com.helder.productactivation.web.dto.request.CreateActivationRequestDTO;
import br.com.helder.productactivation.web.dto.response.CreateActivationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/requests")
public interface ActivationControllerContract {

    @Operation(summary = "Create a new Activation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201",
            content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreateActivationResponseDTO.class))},
            description = "Activation created successfully"),
    })
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CreateActivationResponseDTO> createActivation(
        @Valid
        @RequestBody
        CreateActivationRequestDTO createActivationRequestDTO
    );
}
