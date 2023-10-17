package eu.bcvsolutions.idm.core.api.config.swagger;

import org.springdoc.core.GroupedOpenApi;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;

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
	protected GroupedOpenApi api(String... basePackages) {
		return GroupedOpenApi.builder()
				// common
                .displayName(getModuleDescriptor().getId())
                .group(getModuleDescriptor().getId())
                .packagesToScan(basePackages)
                .addOpenApiCustomiser(openApi->
                    {
                        openApi.setInfo(new Info().title(getModuleDescriptor().getName() + " - RESTful API")
                                .description(getModuleDescriptor().getDescription())
                                .version(getModuleDescriptor().getVersion())
                                .termsOfService("Terms of service")
                                .contact((new Contact()).name(getModuleDescriptor().getVendor()).url(getModuleDescriptor().getVendorUrl()).email(getModuleDescriptor().getVendorEmail()))
                                .license(new License().name("MIT").url("https://github.com/bcvsolutions/CzechIdMng/blob/develop/LICENSE")));
                        openApi.setComponents(new Components()
                                .addSecuritySchemes(AUTHENTICATION_BASIC, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
                                .addSecuritySchemes(AUTHENTICATION_CIDMST, new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name(AUTHENTICATION_CIDMST_TOKEN)));
                        openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                            ApiResponses apiResponses = operation.getResponses();
                            apiResponses.addApiResponse("401", new ApiResponse().description("Unauthorized"));
                            apiResponses.addApiResponse("403", new ApiResponse().description("Forbidden"));
                            apiResponses.addApiResponse("404", new ApiResponse().description("Not Found"));
                            apiResponses.addApiResponse("500", new ApiResponse().description("Server Error"));
                        }));
                    }
                )
                // module
                .build()
                ;
	}
}
