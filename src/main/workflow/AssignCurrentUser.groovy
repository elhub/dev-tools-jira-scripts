// This script will assign the current user to the issue, if no one is previously assigned.
import com.atlassian.jira.component.ComponentAccessor

def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

// issue is defined automatically when used as a post-function in Jira
if(!issue.assignee){
    issue.setAssignee((currentUser))
}
