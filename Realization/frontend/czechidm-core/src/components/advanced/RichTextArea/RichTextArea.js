import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Joi from 'joi';
import { Editor } from 'react-draft-wysiwyg';
import { EditorState, ContentState, convertFromHTML, DefaultDraftBlockRenderMap, getSafeBodyFromHTML } from 'draft-js';
import { stateToHTML } from 'draft-js-export-html';
//
import { withStyles } from '@material-ui/core/styles';
//
import * as Basic from '../../basic';
import FormComponentLabel from '../../basic/AbstractFormComponent/FormComponentLabel';

const styles = theme => ({
  root: {
    '& .basic-richtextarea': {
      borderRadius: theme.shape.borderRadius,
      borderWidth: 1,
      borderColor: theme.palette.type === 'light'
        ? 'rgba(0, 0, 0, 0.23)'
        : 'rgba(255, 255, 255, 0.23)', // ~ hardcoded somewhere in material text field
      // TODO: how to implement focus?
    }
  }
});

/**
 * Based on Draf.js and react draft wysiwyg editor
 * TODO: export to markdown and json
 * TODO: custom upload image
 * TODO: onFocus and onBlur.
 *
 * All potions for props can be found at
 * https://jpuri.github.io/react-draft-wysiwyg/#/docs
 *
 * @author Radek Tomiška
 * @author Ondřej Kopr
 */
class RichTextArea extends Basic.AbstractFormComponent {

  constructor(props) {
    super(props);
    const editorState = this.props.value ? this._createEditorState(this.props.value) : EditorState.createEmpty();
    this.state = {
      ...this.state,
      editorState,
      value: editorState.getCurrentContent().getPlainText(),
      activeControls: []
    };
  }

  getValidationDefinition(required, validation = null) {
    const { min, max } = this.props;
    let _validation = super.getValidationDefinition(min ? true : required, validation);

    if (min && max) {
      _validation = _validation.concat(Joi.string().min(min).max(max).disallow(''));
    } else if (min) {
      _validation = _validation.concat(Joi.string().min(min));
    } else if (max) {
      if (!required) {
        // if set only max is necessary to set allow null and empty string
        _validation = _validation.concat(Joi.string().max(max).allow(null).allow(''));
      } else {
        // if set prop required it must not be set allow null or empty string
        _validation = _validation.concat(Joi.string().max(max));
      }
    }

    return _validation;
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  _createEditorState(value) {
    if (!value) {
      return EditorState.createEmpty();
    }
    try {
      //
      // paragraph is block (need for rich - html conversions)
      const blockRenderMap = DefaultDraftBlockRenderMap.set('p', { element: 'p' });
      const blocksFromHTML = convertFromHTML(value, getSafeBodyFromHTML, blockRenderMap);
      // set converted content
      const state = ContentState.createFromBlockArray(blocksFromHTML);
      return EditorState.createWithContent(state);
    } catch (err) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.warn(`[RichTextArea]: value [${value}] will be rendered as plain text.`, err);
      } else {
        LOGGER.warn(`[RichTextArea]: value [${value}] will be rendered as plain text.`);
      }
      return EditorState.createWithContent(ContentState.createFromText(value));
    }
  }

  onChange(editorState) {
    let result = true;
    if (this.props.onChange) {
      result = this.props.onChange(editorState); // TODO: event value only?
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }

    this.setState({
      editorState,
      value: editorState.getCurrentContent().getPlainText()
    });
    //
    this.validate();
  }

  /**
   * Focus input field
   */
  focus() {
    // TODO: focus doesn't work / see react-draft-wysiwyg doc
    // this.refs.input.focus();
  }

  setValue(value) {
    const editorState = this._createEditorState(value);
    this.setState({
      editorState,
      value: editorState.getCurrentContent().getPlainText()
    });
  }

  _getToolbar() {
    const { toolbarOptions, fontSizeOptions, fontFamilyOptions } = this.props;
    const toolbar = {
      options: toolbarOptions,
      fontSize: fontSizeOptions,
      fontFamily: fontFamilyOptions
    };
    return toolbar;
  }

  /**
   * Returns filled value as html
   *
   * TODO: markdown, raw js?
   *
   * @return {string} html
   */
  getValue() {
    const { editorState } = this.state;
    //
    try {
      return stateToHTML(editorState.getCurrentContent());
    } catch (err) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.warn(`[RichTextArea]: editorState will be returned as plain text.`, err);
      } else {
        LOGGER.warn(`[RichTextArea]: editorState will be returned as plain text.`);
      }
      return editorState.getCurrentContent().getPlainText();
    }
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, placeholder, style, required, showToolbar, mentions, classes } = this.props;
    const { editorState, disabled, readOnly } = this.state;
    const labelClassName = classNames(
      labelSpan
    );
    const containerClassName = classNames(
      'basic-richtextarea',
      { readOnly: readOnly === true}
    );
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
          { 'has-feedback': !!feedback },
          { 'has-error': !!feedback },
          { disabled: disabled || readOnly },
          classes ? classes.root : null
        )
      }>
        <FormComponentLabel
          className={ labelClassName }
          label={ _label }
          readOnly={disabled || readOnly}
          helpIcon={ this.renderHelpIcon() }/>
        <div className={componentSpan}>
          <Basic.Tooltip ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
            <div className={ containerClassName }>
              <Editor
                ref="input"
                readOnly={readOnly}
                placeholder={placeholder}
                toolbarOnFocus={!showToolbar}
                editorState={editorState}
                onEditorStateChange={this.onChange}
                className="form-control"
                mention={mentions}
                style={style}
                toolbar={this._getToolbar()}
                spellCheck/>
              { feedback }
            </div>
          </Basic.Tooltip>
          { _label.length === 0 ? this.renderHelpIcon() : null }
        </div>
        { this.renderHelpBlock() }
      </div>
    );
  }
}

RichTextArea.propTypes = {
  ...Basic.AbstractFormComponent.propTypes,
  value: PropTypes.string,
  placeholder: PropTypes.string,
  min: PropTypes.number,
  max: PropTypes.number,
  showToolbar: PropTypes.bool,
  mentions: PropTypes.object,
  toolbarOptions: PropTypes.arrayOf(PropTypes.string),
  fontSizeOptions: PropTypes.object,
  fontFamilyOptions: PropTypes.object
};

RichTextArea.defaultProps = {
  ...Basic.AbstractFormComponent.defaultProps,
  showToolbar: false,
  toolbarOptions: [
    'inline', 'blockType', 'list', 'emoji', 'remove', 'history', 'link'
  ], // + order of component  ; remove: 'image', 'embedded', 'fontSize', 'fontFamily', 'colorPicker', 'textAlign',
  fontSizeOptions: {
    options: [7, 8, 9, 10, 11, 12, 14, 18, 24, 30, 36, 48, 60, 72, 96],
    className: undefined,
    dropdownClassName: undefined,
  },
  fontFamilyOptions: {
    options: ['Arial', 'Georgia', 'Impact', 'Tahoma', 'Times New Roman', 'Verdana'],
    className: undefined,
    dropdownClassName: undefined,
  },
};

export default withStyles(styles, { withTheme: true })(RichTextArea);
