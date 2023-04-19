package eu.bcvsolutions.idm.core.api.config.swagger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import io.swagger.annotations.Api;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Modular swagger simple configuration
 * - expose all api endpoints in given basePackage
 *
 * @author Radek Tomiška
 *
 */
public abstract class AbstractSwaggerConfig implements SwaggerConfig {

	/**
	 * Module for this swagger configuration
	 *
	 * @return
	 */
	protected abstract ModuleDescriptor getModuleDescriptor();

	/**
	 * Docket initialization by module conventions.
	 *
	 * @see ModuleDescriptor
	 * @param basePackages Expose endpoints from given base basePackages
	 * @return
	 */
	protected Docket api(String... basePackages) {
		return new Docket(DocumentationType.SWAGGER_2)
				// common
				.forCodeGeneration(true)
				.genericModelSubstitutes(ResponseEntity.class)
				.securitySchemes(Arrays.asList(
						new BasicAuth(AUTHENTICATION_BASIC),
						apiKey()
						))
				.ignoredParameterTypes(Pageable.class, MultiValueMap.class)
				// module
				.groupName(getModuleDescriptor().getId())
				.select()
					.apis(getApis(basePackages))
					.paths(PathSelectors.any())
				.build()
				.apiInfo(metaData());
	}

	/**
	 * Expose endpoints from given base packages. Security endpoint will be in all docs.
	 *
	 * @param basePackages
	 * @return
	 */
	protected Predicate<RequestHandler> getApis(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package is required to generate swagger documentation.");
		//
		// endpoints from packages
		List<Predicate<RequestHandler>> basePackagesPredicates = Arrays.stream(basePackages)
				.map(RequestHandlerSelectors::basePackage)
				.collect(Collectors.toList());
		//
		Predicate<RequestHandler> apiPredicate = basePackagesPredicates.stream().reduce(Predicate::or).orElse(x -> false);
		// and with annotations
		apiPredicate.and(RequestHandlerSelectors.withClassAnnotation(Api.class));
		return apiPredicate;
	}

	/**
	 * CIDMST token authentication.
	 *
	 * @return
	 */
	protected ApiKey apiKey() {
		return new ApiKey(AUTHENTICATION_CIDMST, AUTHENTICATION_CIDMST_TOKEN, "header");
	}

	/**
	 * TODO: license to properties (maven license plugin or simple pom props?).
	 *
	 * @return
	 */
	protected ApiInfo metaData() {
		ApiInfo apiInfo = new ApiInfo(
                getModuleDescriptor().getName() + " - RESTful API",
                getModuleDescriptor().getDescription(),
                getModuleDescriptor().getVersion(),
                "Terms of service",
                new Contact(getModuleDescriptor().getVendor(), getModuleDescriptor().getVendorUrl(), getModuleDescriptor().getVendorEmail()),
               "MIT",
                "https://github.com/bcvsolutions/CzechIdMng/blob/develop/LICENSE",
                Lists.newArrayList());
		//
		return apiInfo;
    }
}
