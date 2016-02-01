package com.ceilfors.jenkins.plugins.jiratrigger

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup
import com.atlassian.jira.rest.client.api.domain.Issue
import com.ceilfors.jenkins.plugins.jiratrigger.changelog.ChangelogMatcher
import groovy.util.logging.Log
import hudson.Extension
import hudson.model.Cause
import hudson.model.ParameterValue
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

/**
 * @author ceilfors
 */
@Log
class JiraChangelogTrigger extends JiraTrigger<ChangelogGroup> {

    private List<ChangelogMatcher> changelogMatchers = []

    @DataBoundConstructor
    JiraChangelogTrigger() {
    }

    List<ChangelogMatcher> getChangelogMatchers() {
        return changelogMatchers
    }

    @SuppressWarnings("GroovyUnusedDeclaration") // Jenkins DataBoundSetter
    @DataBoundSetter
    void setChangelogMatchers(List<ChangelogMatcher> changelogMatchers) {
        this.changelogMatchers = changelogMatchers
    }

    boolean filter(Issue issue, ChangelogGroup changelogGroup) {
        for (changelogMatcher in changelogMatchers) {
            if (!changelogMatcher.matches(changelogGroup)) {
                log.fine("[${job.fullName}] - Not scheduling build: The changelog [${changelogGroup}] doesn't match with the changelog matcher [${changelogMatcher}]")
                return false
            }
        }
        return true
    }

    @Override
    protected List<ParameterValue> collectParameterValues(Issue issue, ChangelogGroup changelogGroup) {
        return []
    }

    @Override
    Cause getCause(Issue issue, ChangelogGroup changelogGroup) {
        return new JiraChangelogTriggerCause()
    }

    @SuppressWarnings("UnnecessaryQualifiedReference")
    @Extension
    static class JiraChangelogTriggerDescriptor extends JiraTrigger.JiraTriggerDescriptor {

        public String getDisplayName() {
            return "Build when an issue is updated in JIRA"
        }

        @SuppressWarnings("GroovyUnusedDeclaration") // Jenkins jelly
        public List<ChangelogMatcher.ChangelogMatcherDescriptor> getChangelogMatcherDescriptors() {
            return jenkins.getDescriptorList(ChangelogMatcher)
        }
    }

    static class JiraChangelogTriggerCause extends Cause {

        @Override
        String getShortDescription() {
            return "JIRA issue is updated"
        }
    }
}
