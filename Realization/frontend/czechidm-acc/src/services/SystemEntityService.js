import { Services, Domain, Utils } from 'czechidm-core';
//
import SystemEntityTypeService from './SystemEntityTypeService';

/**
 * @author Svanda
 */
export default class SystemEntityService extends Services.AbstractService {

  constructor() {
    super();
    this.systemEntityTypeService = new SystemEntityTypeService();
  }

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity._embedded.system.name}:${this.systemEntityTypeService.getNiceLabelForEntityType(entity)}:${entity.uid}`;
  }

  getApiPath() {
    return '/system-entities';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('uid');
  }

  /**
  * Get connector object by given system entity. Call directly connector.
  */
  getConnectorObject(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/connector-object`)
      .then(response => {
        if (!response) {
          return null;
        }
        if (response.status === 204) {
          return null;
        }
        return response.json();
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        if (Utils.Response.hasInfo(jsonResponse)) {
          throw Utils.Response.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }
}
