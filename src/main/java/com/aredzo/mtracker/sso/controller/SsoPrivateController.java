package com.aredzo.mtracker.sso.controller;


import com.aredzo.mtracker.sso.dto.ErrorResponse;
import com.aredzo.mtracker.sso.dto.UserPostRequest;
import com.aredzo.mtracker.sso.dto.UserResponse;
import com.aredzo.mtracker.sso.dto.UserTokenResponse;
import com.aredzo.mtracker.sso.dto.ValidateTokenResponse;
import com.aredzo.mtracker.sso.entity.UserTypeEnum;
import com.aredzo.mtracker.sso.exception.SsoServiceError;
import com.aredzo.mtracker.sso.exception.SsoServiceException;
import com.aredzo.mtracker.sso.service.SsoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@Api(value = "Sso user private controller", tags = "private")
@RequestMapping(value = "/v1/int", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "User Not Found", response = ErrorResponse.class),
        @ApiResponse(code = 403, message = "User Not Authorized", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResponse.class)
})
public class SsoPrivateController {

    private final SsoService ssoService;

    public SsoPrivateController(SsoService ssoService) {
        this.ssoService = ssoService;
    }

    @PostMapping("/users/signup")
    @ApiOperation("Signup new service user")
    public UserTokenResponse signupUser(@Valid @RequestBody UserPostRequest requestBody) {
        return ssoService.addNewUser(requestBody.getEmail(), requestBody.getPassword(), UserTypeEnum.SERVICE);
    }

    @GetMapping("/users/{userId}")
    @ApiOperation("Get user by id")
    public UserResponse getUserByUserId(
            @RequestHeader(name = "authorization") UUID token,
            @NotNull @NotEmpty @PathVariable int userId) {
        ValidateTokenResponse validateTokenResponse = ssoService.validateToken(token);
        if (
                validateTokenResponse.getUserId() == userId ||
                        validateTokenResponse.getUserType().equals(UserTypeEnum.SERVICE)) {
            return ssoService.getUserWithId(userId);
        } else {
            throw new SsoServiceException(SsoServiceError.NOT_AUTHORIZED);
        }
    }

    @GetMapping("/users")
    @ApiOperation("Get all registered user")
    public List<UserResponse> getAllUsers(@RequestHeader(name = "authorization") UUID serviceToken) {
        if (ssoService.validateToken(serviceToken).getUserType().equals(UserTypeEnum.SERVICE)) {
            return ssoService.getAllUsers();
        } else {
            throw new SsoServiceException(SsoServiceError.NOT_AUTHORIZED);
        }
    }

    @GetMapping("/token")
    @ApiOperation("Validate user token and get userId")
    public ValidateTokenResponse validateUserToken(
            @RequestHeader(name = "authorization") UUID serviceToken,
            @RequestParam(name = "token") UUID userToken) {
        if (ssoService.validateToken(serviceToken).getUserType().equals(UserTypeEnum.SERVICE)) {
            return ssoService.validateToken(userToken);
        } else {
            throw new SsoServiceException(SsoServiceError.NOT_AUTHORIZED);
        }
    }
}
