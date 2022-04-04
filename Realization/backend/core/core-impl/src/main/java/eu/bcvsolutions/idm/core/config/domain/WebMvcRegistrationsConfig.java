package eu.bcvsolutions.idm.core.config.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.InitBinderDataBinderFactory;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

/**
 * A workaround solution to mitigate the CVE-2022-22965 vulnerability.
 * This should be a temporary solution removed once Spring Boot is updated
 * to v5.3.18 (TODO). 
 * 
 * @author Tomáš Doischer
 *
 */
@Configuration
public class WebMvcRegistrationsConfig {
	@Bean
	public WebMvcRegistrations mvcRegistrations() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
				return new ExtendedRequestMappingHandlerAdapter();
			}
		};
	}


	private static class ExtendedRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

		@Override
		protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> methods) {

			return new ServletRequestDataBinderFactory(methods, getWebBindingInitializer()) {

				@Override
				protected ServletRequestDataBinder createBinderInstance(
						Object target, String name, NativeWebRequest request) throws Exception {

					ServletRequestDataBinder binder = super.createBinderInstance(target, name, request);
					String[] fields = binder.getDisallowedFields();
					List<String> fieldList = new ArrayList<>(fields != null ? Arrays.asList(fields) : Collections.emptyList());
					fieldList.addAll(Arrays.asList("class.*", "Class.*", "*.class.*", "*.Class.*"));
					binder.setDisallowedFields(fieldList.toArray(new String[] {}));
					return binder;
				}
			};
		}
	}
}