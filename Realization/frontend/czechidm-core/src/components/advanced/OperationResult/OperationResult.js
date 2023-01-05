import React from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";
//
import * as Basic from "../../basic";
import OperationStateEnum from "../../../enums/OperationStateEnum";
import { AttachmentService } from "../../../services";
import OperationResultDownloadButton from "./OperationResultDownloadButton";

/**
 * Operation result component - shows enum value and result code with flash message
 *
 * @author Radek Tomiška
 * @author Patrik Stloukal
 *
 */
export default class OperationResult extends Basic.AbstractContextComponent {
  constructor(props, context) {
    super(props, context);
    this.attachmentService = new AttachmentService();
    this.state = { toggle: true };
  }

  getComponentKey() {
    return "component.advanced.OperationResult";
  }

  _getLevel(level, message) {
    if (level) {
      return level;
    }
    if (!message) {
      return null;
    }
    return message.level;
  }

  /**
   * Renders popover info card
   */
  _renderPopover() {
    const { value, stateLabel, level } = this.props;
    const message = this.getFlashManager().convertFromResultModel(value.model);
    const _level = this._getLevel(level, message);
    //
    return (
      <Basic.Div style={{ whiteSpace: "nowrap" }}>
        <Basic.EnumValue
          level={_level}
          value={value.state}
          enum={OperationStateEnum}
          label={
            stateLabel ||
            (message && message.level === "info" && value.state === "EXCEPTION"
              ? this.i18n("label.warning")
              : null)
          }
        />
        <Basic.Popover
          ref="popover"
          trigger={["click"]}
          value={this._getPopoverContent()}
          className="abstract-entity-info-popover"
          rendered={value.code !== null && value.code !== undefined}
        >
          <Basic.Button
            level="info"
            buttonSize="xs"
            style={{ padding: 0, marginLeft: 3 }}
            title={this.i18n("link.popover.title")}
            icon="fa:info-circle"
          />
        </Basic.Popover>
      </Basic.Div>
    );
  }

  /**
   * Returns popover info content
   *
   * @param  {object} result
   */
  _getPopoverContent() {
    const { value, detailLink } = this.props;
    //
    const linkLabel = (
      <span>
        <Basic.Icon value="fa:angle-double-right" />{" "}
        {this.i18n("link.detail.label")}
      </span>
    );
    //

    return (
      <Basic.Panel>
        <Basic.PanelBody style={{ padding: 2 }}>
          <Basic.FlashMessage
            message={this.getFlashManager().convertFromResultModel(value.model)}
            style={{ wordWrap: "break-word", margin: 0 }}
          />
        </Basic.PanelBody>
        {/* RT: hidden - show as collapse, tooltip instead? */}
        <Basic.PanelBody
          rendered={value.model !== null}
          style={{ display: "none" }}
        >
          {this.i18n("result.code")}
          {": "}
          {value.code}
        </Basic.PanelBody>
        {!detailLink || (
          <Basic.PanelFooter>
            {typeof detailLink === "function" ? (
              <a href="#" onClick={this._showDetail.bind(this)}>
                {linkLabel}
              </a>
            ) : (
              <Link to={detailLink}>{linkLabel}</Link>
            )}
          </Basic.PanelFooter>
        )}
      </Basic.Panel>
    );
  }

  _showDetail(event) {
    if (event) {
      event.preventDefault();
    }
    //
    // close popover
    this.refs.popover.close();
    // use link function
    this.props.detailLink();
  }
  // Wrap button
  onWrap = () => {
    this.setState({ toggle: !this.state.toggle });
  };
  // Copy button
  onCopy = (i) => {
    navigator.clipboard.writeText(i.stackTrace);
    this.addMessage({
      level: "success",
      message: this.i18n(`textCopy`),
    });
  };

