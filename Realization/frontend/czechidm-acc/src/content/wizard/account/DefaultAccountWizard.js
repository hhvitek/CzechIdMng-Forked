import { Basic, Advanced, Managers, Domain } from 'czechidm-core';
import React from 'react';
import _ from 'lodash';
import { AccountManager, SystemManager, SystemMappingManager } from '../../../redux';

import Accordion from '@material-ui/core/Accordion';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

const accountManager = new AccountManager();
const systemManager = new SystemManager();
const systemMappingManager = new SystemMappingManager();
const identityManager = new Managers.IdentityManager();

/**
 * Wizard for create account
 *
 * @author Roman Kucera
 */
export default class DefaultAccountWizard extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    const { connectorType } = props;
    const wizardContext = context.wizardContext;
    if (wizardContext && connectorType) {
      wizardContext.connectorType = connectorType;
    }
    this.state = {
      expanded: 'panel1'
    };
  }

  getWizardId() {
    return 'create-account';
  }

  getModule() {
    return 'acc';
  }

  getApiPath() {
    return `/connector-types/${this.getWizardId()}`;
  }

  /**
   * Returns current wizard steps. Steps are changing dynamically.
   * It means this method is calls in every render of the wizard component.
   */
  getWizardSteps() {
    const context = this.context;
    let activeStep = null;
    if (context && context.wizardContext) {
      activeStep = context.wizardContext.activeStep;
    }

    // New system detail step
    const stepNewAccount = this.getFirstStep();
    const stepAttributes = this.getSecondStep();
    const stepRecapitulation = this.getRecapilutationStep();
    // this is last step, with congratulation
    const stepSummary = this.getSummaryStep();

    return [stepNewAccount, stepAttributes, stepRecapitulation, stepSummary];
  }

  getFirstStep() {
    const { systemId } = this.state;
    const { connectorType, accountType } = this.props;
    let forceSearchMappings;
    if (accountType) {
      forceSearchMappings = new Domain.SearchParameters()
        .setFilter('systemId', systemId || Domain.SearchParameters.BLANK_UUID)
        .setFilter('operationType', 'PROVISIONING')
        .setFilter('accountType', accountType);
    } else {
      forceSearchMappings = new Domain.SearchParameters()
        .setFilter('systemId', systemId || Domain.SearchParameters.BLANK_UUID)
        .setFilter('operationType', 'PROVISIONING');
    }

    return {
      id: "accountNew",
      wizardId: this.getWizardId(),
      label: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.one.name`),
      help: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.one.help`),
      getComponent: () => {
        return (
          <Basic.Div>
            <Basic.AbstractForm
              ref="form">
              <Basic.SelectBox
                ref="system"
                manager={systemManager}
                label={this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.one.system`)}
                onChange={this.onSystemChange.bind(this)}
                required />
              <Basic.SelectBox
                ref="systemMapping"
                manager={systemMappingManager}
                forceSearchParameters={forceSearchMappings}
                label={this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.one.type`)}
                onChange={this.onSystemMappingChange.bind(this)}
                required
              />
              <Basic.SelectBox
                ref="owner"
                manager={identityManager}
                label={this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.one.owner`)}
                onChange={this.onOwnerChange.bind(this)}
                required
              />
            </Basic.AbstractForm>
          </Basic.Div>
        );
      },
      // Good method for execute a step on the backend (if is defined).
      // For example set connector info after new system is created.
      getExecuteConnectorType: (cb) => {
        if (!this.refs.wizard.refs.form.isFormValid()) {
          return;
        }

        const wizardContext = this.context.wizardContext;
        const { connectorType } = this.props;
        const _connectorType = _.merge({}, connectorType);
        _connectorType.wizardStepName = 'accountNew';
        _connectorType.metadata.system = wizardContext.system ? wizardContext.system.id : null;
        _connectorType.metadata.systemMapping = wizardContext.systemMapping ? wizardContext.systemMapping.id : null;
        _connectorType.metadata.owner = wizardContext.owner ? wizardContext.owner.id : null;

        this.setState({
          showLoading: true
        }, () => {
          accountManager.getService().executeConnectorType(_connectorType)
            .then((json) => {
              wizardContext.connectorType = json;
              this.setState({
                showLoading: false
              }, () => {
                if (cb) {
                  cb();
                }
              });
            }).catch(ex => {
              this.setState({
                showLoading: false
              });
              this.addError(ex);
            });
        });
      }
    };
  }

  onSystemChange(system) {
    const context = this.context;
    const systemId = system ? system.id : null;
    this.setState({
      systemId: systemId,
    }, () => {
      this.refs.wizard.refs.systemMapping.setValue(null);
      context.wizardContext.system = system;
    });
  }

  onSystemMappingChange(mapping) {
    const context = this.context;
    context.wizardContext.systemMapping = mapping;
  }

  onOwnerChange(owner) {
    const context = this.context;
    context.wizardContext.owner = owner;
  }

  /**
   * Second step with attributes of account
   */
  getSecondStep() {
    const wizardContext = this.context.wizardContext;
    let formInstance;
    if (wizardContext && wizardContext.connectorType && wizardContext.connectorType.formDefinition) {
      formInstance = new Domain.FormInstance(wizardContext.connectorType.formDefinition, wizardContext.connectorType.values);
    }
    const { connectorType } = this.props;
    return {
      id: "accountAttributes",
      wizardId: this.getWizardId(),
      label: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.two.name`),
      help: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.two.help`),
      getComponent: () => {
        const { expanded } = this.state;
        return (
          <Basic.Div>
            <Basic.AbstractForm
              ref="form1">
              <Accordion expanded={expanded === 'panel1'} onChange={this.handleChange.bind(this, 'panel1')}>
                <AccordionSummary
                  expandIcon={<ExpandMoreIcon />}
                  aria-controls="panel1bh-content"
                  id="panel1bh-header"
                >
                  <Typography>{this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.two.basic-attr`)}</Typography>
                </AccordionSummary>
                <AccordionDetails style={{display: 'inherit'}}>
                  <Typography>
                    {formInstance
                      ? <Advanced.EavForm
                        ref="attributes"
                        formInstance={formInstance} />
                      : null}
                  </Typography>
                </AccordionDetails>
              </Accordion>
            </Basic.AbstractForm>
          </Basic.Div>
        );
      },
      getExecuteConnectorType: (cb) => {
        if (!this.refs.wizard.refs.attributes.isValid()) {
          return;
        }

        const wizardContext = this.context.wizardContext;
        if (this?.refs?.wizard?.refs?.attributes) {
          // compare filled values with default ones. We want to save to EAV only values which were filled by user (exclude default ones)
          const filledValues = new Map();
          const defaultValues = new Map();
          const valuesToSave = new Map();

          this.refs.wizard.refs.attributes.getValues().forEach(value => {
            if (filledValues.has(value.formAttribute)) {
              const values = filledValues.get(value.formAttribute);
              values.push(value);
              filledValues.set(value.formAttribute, values);
            } else {
              const values = [value];
              filledValues.set(value.formAttribute, values);
            }
          })

          wizardContext.connectorType.values.forEach(value => {
            if (defaultValues.has(value.formAttribute)) {
              const values = defaultValues.get(value.formAttribute);
              values.push(value);
              defaultValues.set(value.formAttribute, values);
            } else {
              const values = [value];
              defaultValues.set(value.formAttribute, values);
            }
          })

          filledValues.forEach((value, key) => {
            if (defaultValues.has(key)) {
              // compare each value
              if (defaultValues.get(key).length === value.length) {
                for (let index = 0; index < value.length; index++) {
                  if (defaultValues.get(key)[index].value !== value[index].value) {
                    valuesToSave.set(value[index].formAttribute, value);
                  }
                }
              } else {
                // arrays have diffrent size, something changed
                valuesToSave.set(value[0].formAttribute, value);
              }
            } else {
              valuesToSave.set(value[0].formAttribute, value);
            }
          });

          wizardContext.connectorType.values = this.refs.wizard.refs.attributes.getValues();

          let valuesToSaveArray = [];
          valuesToSave.forEach(value => {
            valuesToSaveArray = valuesToSaveArray.concat(value);
          });

          this.setState({
            valuesToSave: valuesToSaveArray
          });
        }
        wizardContext.connectorType.wizardStepName = "accountAttributes";
        cb();
      }
    };
  }

  handleChange(toBeExpanded) {
    const { expanded } = this.state;
    if (toBeExpanded === expanded) {
      this.setState({
        expanded: '',
      });
    } else {
      this.setState({
        expanded: toBeExpanded,
      });
    }
  }

  getRecapilutationStep() {
    const wizardContext = this.context.wizardContext;
    let formInstance;
    if (wizardContext?.connectorType?.values && wizardContext?.connectorType?.formDefinition) {
      formInstance = new Domain.FormInstance(wizardContext.connectorType.formDefinition, wizardContext.connectorType.values);
    }
    const { connectorType } = this.props;
    return {
      id: "accountRecapitulation",
      wizardId: this.getWizardId(),
      label: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.three.name`),
      help: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.three.help`),
      getComponent: () => {
        const { expanded } = this.state;
        return (
          <div>
            <Accordion expanded={expanded === 'panel1'} onChange={this.handleChange.bind(this, 'panel1')}>
              <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls="panel1bh-content"
                id="panel1bh-header"
              >
                <Typography>{this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.three.basic-attr`)}</Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Typography>
                  {formInstance
                    ? <Advanced.EavForm
                      ref="attributesRecap"
                      formInstance={formInstance}
                      readOnly
                      condensed />
                    : null}
                </Typography>
              </AccordionDetails>
            </Accordion>
          </div>
        );
      },
      // Good method for execute a step on the backend (if is defined).
      // For example set connector info after new system is created.
      getExecuteConnectorType: (cb) => {
        const { valuesToSave } = this.state;
        const wizardContext = this.context.wizardContext;
        wizardContext.connectorType.wizardStepName = "accountRecapitulation";
        // put only the values which were overriden by user
        const connectorTypeToSend = _.cloneDeep(wizardContext.connectorType);
        connectorTypeToSend.values = valuesToSave;
        this.setState({
          showLoading: true
        }, () => {
          accountManager.getService().executeConnectorType(connectorTypeToSend)
            .then((json) => {
              wizardContext.connectorType = json;
              this.setState({
                showLoading: false
              }, () => {
                if (cb) {
                  cb();
                }
              });
            }).catch(ex => {
              this.setState({
                showLoading: false
              });
              this.addError(ex);
            });
        });
      }
    };
  }

  /**
   * Final summary step.
   */
  getSummaryStep() {
    const { connectorType } = this.props;
    return {
      id: 'summary',
      wizardId: this.getWizardId(),
      label: this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.four.name`),
      getComponent: () => {
        return (
          <SummaryStep
            match={this.props.match}
            reopened={this.props.reopened}
            wizardStepId="summary"
            connectorType={this.props.connectorType}
          />
        );
      }
    };
  }

  _buildStepProps(stepId, wizardContext) {
    return {
      match: { ...this.props.match, params: { entityId: wizardContext.entity ? wizardContext.entity.id : null } },
      location: this.props.location,
      entity: wizardContext.entity,
      wizardStepId: stepId
    };
  }

  /**
   * Button for show detail (show system).
   */
  onShowDetailBtn() {
    return !!this.props.reopened;
  }

  _getWizardLabel(connectorName) {
    let label;
    const locKey = `wizard.${connectorName}.name`;
    label = this.i18n(`${this.getModule()}:${locKey}`);
    if (label === locKey) {
      label = null;
    }
    return label;
  }

  render() {
    const { show, modal, connectorType } = this.props;
    const { showLoading } = this.state;
    let wizardName;
    if (connectorType
      && connectorType._embedded
      && connectorType._embedded.system) {
      wizardName = connectorType._embedded.system.name;
    }
    if (!wizardName) {
      wizardName = this._getWizardLabel(connectorType.name);
    }
    return (
      <Basic.Wizard
        ref="wizard"
        getSteps={this.getWizardSteps.bind(this)}
        modal={modal}
        name={wizardName}
        showLoading={showLoading}
        show={show}
        icon={connectorType ? connectorType.iconKey : null}
        module={this.getModule()}
        id={this.getWizardId()}
        onCloseWizard={this.props.closeWizard}
        onShowDetailBtn={this.onShowDetailBtn.bind(this)} />
    );
  }
}

