import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.Ignore

import com.lesfurets.jenkins.unit.BasePipelineTest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import util.Rules

class ChecksPublishResultsTest extends BasePipelineTest {
    Map publisherStepOptions
    List archiveStepPatterns

    @Rule
    public RuleChain ruleChain = Rules.getCommonRules(this)

    def checksPublishResultsScript

    @Before
    void init() {
        publisherStepOptions = [:]
        archiveStepPatterns = []
        // prepare checkResultsPublish step
        checksPublishResultsScript = loadScript("checksPublishResults.groovy").checksPublishResults
        // add handler for generic step call
        helper.registerAllowedMethod("step", [Map.class], {
            parameters -> publisherStepOptions[parameters.$class] = parameters
        })
        helper.registerAllowedMethod("archiveArtifacts", [Map.class], {
            parameters -> archiveStepPatterns.push(parameters.artifacts)
        })
    }

    @Test
    void testPublishWithDefaultSettings() throws Exception {
        checksPublishResultsScript.call()

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        // ensure nothing else is published
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
    }

    @Test
    void testPublishForJavaWithDefaultSettings() throws Exception {
        checksPublishResultsScript.call(pmd: true, cpd: true, findbugs: true, checkstyle: true)

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("PmdPublisher options not set", publisherStepOptions['PmdPublisher'] != null)
        assertTrue("DryPublisher options not set", publisherStepOptions['DryPublisher'] != null)
        assertTrue("FindBugsPublisher options not set", publisherStepOptions['FindBugsPublisher'] != null)
        assertTrue("CheckStylePublisher options not set", publisherStepOptions['CheckStylePublisher'] != null)
        // ensure default patterns are set
        assertEquals("PmdPublisher default pattern not set", '**/target/pmd.xml', publisherStepOptions['PmdPublisher']['pattern'])
        assertEquals("DryPublisher default pattern not set", '**/target/cpd.xml', publisherStepOptions['DryPublisher']['pattern'])
        assertEquals("FindBugsPublisher default pattern not set", '**/target/findbugsXml.xml, **/target/findbugs.xml', publisherStepOptions['FindBugsPublisher']['pattern'])
        assertEquals("CheckStylePublisher default pattern not set", '**/target/checkstyle-result.xml', publisherStepOptions['CheckStylePublisher']['pattern'])
        // ensure nothing else is published
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
    }

    @Test
    void testPublishForJavaScriptWithDefaultSettings() throws Exception {
        checksPublishResultsScript.call(eslint: true)

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("WarningsPublisher options not set", publisherStepOptions['WarningsPublisher'] != null)
        assertTrue("WarningsPublisher parser configuration number not correct", publisherStepOptions['WarningsPublisher']['parserConfigurations'].size() == 1)
        // ensure correct parser is set set
        assertEquals("ESLint parser not correct", 'JSLint', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['parserName'])
        // ensure default patterns are set
        assertEquals("ESLint default pattern not set", '**/eslint.xml', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['pattern'])
        // ensure nothing else is published
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
    }

    @Test
    void testPublishForPythonWithDefaultSettings() throws Exception {
        checksPublishResultsScript.call(pylint: true)

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("WarningsPublisher options not set", publisherStepOptions['WarningsPublisher'] != null)
        assertTrue("WarningsPublisher parser configuration number not correct", publisherStepOptions['WarningsPublisher']['parserConfigurations'].size() == 1)
        assertEquals('PyLint', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['parserName'])
        // ensure correct parser is set set
        assertEquals("PyLint parser not correct", 'PyLint', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['parserName'])
        // ensure default patterns are set
        assertEquals("PyLint default pattern not set", '**/pylint.log', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['pattern'])
        // ensure nothing else is published
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
    }

    @Test
    void testPublishNothing() throws Exception {
        checksPublishResultsScript.call(aggregation: false)

        // ensure nothing is published
        assertTrue("AnalysisPublisher options not empty", publisherStepOptions['AnalysisPublisher'] == null)
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
    }

    @Test
    void testPublishNothingExplicitFalse() throws Exception {
        checksPublishResultsScript.call(pmd: false)

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        // ensure nothing else is published
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
    }

    @Test
    void testPublishNothingImplicitTrue() throws Exception {
        checksPublishResultsScript.call(pmd: [:])

        // ensure pmd is not published
        assertTrue("PmdPublisher options not set", publisherStepOptions['PmdPublisher'] != null)
    }

    @Test
    void testPublishNothingExplicitActiveFalse() throws Exception {
        checksPublishResultsScript.call(pmd: [active: false])

        // ensure pmd is not published
        assertTrue("PmdPublisher options not empty", publisherStepOptions['PmdPublisher'] == null)
    }

    @Test
    void testPublishWithChangedStepDefaultSettings() throws Exception {
        // pmd has been set to active: true in step configuration
        checksPublishResultsScript.call(script: [commonPipelineEnvironment: [
            configuration: [steps: [checksPublishResults: [pmd: [active: true]]]]
        ]])

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("PmdPublisher options not set", publisherStepOptions['PmdPublisher'] != null)
        // ensure nothing else is published
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
    }

