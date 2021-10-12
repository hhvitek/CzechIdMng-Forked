import React from 'react';
import Recaptcha from 'react-google-recaptcha';
import classNames from 'classnames';
//
import * as Basic from '../../basic';
import { RecaptchaService } from '../../../services';
import ConfigLoader from '../../../utils/ConfigLoader';

const recaptchaService = new RecaptchaService();

/**
 * Google ReCaptcha compotent. It is a bot protection.
 *
 * @author Filip Mestanek
 * @author Radek Tomiška
 */
export default class AdvancedRecaptcha extends Basic.AbstractFormComponent {

  constructor(props) {
    super(props);
    // load config properties
    const enabled = ConfigLoader.getConfig('recaptcha.enabled') === true;
    const siteKey = ConfigLoader.getConfig('recaptcha.siteKey');
    this.state = {
      captchaValid: !enabled,
      rendered: enabled,
      siteKey
    };
    //
    if (siteKey === null && enabled) {
      this.getLogger().error('[Recaptcha]: The configuration property "recaptcha.siteKey" not defined!');
    }
  }

  getComponentKey() {
    return 'component.advanced.Recaptcha';
  }

  isValid() {
    return this.state.captchaValid;
  }

  validate(showValidationError) {
    const showValidations = showValidationError != null ? showValidationError : true;
    if (!this.isValid()) {
      this.setState({
        validationResult: {
          status: 'error',
          class: 'has-error has-feedback',
          isValid: false,
          message: this.i18n('required')
        },
        showValidationError: showValidations
      });
      return false;
    }
    return true;
  }

  /**
   * Called, when the state has changed.
   *
   * @param  {string} value Id of the request generated by Google. If null,
   *                        the ReCaptcha validation has expired.
   */
  recaptchaChange(value) {
    if (value === null) {
      this.setState({
        captchaValid: false
      });
      return;
    }
    //
    recaptchaService.checkResponse(value)
      .then(result => {
        this.setState({
          captchaValid: result.success
        });
      })
      .catch(error => {
        this.addError(error);
      });
  }

  getBody(feedback) {
    const { label, placeholder, required } = this.props;
    const { rendered, siteKey, disabled, readOnly } = this.state;
    const labelClassName = classNames('control-label');
    //
    if (!rendered || !siteKey) {
      return null;
    }
    //
    const _label = [];
    if (label) {
      _label.push(label);
    } else if (placeholder) {
      _label.push(placeholder);
    }
    if (_label.length > 0 && required) {
      _label.push(' *');
    }
    //
    return (
      <div className={
        classNames(
          'basic-form-component',
          { 'has-feedback': feedback },
          { disabled: disabled || readOnly }
        )
      }>
        {
          _label.length === 0
          ||
          <label
            className={ labelClassName }>
            { _label }
            { this.renderHelpIcon() }
          </label>
        }
        <div style={{ whiteSpace: 'nowrap' }}>
          <Basic.Tooltip ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
            <span>
              <Recaptcha
                ref="recaptcha"
                sitekey={ siteKey }
                onChange={ this.recaptchaChange.bind(this) }
                className="advanced-recaptcha"
              />
            </span>
          </Basic.Tooltip>
          { _label.length === 0 ? this.renderHelpIcon() : null }
          { this.renderHelpBlock() }
        </div>
      </div>
    );
  }
}

AdvancedRecaptcha.propTypes = {
  ...Basic.AbstractFormComponent.propTypes
};

AdvancedRecaptcha.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps
};