/**
 * The last step in a system wizard.
 */
class SummaryStep extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    const wizardContext = context.wizardContext;
    this.state = {
      showLoading: false
    };
    // If context contains connectorType, then we will used it.
    if (wizardContext && wizardContext.connectorType) {
      this.state.connectorType = wizardContext.connectorType;
    }
  }

  wizardNext() {
    this.setState({
      showLoading: true
    });

    this.setState({
      showLoading: false
    }, () => {
      const wizardContext = this.context.wizardContext;
      if (wizardContext.callBackNext) {
        wizardContext.callBackNext();
      } else if (wizardContext.onClickNext) {
        wizardContext.onClickNext(false, true);
      }
    });
  }

  render() {
    const { showLoading } = this.state;
    const { connectorType } = this.props;

    return (
      <Basic.Div style={{ marginTop: 15 }} showLoading={showLoading}>
        <Basic.Row>
          <Basic.Col lg={1} md={1} />
          <Basic.Col lg={10} md={10}>
            <Basic.Alert
              level="success"
              icon="ok"
              className="alert-icon-large"
              text={this.i18n(`${connectorType.module}:wizard.${connectorType.name}.steps.four.help`, { escape: false })}
            />
          </Basic.Col>
        </Basic.Row>
      </Basic.Div>
    );
  }
}