  /**
   * Renders full info card (with exception stacktrace etc.)
   */
  _renderFull() {
    const {
      value,
      stateLabel,
      level,
      header,
      downloadLinkPrefix,
      downloadLinkSuffix,
    } = this.props;
    const message = this.getFlashManager().convertFromResultModel(value.model);
    const _level = this._getLevel(level, message);
    //

    return (
      <Basic.Div>
        <Basic.ContentHeader
          text={header === null ? this.i18n("result.header") : header}
        />

        <Basic.Div style={{ marginBottom: 15 }}>
          <Basic.Div style={{ float: "left" }}>
            <Basic.EnumValue
              level={_level}
              value={value.state}
              enum={OperationStateEnum}
              label={
                stateLabel ||
                (message &&
                message.level === "info" &&
                value.state === "EXCEPTION"
                  ? this.i18n("label.warning")
                  : null)
              }
            />
          </Basic.Div>
          {!value || !value.code || (
            <Basic.Div
              style={{
                marginLeft: value.state ? 15 : 0,
                fontStyle: "italic",
                float: "right",
              }}
            >
              {`${this.i18n("result.code")}: ${value.code}`}
            </Basic.Div>
          )}
          <Basic.Div className="clearfix" />
          <Basic.FlashMessage
            message={message}
            style={{ margin: "15px 0 0 0" }}
          />
          <OperationResultDownloadButton
            style={{ marginTop: "15px" }}
            downloadLinkPrefix={downloadLinkPrefix}
            downloadLinkSuffix={downloadLinkSuffix}
            operationResult={value}
          />
        </Basic.Div>
        {!value || !value.stackTrace || (
          <Basic.Div>
            <Basic.Button
              title={this.i18n("copyClipboard")}
              className="customButton"
              icon="fa:copy"
              onClick={this.onCopy.bind(this, value)}
            ></Basic.Button>
            <Basic.Button
              title={this.i18n(this.state.toggle ? "wrapOff" : "wrapOn")}
              className="customButton"
              onClick={this.onWrap.bind(this)}
              icon={this.state.toggle ? "fa:angle-left" : "fa:angle-right"}
            ></Basic.Button>

            <Basic.TextArea
              rows="10"
              value={value.stackTrace}
              level="success"
              notEdit
              className={
                this.state.toggle ? "customTextAre" : "customTextAreNot"
              }
            ></Basic.TextArea>
          </Basic.Div>
        )}
      </Basic.Div>
    );
  }

  render() {
    const { value, face, rendered, showLoading } = this.props;
    if (!rendered || !value) {
      return null;
    }
    if (showLoading) {
      return <Basic.Icon value="refresh" showLoading />;
    }
    //
    switch (face) {
      case "popover": {
        return this._renderPopover();
      }
      default: {
        return this._renderFull();
      }
    }
  }
}

OperationResult.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * OperationResult object
   * @type {OperationResult}
   */
  value: PropTypes.object,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(["popover", "full"]),
  /**
   * Custom label
   */
  stateLabel: PropTypes.string,
  /**
   * Label level / css / class - override level from given message.
   *
   * @since 11.2.0
   */
  level: PropTypes.oneOf([
    "default",
    "success",
    "warning",
    "info",
    "danger",
    "error",
    "primary",
  ]),
  /**
   * link to detail
   */
  detailLink: PropTypes.oneOfType([PropTypes.string, PropTypes.func]),
  /**
   * Header text
   */
  header: PropTypes.string,
  /**
   * Download link prefix for specific download url.
   * When is download link prefix null classic download url from attachment controller
   * will be used. Eq.: /attachments/{$attachmentId}/download
   */
  downloadLinkPrefix: PropTypes.string,
  /**
   * Download link prefix for specific download url. Suffix can be used only with
   * prefix. Cant be used itself.
   */
  downloadLinkSuffix: PropTypes.string,
};
OperationResult.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  value: null,
  face: "popover",
  stateLabel: null,
  rendered: true,
  detailLink: null,
  header: null, // default text from component locale will be used
  downloadLinkPrefix: null,
  downloadLinkSuffix: null,
  level: null,
};

OperationResult.PARTIAL_CONTENT_STATUS = 206;
