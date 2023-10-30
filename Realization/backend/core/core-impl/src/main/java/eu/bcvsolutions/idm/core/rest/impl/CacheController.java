package eu.bcvsolutions.idm.core.rest.impl;

import static eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission.APP_ADMIN;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Cache controller - provides clear cache and search all functionality
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/caches")
@Tag(
		name = CacheController.TAG//,
		

		

)
public class CacheController {

	protected static final String TAG = "Cache";
	//
	@Autowired private IdmCacheManager cacheManager;
	@Autowired private PagedResourcesAssembler<IdmCacheDto> pagedResourcesAssembler;

	/**
	 * Returns all available caches. Uses {@link IdmCacheManager} to fetch them. No filtering is possible. This method
	 * always returns all currently created caches. Note that spring creates caches lazily, so not all expected caches
	 * may be present in the result set.
	 *
	 * @return a list of {@link IdmCacheDto}
	 */
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@Operation(
			summary = "Get all available caches",
			/* nickname = "getAvailableCaches", */
			tags = { CacheController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						APP_ADMIN }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						APP_ADMIN })
        }
    )
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CollectionModel<?> getAvailableCaches() {
		List<IdmCacheDto> records = cacheManager.getAllAvailableCaches();
		PageImpl page = new PageImpl(records, PageRequest.of(0, records.size() == 0 ? 10 : records.size()), records.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, IdmCacheDto.class);
		}
		return pagedResourcesAssembler.toModel(page);
	}


	/**
	 * Evict cache with given name. If cache with given name does not exist, then {@link eu.bcvsolutions.idm.core.api.exception.ResultCodeException}
	 * will be thrown. Note that caches may be created lazily, so your cache may not be created in the time of calling
	 * this method. To avoid errors, check that your cache is created using getAvailableCaches.
	 *
	 * @param cacheId Name of cache to evict
	 * @throws eu.bcvsolutions.idm.core.api.exception.ResultCodeException If cache with given name does not exist
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{cacheId}/evict", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@Operation(
			summary = "Evict cache",
			/* nickname = "evictCache", */
			tags = { CacheController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						APP_ADMIN }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						APP_ADMIN })
        }
    )
	public void evictCache(
			 @Parameter(description = "Cache identifier.", required = true)
			@PathVariable @NotNull String cacheId) {
		cacheManager.evictCache(cacheId);
	}

	/**
	 * Evict all caches. It evicts all caches, that are present in container at the time of calling this method.
	 *
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/evict", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@Operation(
			summary = "Evict all caches",
			/* nickname = "evictAllCaches", */
			tags = { CacheController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							APP_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							APP_ADMIN })
        }
    )
	public void evictAllCaches() {
		cacheManager.evictAllCaches();
	}

}
