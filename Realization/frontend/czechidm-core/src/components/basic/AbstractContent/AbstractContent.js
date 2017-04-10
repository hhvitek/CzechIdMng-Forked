import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import PageHeader from '../PageHeader/PageHeader';
import ContentHeader from '../ContentHeader/ContentHeader';
import Icon from '../Icon/Icon';
import { selectNavigationItems, selectNavigationItem, getNavigationItem } from '../../../redux/config/actions';

/**
* Basic content = page representation
* Requires store and router context
*
* @author Radek Tomiška
*/
export default class AbstractContent extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    if (this.getNavigationKey()) {
      this.selectNavigationItem(this.getNavigationKey());
    }
  }

  /**
   * select items in navigation by content / page
   *
   * @param  {array} selectedNavigationItems Array of selected navigation item. Can contains null values for select specified navigation level
   */
  selectNavigationItems(selectedNavigationItems) {
    this.context.store.dispatch(selectNavigationItems(selectedNavigationItems));
  }

  /**
   * select item in navigation by content / page id
   *
   * @param  {string} selectedNavigationItem id
   */
  selectNavigationItem(selectedNavigationItem) {
    this.context.store.dispatch(selectNavigationItem(selectedNavigationItem));
  }

  /**
   * Returns navigation for given id. If no navigationId is given, then returns navigation item by defined navigation key.
   *
   * @param  {string} navigationId
   * @return {object} navigationItem
   */
  getNavigationItem(navigationId = null) {
    if (!navigationId) {
      navigationId = this.getNavigationKey();
    }
    if (!navigationId) {
      return null;
    }
    return this.context.store.dispatch(getNavigationItem(this.context.store.getState().config, navigationId));
  }

  /**
   * @deprecated - use #selectNavigationItems
   * select item in sidebar by content / page
   *
   * @param  {string} selectedSidebarItem
   */
  selectSidebarItem(selectedSidebarItem) {
    this.context.store.dispatch(selectNavigationItems([null, selectedSidebarItem]));
  }

  /**
   * Return content identifier, with can be used in localization etc.
   *
   * @return {string} content identifier
   */
  getContentKey() {
    return null;
  }

  /**
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return this.getContentKey() || super.getComponentKey();
  }

  /**
   * Return navigation identifier, with can be used to show content header, title, icon ...
   *
   * @return {string} navigation item identifier
   */
  getNavigationKey() {
    return null;
  }

  /**
   * Default Page header with page title based on navigation item
   *
   * @param {object} props PageHeader properties e.g. style, className
   * @return {element} react element
   */
  renderPageHeader(props) {
    const navigationItem = this.getNavigationItem() || {};
    //
    return (
      <PageHeader {...props}>
        <Helmet title={this.i18n('title')} />
        <Icon value={navigationItem.icon}/>
        {' '}
        <span dangerouslySetInnerHTML={{__html: this.i18n('header')}}/>
      </PageHeader>
    );
  }

  /**
   * Default content header with title based on navigation item
   *
   * @param {object} props ContentHeader properties e.g. style, className
   * @return {element} react element
   */
  renderContentHeader(props) {
    const navigationItem = this.getNavigationItem() || {};
    //
    return (
      <ContentHeader {...props}>
        <Helmet title={this.i18n('title')} />
        <Icon value={navigationItem.icon}/>
        {' '}
        <span dangerouslySetInnerHTML={{__html: this.i18n('header')}}/>
      </ContentHeader>
    );
  }

  /**
   * Reloads current route
   */
  reloadRoute() {
    this.context.router.replace(this.context.store.getState().routing.location.pathname);
  }

  /**
   * Makes redirect to error pages or show given error.
   *
   * @param  {error} error from BE
   */
  handleError(error) {
    if (!error) {
      return;
    }
    //
    const message = {};
    if (error.statusCode === 403) {
      this.context.router.push('/error/403');
      message.hidden = true;
    } else if (error.statusCode === 404) {
      if (error.parameters && error.parameters.entity) {
        this.context.router.push(`/error/404?id=${error.parameters.entity}`);
        message.hidden = true;
      } else {
        this.context.router.push(`/error/404`);
        message.hidden = true;
      }
    }
    //
    this.addErrorMessage(message, error);
  }
}

AbstractContent.propTypes = {
  ...AbstractContextComponent.propTypes
};

AbstractContent.defaultProps = {
  ...AbstractContextComponent.defaultProps
};

AbstractContent.contextTypes = {
  ...AbstractContextComponent.contextTypes,
  router: PropTypes.object // .isRequired
};
