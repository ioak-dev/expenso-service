package io.ioak.expenso.jwt;

import io.ioak.expenso.space.SpaceHolder;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth/{spaceId}")
@CrossOrigin
@Slf4j
public class JwtAuthenticationController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpaceHolder spaceHolder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil tokenUtil;

    @ApiOperation(value = "Create jwt Token", response = String.class)
    @GetMapping(value = "/session/{token}")
    protected ResponseEntity<?> validateToken(@PathVariable String token, @PathVariable String spaceId) throws Exception {

        String customURL = "http://127.0.0.1:8020/auth/" + "space/" + spaceId + "/session/" + token;

        try {
            ResponseEntity<UserResource> responseEntity = restTemplate.getForEntity(customURL, UserResource.class);

            UserResource userResource = responseEntity.getBody();
            spaceHolder.setSpaceId("ir_"+spaceId);
            if (userResource != null) {
                String userId = tokenUtil.extractUserWithSecurityKey(userResource.getToken(), "jwtsecret");
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    userRepository.save(user);
                }
                UserResource userResource1 = getUserResource(user, userResource.getToken());
                return ResponseEntity.ok(getUserData(userResource1));
            }

        } catch (Exception e) {

        }
        return null;
    }

    @ApiOperation(value = "Create jwt Token", response = String.class)
    @GetMapping(value = "/session/appspace/{token}")
    protected ResponseEntity<?> validateTokenAppspace(@PathVariable String token, @PathVariable String spaceId) throws Exception {

        String customURL = "http://127.0.0.1:8020/auth/" + "appspace/" + spaceId + "/session/" + token;

        try {
            ResponseEntity<UserResource> responseEntity = restTemplate.getForEntity(customURL, UserResource.class);

            UserResource userResource = responseEntity.getBody();
            spaceHolder.setSpaceId("ir_"+spaceId);
            if (userResource != null) {
                String userId = tokenUtil.extractUserWithSecurityKey(userResource.getToken(), "jwtsecret");
                User user = userRepository.findFirstByReference(userId);
                if (user == null) {
                    user = new User();
                    user.setReference(userId);
                }
                user.setEmail(userResource.getEmail());
                user.setFirstName(userResource.getFirstName());
                user.setLastName(userResource.getLastName());
                userRepository.save(user);
                UserResource userResource1 = getUserResource(user, userResource.getToken());
                return ResponseEntity.ok(getUserData(userResource1));
            }

        } catch (Exception e) {

        }
        return null;
    }

    private UserResource getUserResource(User user, String token) {
        UserResource userResource = new UserResource();
        userResource.set_id(user.getId());
        userResource.setFirstName(user.getFirstName());
        userResource.setLastName(user.getLastName());
        userResource.setEmail(user.getEmail());
        userResource.setToken(token);

        return userResource;
    }

    private UserData getUserData(UserResource user) {
        UserData userData = new UserData();
        userData.setData(user);

        return userData;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserData {
        private UserResource data;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResource {
        private String _id;
        private String firstName;
        private String lastName;
        private String email;
        private String token;
    }
}
