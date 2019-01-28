import React from 'react';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { faCircle } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Icon for the business role. It's combined from different icons - layers are used.
 * - https://fontawesome.com/how-to-use/on-the-web/styling/layering
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class BusinessRoleIcon extends AbstractIcon {

  constructor(props) {
    super(props);
  }

  renderIcon() {
    return (
      <span className="fa-layers fa-fw">
        <FontAwesomeIcon icon={ faKey } transform="rotate-315 up-1 right-0.3" style={{ color: '#ccc' }} />
        <FontAwesomeIcon icon={ faKey } transform="up-3.2 right--3"/>
        <FontAwesomeIcon icon={ faCircle } transform="up-9 right-5 shrink-6"/>
      </span>
    );
  }
}