    @Test
    void testPublishWithCustomPattern() throws Exception {
        checksPublishResultsScript.call(eslint: [pattern: 'my-fancy-file.ext'], pmd: [pattern: 'this-is-not-a-patter.xml'])

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("PmdPublisher options not set", publisherStepOptions['PmdPublisher'] != null)
        assertTrue("WarningsPublisher options not set", publisherStepOptions['WarningsPublisher'] != null)
        assertTrue("WarningsPublisher parser configuration number not correct", publisherStepOptions['WarningsPublisher']['parserConfigurations'].size() == 1)
        // ensure custom patterns are set
        assertEquals("PmdPublisher custom pattern not set", 'this-is-not-a-patter.xml', publisherStepOptions['PmdPublisher']['pattern'])
        assertEquals("ESLint custom pattern not set", 'my-fancy-file.ext', publisherStepOptions['WarningsPublisher']['parserConfigurations'][0]['pattern'])
        // ensure nothing else is published
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
    }

    @Test
    void testPublishWithArchive() throws Exception {
        checksPublishResultsScript.call(archive: true, eslint: true, pmd: true, cpd: true, findbugs: true, checkstyle: true)

        assertTrue("ArchivePatterns number not correct", archiveStepPatterns.size() == 5)
        assertTrue("ArchivePatterns contains no PMD pattern", archiveStepPatterns.contains('**/target/pmd.xml'))
        assertTrue("ArchivePatterns contains no CPD pattern", archiveStepPatterns.contains('**/target/cpd.xml'))
        assertTrue("ArchivePatterns contains no FindBugs pattern", archiveStepPatterns.contains('**/target/findbugsXml.xml, **/target/findbugs.xml'))
        assertTrue("ArchivePatterns contains no CheckStyle pattern", archiveStepPatterns.contains('**/target/checkstyle-result.xml'))
        assertTrue("ArchivePatterns contains no ESLint pattern", archiveStepPatterns.contains('**/eslint.xml'))
    }

    @Test
    void testPublishWithPartialArchive() throws Exception {
        checksPublishResultsScript.call(archive: true, eslint: [archive: false], pmd: true, cpd: true, findbugs: true, checkstyle: true)

        assertTrue("ArchivePatterns number not correct", archiveStepPatterns.size() == 4)
        assertTrue("ArchivePatterns contains no PMD pattern", archiveStepPatterns.contains('**/target/pmd.xml'))
        assertTrue("ArchivePatterns contains no CPD pattern", archiveStepPatterns.contains('**/target/cpd.xml'))
        assertTrue("ArchivePatterns contains no FindBugs pattern", archiveStepPatterns.contains('**/target/findbugsXml.xml, **/target/findbugs.xml'))
        assertTrue("ArchivePatterns contains no CheckStyle pattern", archiveStepPatterns.contains('**/target/checkstyle-result.xml'))
        // ensure no ESLint  pattern is contained
        assertTrue("ArchivePatterns contains ESLint pattern", !archiveStepPatterns.contains('**/eslint.xml'))
    }

    @Test
    void testPublishWithDefaultThresholds() throws Exception {
        checksPublishResultsScript.call(pmd: true)

        assertTrue("AnalysisPublisher options not set",
            publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("PmdPublisher options not set",
            publisherStepOptions['PmdPublisher'] != null)
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalHigh not correct",
            '0', publisherStepOptions['AnalysisPublisher']['failedTotalHigh'])
        assertEquals("PmdPublisher thresholds configuration for failedTotalHigh not correct",
            '0', publisherStepOptions['PmdPublisher']['failedTotalHigh'])
        // ensure other values are empty
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalNormal is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalNormal'])
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalLow is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalLow'])
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalAll is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalAll'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalHigh not correct",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalHigh'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalNormal is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalNormal'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalLow is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalLow'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalAll is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalAll'])
        // ensure nothing else is published
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
    }

    @Test
    void testPublishWithThresholds() throws Exception {
        checksPublishResultsScript.call(aggregation: [thresholds: [fail: [high: '10']]], pmd: true)

        assertTrue("AnalysisPublisher options not set", publisherStepOptions['AnalysisPublisher'] != null)
        assertTrue("PmdPublisher options not set", publisherStepOptions['PmdPublisher'] != null)
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalHigh not correct",
            '10', publisherStepOptions['AnalysisPublisher']['failedTotalHigh'])
        // ensure other values are empty
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalNormal is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalNormal'])
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalLow is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalLow'])
        assertEquals("AnalysisPublisher thresholds configuration for failedTotalAll is set",
            null, publisherStepOptions['AnalysisPublisher']['failedTotalAll'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalHigh not correct",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalHigh'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalNormal is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalNormal'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalLow is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalLow'])
        assertEquals("AnalysisPublisher thresholds configuration for unstableTotalAll is set",
            null, publisherStepOptions['AnalysisPublisher']['unstableTotalAll'])
        // ensure nothing else is published
        assertTrue("DryPublisher options not empty", publisherStepOptions['DryPublisher'] == null)
        assertTrue("FindBugsPublisher options not empty", publisherStepOptions['FindBugsPublisher'] == null)
        assertTrue("CheckStylePublisher options not empty", publisherStepOptions['CheckStylePublisher'] == null)
        assertTrue("WarningsPublisher options not empty", publisherStepOptions['WarningsPublisher'] == null)
    }
}
