import FormableEntityManager from './FormableEntityManager';
import { ConceptRoleRequestService } from '../../services';

export default class ConceptRoleRequestManager extends FormableEntityManager {

  constructor() {
    super();
    this.service = new ConceptRoleRequestService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ConceptRoleRequest';
  }

  getCollectionType() {
    return 'conceptRoleRequests';
  }

  getEmbeddedOwner(concept) {
    return concept._embedded.identityContract
  }
}

ConceptRoleRequestManager.ENTITY_TYPE = "eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto";