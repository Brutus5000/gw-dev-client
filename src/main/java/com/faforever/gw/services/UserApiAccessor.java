package com.faforever.gw.services;

import com.faforever.gw.model.ClientState;
import com.faforever.gw.services.api.messages.CreditsResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class UserApiAccessor {

	private CountDownLatch authorizedLatch;

	private RestTemplateBuilder restTemplateBuilder;
	private RestTemplate restTemplate;

	@Value("${gw.server.host}")
	private String host;
	@Value("${gw.server.port}")
	private int port;
	@Value("${gw.server.protocol}")
	private String protocol;

	@Value("${api.client-id}")
	private String apiClientId;
	@Value("${api.client-secret}")
	private String apiClientSecret;
	@Value("${api.access-token-uri}")
	private String apiAccessTokenUrl;

	@Inject
	public UserApiAccessor(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;

		authorizedLatch = new CountDownLatch(1);
	}

	@PostConstruct
	public void init() {
		restTemplateBuilder = restTemplateBuilder.rootUri(buildRootURL());
	}

	@SneakyThrows
	public void authorize(String username, String password) {
		log.debug("Configuring OAuth2 login with player = '{}', password=[hidden]", username);
		ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
		details.setClientId(apiClientId);
		details.setClientSecret(apiClientSecret);
		details.setClientAuthenticationScheme(AuthenticationScheme.header);
		details.setAccessTokenUri(apiAccessTokenUrl);
		details.setUsername(username);
		details.setPassword(password);

		if(username != null) {//Debugging purposes, no auth
			restTemplate = restTemplateBuilder.configure(new OAuth2RestTemplate(details));
		} else {
			restTemplate = restTemplateBuilder.build();
		}

		restTemplate.setInterceptors(Collections.singletonList(
				(request, body, execution) -> {
					HttpHeaders headers = request.getHeaders();
					if(username == null) {//Debugging purposes, no auth
						headers.set("Authorization", String.format("Bearer %s", password));
					}
					headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.api+json")));
					if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PATCH || request.getMethod() == HttpMethod.PUT) {
						headers.setContentType(MediaType.APPLICATION_JSON);
					}
					return execution.execute(request, body);
				}
		));

		authorizedLatch.countDown();
	}

	@EventListener
	private void onClientStateChanged(ClientState newState) {
		switch (newState) {
			case CONNECTED:
				//called from gwClient
				break;
			case DISCONNECTED:
				//TODO: reset rest template builder
				authorizedLatch = new CountDownLatch(1);
				break;
		}
	}

	@SneakyThrows
	private String buildRootURL() {
		return MessageFormat.format("{0}://{1}:{2,number,#}", protocol, host, port);
	}

	@SneakyThrows
	public int queryCredits() {

		return restTemplate.getForObject("/reinforcements/availableCredits", CreditsResponse.class).getCredits();
	}
}
