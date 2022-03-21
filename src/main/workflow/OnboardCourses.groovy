// This is a post function script built for Jira Scriptrunner
// It creates subtasks in a set of issues based on the query and links them back to the triggering issue.

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

// Components
def issueService = ComponentAccessor.getIssueService()
def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
def searchService = ComponentAccessor.getComponent(SearchService.class)
def issueLinkManager = ComponentAccessor.getIssueLinkManager()
def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)

// Subtask Defines
final issueTypeName = 'Course Participation' // Course Participation issue type
final priorityName = 'Major'
def teamField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue).find { it.name == "Elhub Team" }
def teamName = issue.getCustomFieldValue(teamField) as String
def query = jqlQueryParser.parseQuery("project = EO AND issuetype = 'Course' AND status = Active AND ('Elhub Team' = '$teamName' OR 'Elhub Team' IS EMPTY)")

def subtaskIssueTypes = ComponentAccessor.constantsManager.allIssueTypeObjects.findAll { it.subTask }
def subTaskIssueType = subtaskIssueTypes.findByName(issueTypeName)
assert subTaskIssueType : "Could not find subtask issue type with name $issueTypeName. Available subtask issue types are ${subtaskIssueTypes*.name.join(", ")}"
def priorityId = ComponentAccessor.constantsManager.priorities.findByName(priorityName)?.id ?: prioritySchemeManager.getDefaultOption(parentIssue)

// Iterate through courses
def results = searchService.search(loggedInUser, query, PagerFilter.getUnlimitedFilter())
results.getResults().each { course ->
    def summary = course.getSummary() + " | " + issue.getSummary()
    def issueInputParameters = issueService.newIssueInputParameters().with {
        setProjectId(course.projectObject.id)
        setIssueTypeId(subTaskIssueType.id)
        setReporterId(loggedInUser.username)
        setSummary(summary)
        setPriorityId(priorityId)
    }

    def validationResult = issueService.validateSubTaskCreate(loggedInUser, course.id, issueInputParameters)
    assert validationResult.valid : validationResult.errorCollection

    def issueResult = issueService.create(loggedInUser, validationResult)
    assert issueResult.valid : issueResult.errorCollection

    def subtask = issueResult.issue
    ComponentAccessor.subTaskManager.createSubTaskIssueLink(course, subtask, loggedInUser)

    // Set assignee on subtask
    def courseResponsible = course.getAssignee()
    def assignResult = issueService.validateAssign(courseResponsible, subtask.id, courseResponsible.getUsername())
    issueService.assign(courseResponsible, assignResult)

    // Link to originating issue
    def issueLinkId = 10800 // Onboarding Link ID
    issueLinkManager.createIssueLink(subtask.id as long, issue.id as long,  issueLinkId as long, 1, loggedInUser)
}
