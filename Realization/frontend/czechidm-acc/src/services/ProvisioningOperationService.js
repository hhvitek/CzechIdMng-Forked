import _ from 'lodash';
//
import { Services, Domain, Utils } from 'czechidm-core';
import SystemEntityTypeService from './SystemEntityTypeService';

/**
 * Active provisioning operations in the queue.
 *
 * @author Radek Tomiška
 */
export default class ProvisioningOperationService extends Services.AbstractService {

  constructor() {
    super();
    this.systemEntityTypeService = new SystemEntityTypeService();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${(entity._embedded && entity._embedded.system) ? entity._embedded.system.name : entity.system}:${this.systemEntityTypeService.getNiceLabelForEntityType(entity)}:${entity.systemEntityUid}`;
  }

  getApiPath() {
    return '/provisioning-operations';
  }

  getGroupPermission() {
    return 'PROVISIONINGOPERATION';
  }

  supportsBulkAction() {
    return true;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('created', 'DESC');
  }

  /**
   * Delete all provisioning operation from queue for the given system identifier.
   * If system identifier will be null. Delete all provisioning operation for all
   * system.
   *
   * @param  {string} system identifier
   * @return {Promise}
   */
  deleteAll(systemId = null) {
    let deleteUrl = '/action/bulk/delete';
    if (!_.isNil(systemId)) {
      deleteUrl = `${ deleteUrl }?system=${ encodeURIComponent(systemId) }`;
    }
    return Services.RestApiService
      .delete(Services.RestApiService.getUrl(this.getApiPath() + deleteUrl))
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        if (response.status === 204) {
          return {};
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
   * Obtains provisioning values with operation type
   *
   * @param  {[type]} id Provisioning operation item id
   * @return {[type]}
   */
  getDecoratedDifferenceObject(id) {
    return Services.RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/difference-object`)
      .then(response => {
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
