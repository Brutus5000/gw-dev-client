package com.faforever.gw.services;

import com.faforever.gw.services.api.messages.CreditsResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.text.MessageFormat;

@Service
public class UserApiAccessor {

	private RestTemplateBuilder restTemplateBuilder;
	private RestTemplate restTemplate;

	@Value("${gw.server.host}")
	private String host;
	@Value("${gw.server.port}")
	private int port;
	@Value("${gw.server.protocol}")
	private String protocol;

	@Inject
	public UserApiAccessor(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder
				.rootUri(buildRootURL());
	}

	@SneakyThrows
	private void authorize(String username, String password) {
		log.debug("Configuring OAuth2 login with player = '{}', password=[hidden]", username);
		ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
		details.setClientId(apiClientId);
		details.setClientSecret(apiClientSecret);
		details.setClientAuthenticationScheme(AuthenticationScheme.header);
		details.setAccessTokenUri(apiAccessTokenUrl);
		details.setUsername(username);
		details.setPassword(password);

		restTemplate = restTemplateBuilder.configure(new OAuth2RestTemplate(details));
		restTemplate.setInterceptors(Collections.singletonList(
				(request, body, execution) -> {
					HttpHeaders headers = request.getHeaders();
					headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.api+json")));
					if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PATCH || request.getMethod() == HttpMethod.PUT) {
						headers.setContentType(MediaType.APPLICATION_JSON);
					}
					return execution.execute(request, body);
				}
		));

		authorizedLatch.countDown();
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
