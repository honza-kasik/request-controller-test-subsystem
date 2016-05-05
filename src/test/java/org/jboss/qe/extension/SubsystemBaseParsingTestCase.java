package org.jboss.qe.extension;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

import java.io.IOException;

/**
 * This is the bare bones test example that tests subsystem
 * It does same things that {@link SubsystemParsingTestCase} does but most of internals are already done in AbstractSubsystemBaseTest
 * If you need more control over what happens in tests look at  {@link SubsystemParsingTestCase}
 * @author <a href="mailto:tomaz.cerar@redhat.com">Tomaz Cerar</a>
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(RQCTestExtension.SUBSYSTEM_NAME, new RQCTestExtension());
    }


    @Override
    protected String getSubsystemXml() throws IOException {
        return "<subsystem xmlns=\"" + RQCTestExtension.NAMESPACE + "\">" +
                "</subsystem>";
    }

}
