import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { NotificationTemplateManager, SecurityManager } from '../../../redux';

/**
* Basic detail for template detail,
* this detail is also used for create entity
*/

const manager = new NotificationTemplateManager();

export default class TemplateDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.notificationTemplate';
  }

  getNavigationKey() {
    return 'notification-templates';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.refs.form) {
      this.refs.code.focus();
    }
  }

  /**
  * Method check if props in this component is'nt different from new props.
  */
  componentWillReceiveProps(nextProps) {
    if (!this.props.entity || nextProps.entity.id !== this.props.entity.id || nextProps.entity.id !== this.refs.form.getData().id) {
      // this._initForm(nextProps.entity);
    }
  }

  _getIsNew() {
    return this.props.isNew;
  }

  /**
  * Method for basic initial form
  */
  _initForm(entity) {
    if (entity && this.refs.form) {
      const loadedEntity = _.merge({}, entity);
      //
      this.refs.form.setData(loadedEntity);
    }
  }

  /**
  * Default save method that catch save event from form.
  */
  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    },
    this.refs.form.processStarted());

    // get data from form
    const entity = this.refs.form.getData();

    if (entity.id === undefined) {
      this.context.store.dispatch(manager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(manager.patchEntity(entity, `${uiKey}-detail`, (savedEntity, error) => {
        this._afterSave(entity, error);
      }));
    }
  }

  /**
  * Method set showLoading to false and if is'nt error then show success message
  */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.refs.form.processEnded();
    this.setState({
      showLoading: false
    });
    this.context.router.replace('notification/templates/');
  }

  getParameters() {
    const { entity } = this.props;
    let components = {};
    if (entity.parameter) {
      const suggestions = [];
      const parameters = _.split(entity.parameter, ',');
      parameters.forEach( (parameter) => {
        suggestions.push({ text: _.trim(parameter), value: _.trim(parameter) });
      });
      components = {
        separator: ' ',
        trigger: '$',
        caseSensitive: true,
        mentionClassName: 'mention-className',
        dropdownClassName: 'dropdown-className',
        optionClassName: 'option-className',
        suggestions
      };
    }
    return components;
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm
            data={entity}
            ref="form"
            uiKey={uiKey}
            readOnly={ !SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'NOTIFICATIONTEMPLATE_CREATE' : 'NOTIFICATIONTEMPLATE_UPDATE') }
            style={{ padding: '15px 15px 0 15px' }}>
            <Basic.Row>
              <div className="col-lg-3">
                <Basic.TextField
                  ref="code" readOnly={entity.unmodifiable}
                  label={this.i18n('entity.NotificationTemplate.code')}
                  required
                  max={255}/>
              </div>
              <div className="col-lg-9">
                <Basic.TextField
                  ref="name"
                  label={this.i18n('entity.NotificationTemplate.name')}
                  required
                  max={255}/>
              </div>
            </Basic.Row>
            <Basic.TextField
              ref="parameter" readOnly={entity.unmodifiable} max={255}
              label={this.i18n('entity.NotificationTemplate.parameter.name')}
              helpBlock={this.i18n('entity.NotificationTemplate.parameter.help')} />
            <Basic.TextField
              ref="subject"
              label={this.i18n('entity.NotificationTemplate.subject')}
              required
              max={255}/>
            <Basic.Checkbox readOnly={entity.unmodifiable}
              ref="unmodifiable"
              label={this.i18n('entity.NotificationTemplate.unmodifiable.name')}
              helpBlock={this.i18n('entity.NotificationTemplate.unmodifiable.help')}/>
            <Basic.TextArea ref="bodyText" label={this.i18n('entity.NotificationTemplate.bodyText')} />
            {/* TODO: add two areas - text area for plain html and WYSIWYG editor for edit html tags */}
            <Basic.TextArea ref="bodyHtml" label={this.i18n('entity.NotificationTemplate.bodyHtml.name')} rows="20"/>
            {/*
            <Basic.TextArea ref="bodyHtml" label={this.i18n('entity.NotificationTemplate.bodyHtml.name')}
              showToolbar
              helpBlock={this.i18n('entity.NotificationTemplate.bodyHtml.help')}
              mentions={this.getParameters()}/>*/}
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'NOTIFICATIONTEMPLATE_CREATE' : 'NOTIFICATIONTEMPLATE_UPDATE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

TemplateDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  isNew: PropTypes.bool
};
TemplateDetail.defaultProps = {
};
