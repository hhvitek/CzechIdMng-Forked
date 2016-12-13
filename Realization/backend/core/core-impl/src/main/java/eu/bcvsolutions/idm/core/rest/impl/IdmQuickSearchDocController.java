package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;

/**
 * Endpoint for quick search documentation for other endpoints
 * 
 * TODO: parameter data types
 * TODO: other searches than quick
 * TODO: readme and example documentation endpoints (maybe static without reflection)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/doc")
public class IdmQuickSearchDocController implements BaseController {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmQuickSearchDocController.class);
	
	@Autowired
	private ApplicationContext context;
	
	private final String QUICK_SEARCH = "/search/quick";
	
	private final String RESOURCE_NAME = "resourceName";
	
	/**
	 * Returns quick search documentation
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{" + RESOURCE_NAME + "}/search", method = RequestMethod.GET)
	public ResponseEntity<?> quickSeachDocumentation(HttpServletRequest request) {
		 @SuppressWarnings("rawtypes")
		 Map<String, AbstractReadEntityController> controllers = context.getBeansOfType(AbstractReadEntityController.class);

		 String resourceName = ((Map<?, ?>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).get(this.RESOURCE_NAME).toString();

		 for (String key : controllers.keySet()) {
			 Class<?> controller = AopProxyUtils.ultimateTargetClass(controllers.get(key));
			 String[] annotations = controller.getAnnotation(RequestMapping.class).value();

			 
			 if	(annotations.length > 0 && annotations[0].endsWith(resourceName)) {
				ParameterizedType genericSuperClass = (ParameterizedType) controller.getGenericSuperclass();
				Type[] genericTypes = genericSuperClass.getActualTypeArguments();
				
				ArrayList<String> variables = new ArrayList<>();
				
				for (Type type : genericTypes) {
					try {
						Object filterObject = Class.forName(type.getTypeName()).newInstance();
						if (BaseFilter.class.isInstance(filterObject)) {							
							List<Field> pageAbleFields = this.getAllDeclaredFields(filterObject.getClass(), new ArrayList<Field>());
							variables.addAll(this.getListOfFieldsNames(pageAbleFields));
							break;
						}
					} catch(IllegalAccessException|ClassNotFoundException|InstantiationException O_o) {
						throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, "Class [" + type.getTypeName() + "] is not accessible", O_o);
					}
				}
				List<Field> pageAbleFields = this.getAllDeclaredFields(PageRequest.class, new ArrayList<Field>());
				variables.addAll(this.getListOfFieldsNames(pageAbleFields));

				Link link = linkTo(controller).withRel("quick");
				Link quickSearchLink = new Link(new UriTemplate(link.getHref() + QUICK_SEARCH + "{?" + String.join(",", variables) + "}", TemplateVariables.NONE), "quick");
				Link selfLink = linkTo(this.getClass()).withSelfRel();
				
				ResourceSupport resource = new ResourceSupport();
				resource.add(quickSearchLink);
				resource.add(selfLink);
				return new ResponseEntity<>(resource, HttpStatus.OK);
			 }
			 
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * List with all documentations
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> allQuickSearchDocumentations() {
		@SuppressWarnings("rawtypes")
		Map<String, AbstractReadEntityController> controllers = context.getBeansOfType(AbstractReadEntityController.class);
		
		LOG.debug("Found [{}] read entity controllers", controllers.size());
		
		ResourceSupport links = new ResourceSupport();
		for (String key : controllers.keySet()) {
			 Class<?> controller = AopProxyUtils.ultimateTargetClass(controllers.get(key));
			 String[] annotations = controller.getAnnotation(RequestMapping.class).value();
			 
			 if	(annotations.length > 0) {
				 String[] completeName = annotations[0].split("/");
				 String resourceName = annotations[0].replace(BaseEntityController.BASE_PATH, "");
				 Link selfLink = linkTo(BaseController.class).withSelfRel();
				 // construct link to endpoint quick search doc
				 links.add(new Link(new UriTemplate(selfLink.getHref() + BaseEntityController.BASE_PATH + "/doc" +  resourceName + "/search", TemplateVariables.NONE), completeName[completeName.length - 1]));

			 }
		}
		
		Link selfLink = linkTo(this.getClass()).withSelfRel();
		links.add(selfLink);
		
		return new ResponseEntity<>(links, HttpStatus.OK);
	}
	
	/**
	 * Method get recursive all fields to superClass == null
	 * @param type of class
	 * @param list fields 
	 * @return list fields
	 */
	private List<Field> getAllDeclaredFields(Class<?> type, List<Field> fields) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
		
	    if (type.getSuperclass() != null) {
	        fields = getAllDeclaredFields(type.getSuperclass(), fields);
	    }
	    
	    return fields;
	}
	
	/**
	 * Method return list of fields names without attribute 'serialVersionUID'
	 * @param list fields
	 * @return list string
	 */
	private List<String> getListOfFieldsNames(List<Field> fields) {
		List<String> names = new ArrayList<String>();
		for (Field field : fields) {
			if (field.getName() != "serialVersionUID") {
				names.add(field.getName());
			}
		}
		return names;
	}
}