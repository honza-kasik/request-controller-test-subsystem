package org.jboss.qe.extension;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SubsystemDefinition extends PersistentResourceDefinition {
    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

    private static final AttributeDefinition DONE_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            RQCTestExtension.DONE_ATTRIBUTE_NAME, ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setXmlName(RQCTestExtension.DONE_ATTRIBUTE_NAME)
            .setFlags(AttributeAccess.Flag.STORAGE_CONFIGURATION)
            .setDefaultValue(new ModelNode(false))
            .setAllowNull(false)
            .build();

    private static final AttributeDefinition NO_ERRORS_ATTRIBUTE = new SimpleAttributeDefinitionBuilder(
            RQCTestExtension.NO_ERRORS_ATTRIBUTE_NAME, ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setXmlName(RQCTestExtension.NO_ERRORS_ATTRIBUTE_NAME)
            .setFlags(AttributeAccess.Flag.STORAGE_CONFIGURATION)
            .setDefaultValue(new ModelNode(false))
            .setAllowNull(false)
            .build();

    private SubsystemDefinition() {
        super(RQCTestExtension.SUBSYSTEM_PATH,
                RQCTestExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
        resourceRegistration.registerOperationHandler(RunTestOperationHandler.DEFINITION, RunTestOperationHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        resourceRegistration.registerReadOnlyAttribute(DONE_ATTRIBUTE, null);
        resourceRegistration.registerReadOnlyAttribute(NO_ERRORS_ATTRIBUTE, null);
    }

    @Override
    protected List<? extends PersistentResourceDefinition> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(DONE_ATTRIBUTE, NO_ERRORS_ATTRIBUTE);
    }

}
