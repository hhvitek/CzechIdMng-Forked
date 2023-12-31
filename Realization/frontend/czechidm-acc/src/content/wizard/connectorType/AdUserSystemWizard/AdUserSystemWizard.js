import React from 'react';
import DefaultSystemWizard from '../DefaultSystemWizard';
import StepOne from './StepOne';
import StepCrt from './StepCrt';
import StepCheckPermission from './StepCheckPermission';
import StepFour from './StepFour';

/**
 * Wizard for create a system for administration users from a MS AD.
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
export default class AdUserSystemWizard extends DefaultSystemWizard {

  constructor(props, context) {
    super(props, context);
    this.state = {showWizard: false};
    this.wizardContext = {};
  }

  getWizardId() {
    return 'ad-connector-type';
  }

  getModule() {
    return 'acc';
  }

  getWizardSteps(props, context) {
    const stepsDefault = super.getWizardSteps(props, context);
    // Remove name, connector, schema, mapping steps.
    stepsDefault.splice(0, 4);

    const steps = [];
    const stepOneId = 'stepOne';
    // Replace first step.
    const stepOne = {
      id: stepOneId,
      getComponent: () => {
        const {match, connectorType} = this.props;
        return (
          <StepOne
            match={match}
            wizardStepId={stepOneId}
            connectorType={connectorType}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(0, 0, stepOne);

    // Certificate step
    const stepTwoId = 'stepTwo';
    const stepsTwo = {
      id: stepTwoId,
      getComponent: () => {
        const {match, connectorType} = this.props;
        return (
          <StepCrt
            match={match}
            wizardStepId={stepTwoId}
            connectorType={connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(1, 0, stepsTwo);

    // If no trusted CA was found, then wizard has only two steps.
    if (this.context.wizardContext.activeStep
      && this.context.wizardContext.activeStep.id === 'stepTwo'
      && this.context.wizardContext.connectorType.metadata.hasTrustedCa === 'false'
      && this.context.wizardContext.connectorType.metadata.sslSwitch === 'true') {
      // If certificate is missing, then next button have to be hidden.
      steps[1].isLast = true;
      steps[1].hideFinishBtn = true;
    }

    // Permissions step
    const stepThreeId = 'stepThree';
    const stepsThree = {
      id: stepThreeId,
      getComponent: () => {
        const {match, connectorType} = this.props;
        return (
          <StepCheckPermission
            match={match}
            wizardStepId={stepThreeId}
            connectorType={connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(2, 0, stepsThree);

    const stepFourId = 'stepFour';
    const stepFour = {
      id: stepFourId,
      getComponent: () => {
        const {match, connectorType, reopened} = this.props;
        return (
          <StepFour
            match={match}
            wizardStepId={stepFourId}
            reopened={reopened}
            connectorType={connectorType}
            apiPath={this.getApiPath()}
            baseLocKey={this.getBaseLocKey()}
          />
        );
      }
    };
    steps.splice(3, 0, stepFour);

    steps.splice(steps.length, 0, ...stepsDefault);
    return [...steps];
  }
}
