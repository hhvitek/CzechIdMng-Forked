import * as Utils from '../utils';
import FormableEntityService from './FormableEntityService';
import RestApiService from './RestApiService';
import SearchParameters from '../domain/SearchParameters';
import PasswordPolicyService from './PasswordPolicyService';

const passwordPolicyService = new PasswordPolicyService();

/**
 * Identities endpoint
 *
 * @author Radek Tomiška
 */
class IdentityService extends FormableEntityService {

  getApiPath() {
    return '/identities';
  }

  supportsAuthorization() {
    return true;
  }

  supportsPatch() {
    return false;
  }

  getGroupPermission() {
    return 'IDENTITY';
  }

  getNiceLabel(entity) {
    let toString = '';
    if (entity) {
      if (!entity.lastName) {
        return entity.username;
      }
      toString += this.getFullName(entity);
      toString += (entity.username ? ` (${entity.username})` : '');
    }
    return toString;
  }

  getFullName(entity) {
    let toString = '';
    if (entity) {
      toString += (entity.titleBefore ? (entity.titleBefore + ' ') : '');
      toString += (entity.firstName ? (entity.firstName + ' ') : '');
      toString += (entity.lastName ? entity.lastName : '');
      toString += (entity.titleAfter ? `, ${entity.titleAfter}` : '');
    }
    return toString;
  }

  /**
   * Change users pasword in given systems (by json)
   *
   * @param username {string}
   * @param passwordChangeDto {object}
   * @param token {string} - if token has to be used (from public page is is not needed, then *false* could be given)
   * @return {Promise}
   */
  passwordChange(username, passwordChangeDto, token = null) {
    return RestApiService.put(RestApiService.getUrl(`/public${this.getApiPath()}/${encodeURIComponent(username)}/password-change`), passwordChangeDto, token);
  }

  /**
   * Generates password based on configured idm policies
   *
   * @return {Promise}
   */
  generatePassword() {
    return passwordPolicyService.generatePassword();
  }

  /**
   * Required idm password change for given user without login
   *
   * @param username {string}
   * @param oldPassword {string}
   * @param newPassword {string}
   * @return {Promise}
   */
  passwordMustChange(username, oldPassword, newPassword) {
    return RestApiService.put(this.getApiPath() + `/password/must-change`, {
      identity: username,
      oldPassword,
      newPassword
    });
  }

  /**
   * Get given user's roles
   *
   * @param username {string}
   * @param token {string}
   * @return {Promise}
   */
  getRoles(username, token = null) {
    return RestApiService
    .get(this.getApiPath() + `/${encodeURIComponent(username)}/roles`, token)
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
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('username');
  }

  /**
   * Deactivates given identity
   *
   * @param  {String} identity
   * @return {Promise}
   */
  deactivate(username) {
    return this.patchById(username, { disabled: true });
  }

  /**
   * Activates given identity
   *
   * @param  {String} identity
   * @return {Promise}
   */
  activate(username) {
    return this.patchById(username, { disabled: false });
  }

  /**
   * Returns identity accounts
   *
   * @param  {String} username
   * @return {Promise}
   */
  getAccounts(username) {
    return RestApiService.get(this.getApiPath() + `/${encodeURIComponent(username)}/accounts`);
  }

  /**
   * Get given identity's main position in organization
   *
   * @param username {string}
   * @param token {string}
   * @return {Promise}
   */
  getWorkPosition(username) {
    return RestApiService
    .get(this.getApiPath() + `/${encodeURIComponent(username)}/work-position`)
    .then(response => {
      if (response.status === 204) {
        // no work position was found
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

  getAuthorities(username) {
    return RestApiService
    .get(this.getApiPath() + `/${encodeURIComponent(username)}/authorities`)
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
   * Start workflow for change permissions
   * @param  id Identity id
   * @return Promise  task instance
   */
  changePermissions(id) {
    return RestApiService.put(this.getApiPath() + `/${encodeURIComponent(id)}/change-permissions`, null).then(response => {
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
}

export default IdentityService;
