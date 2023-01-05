import { Services, Domain, Utils} from 'czechidm-core';
import WizardService from './WizardService';

/**
 * Target systems
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @author Peter Štrunc
 */
class SystemService extends WizardService {

  getApiPath() {
    return '/systems';
  }

  getNiceLabel(system) {
    if (!system) {
      return '';
    }
    return system.name;
  }

  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'SYSTEM';
  }

  supportsAuthorization() {
    return false;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  generateSchema(id) {
    return Services.RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/generate-schema`, null)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  duplicate(id) {
    return Services.RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/duplicate`, null)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns connector form definition to given system
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getConnectorFormDefinition(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/connector-form-definition`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns filled connector configuration
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getConnectorFormValues(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/connector-form-values`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }


  /**
   * Returns pooling connector form definition to given system
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getPoolingConnectorFormDefinition(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/pooling-connector-form-definition`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns filled connector configuration
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getPoolingConnectorFormValues(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/pooling-connector-form-values`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns operation options connector form definition to given system
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getOperationOptionsConnectorFormDefinition(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/operation-options-connector-form-definition`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns filled connector configuration for operation options
   * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
   *
   * @param  {string} id system identifier
   * @return {promise}
   */
  getOperationOptionsConnectorFormValues(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/operation-options-connector-form-values`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveConnectorFormValues(id, values) {
    return Services.RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/connector-form-values`, values)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves pooling connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  savePoolingConnectorFormValues(id, values) {
    return Services.RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/pooling-connector-form-values`, values)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Saves operation-options connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveOperationOptionsConnectorFormValues(id, values) {
    return Services.RestApiService
      .post(`${ this.getApiPath() }/${ encodeURIComponent(id) }/operation-options-connector-form-values`, values)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }


  checkSystem(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/check`, null)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns all available connectors
   *
   * @return {promise}
   */
  getAvailableConnectors() {
    // TODO: filter etc.
    return Services.RestApiService
      .get(Services.RestApiService.getUrl(`${ this.getApiPath() }/search/local`))
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Returns available remote connector for system id, connector server is part of sy
   */
  getAvailableRemoteConnectors(systemId) {
    return Services.RestApiService
      .get(Services.RestApiService.getUrl(`${ this.getApiPath() }/${ encodeURIComponent(systemId) }/search/remote`))
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  sendLongPollingRequest(systemId, signal) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(systemId) }/check-running-sync`, null, signal)
      .then(response => response.json())
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

}

export default SystemService;
