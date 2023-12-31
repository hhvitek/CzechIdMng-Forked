import PropTypes from 'prop-types';
//
import { withStyles } from '@material-ui/core/styles';
//
import { SelectBox } from '../SelectBox/SelectBox';
import { EnumSelectBox } from '../EnumSelectBox/EnumSelectBox';

/**
 * Select boolean value
 *
 * Warning: string representation is needed (false value not work as selected value for react-select clearable functionality)
 *
 * @author Radek Tomiška
 */
export class BooleanSelectBox extends EnumSelectBox {

  /**
   * Default options from localization
   *
   * @return {arrayOf(object)}
   */
  getDefaultOptions() {
    return [
      // Warning: string representation is needed (false value not work as selected value for react-select clearable functionality)
      { value: 'true', niceLabel: this.i18n('label.yes') },
      { value: 'false', niceLabel: this.i18n('label.no') }
    ];
  }

  getOptions() {
    const options = this.props.options || this.getDefaultOptions();
    const results = [];
    for (const item in options) {
      if (!options.hasOwnProperty(item)) {
        continue;
      }
      results.push(this.itemRenderer(options[item]));
    }
    // add empty option at start
    if (this.props.clearable && results.length > 0) {
      const emptyOption = this.getEmptyOption(this.props.emptyOptionLabel);
      if (emptyOption) {
        results.unshift(emptyOption);
      }
    }
    return results;
  }

  _findNiceLabel(value) {
    if (!value) {
      return null;
    }
    const options = this.props.options || this.getDefaultOptions();
    for (const item in options) {
      if (options[item].value === value) {
        return options[item].niceLabel;
      }
    }
    return null;
  }
}

BooleanSelectBox.propTypes = {
  ...EnumSelectBox.propTypes,
  value: PropTypes.oneOfType([PropTypes.object, PropTypes.string])
};
BooleanSelectBox.defaultProps = {
  ...EnumSelectBox.defaultProps
};

export default withStyles(SelectBox.STYLES, { withTheme: true })(BooleanSelectBox);
